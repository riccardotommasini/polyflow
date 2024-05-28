# RSP4J Documentation
The goal of this document is to give a high level overview of the system as well as a deep explanation of the implementation details of the current system. We will first begin with the goal of RSP4J, the interfaces and the high-level communication between 
components. After that, we will dive in the technical details of each available implementation.

- [Introduction](#introduction)
- [System overview](#system-overview)
    - [Interfaces](#interfaces)
    - [Components Interaction](#components-interaction)
- [Implementations](#implementations)
    - [Continuous Program](#continuous-program)
    - [Task](#task)
    - [Operators](#operators)
        - [Stream To Relation](#stream-to-relation)
        - [Relation To Relation](#relation-to-relation)
        - [Relation To Stream](#relation-to-stream)
    - [Time Varying](#time-varying)

## Introduction
RSP4J is a library that allows to create Stream Processing Engines in a transparent way with respect to the chosen data models. In its essence, it's composed by a set of interfaces that split the system in various components, each with its own responsibility. \
The advantages of this library can be found in the [associated paper](https://www.researchgate.net/publication/352796292_Web_stream_processing_with_RSP4J) (although it was an old iteration of the system, the reasonings are still valid). Some of the advantages include fair benchmarking among different stream engines and fast prototyping.\
RSP4J was originally born as a library to write stream processing engines for RDF and was later extended (using Java generics) to support multiple data models, making it agnostic to the data model used.\
Let's see how the system works from a high-level perspective.
## System overview
### Interfaces
There are a few core interfaces in our library, all of them are implemented using Java generics to capture the type-agnostic philosophy.\
The generics used are: `I`, `W`, `R` and `O`.\
`I` Represents the type of the elements in the input stream (tuples, graphs, documents etc..).\
`W` Represents the type of the elements in the windows. Usually `W`=`I`, but it's not always the case.\
`R` Represents a type on which is defined a relational algebra (it is possible to apply a chain of operations on it).\
`O` Represents the type of the elements in the output stream.\

 We will dive deep in each component of the system in the following sections, but we can start to give a general view of the responisibilities:
- `ContinuousProgram<I, W, R, O>`:  Acts as the coordinator of the system, manages the queries and the input/output streams.
- `Task<I, W, R, O>`: Represents a query, contains the needed operators to answer it.
- `StreamToRelationOperator<I, W, R>`: An operator that represents a bridge between the streaming world and the static world, commonly implemented as a windowing operator.
- `RelationToRelationOperator<R>`: An operator that acts on windowed (static) data. Applies an operation on some data of type R and returns new data of type R as a result.
- `RelationToStreamOperator<R, O>`: An operator that transforms static data back to a stream of data.
- `DAG<R>`: Directed acyclic graph, used to represent a chain of Relation to Relation operators
- `DAGNode<R>`: Building blocks of the DAG
- `TimeVarying<R>`: As explained in the [CQL paper](https://www.researchgate.net/publication/2901127_The_CQL_Continuous_Query_Language_Semantic_Foundations_and_Query_Execution), "the application of an operator over a stream of data returns a function, called Time-Varying Relation" (a relation that changes with time).\
    To make it simple, a Time-Varying Relation is a function that, given a timestamp, returns a Relation. We defined two of such functions by applying the concept to windowing and queries. The first one materializes a window on the data stream (a Relation), while the second one materializes the result of a query (still a Relation). The latter is obtained by chaining the materialization of a window (first function) with a set of Relation to Relation operators.\
 

### Components Interaction
Let's continue with the interaction between the components defined so far.\
As mentioned above, the Continuous Program manages the queries and the input/output streams, in particular:
- when an event arrives from an input stream, the Continuous Program notifies all the Tasks interested in that stream
- when a Task emits the result of a computation, the Continuous Program forwards it to all the interested output streams

Each Task is composed by Stream To Relation (S2R), Relation To Relation (R2R) and Relation to Stream (R2S) operators, which work together to provide the query result. In particular, each S2R operator represents a window over a stream of data, and a Task can have multiple instances of such operator to consume from multiple input streams. Then, a sequence of R2R operators is applied to the windows, this sequence is represented by the DAG and DAG nodes. Finally, a R2S operator is applied to transform the result of the DAG in a data stream, which will be then sent to the interested output streams (the ones managed by the Continuous Program).\
As mentioned in the previous section, the application of a S2R operator over the data stream returns a Time-Varying Relation, which is the function used to retrieve a window of data given a timestamp.

## Implementations
What we defined above are the intended behaviours of the various components of RSP4J, the specific implementations are up to the user.\
That being said, we provide some (fully generic) default implementations to show an example of how the library can be used and to reduce the time needed to do prototyping of only some components, without having to implement the full system. For instance, someone that only wants to test out its ground-breaking algorithm to store elements in a window is not forced to build all the system from scratch.\
In this section we dive deep in our custom implementations to make it easier to understand how the system works without reading thousands of lines of code.
### Continuous Program
Our implementation of the Continuous Program keeps the Java generics defined in the respective interface. It has the following attributes: 
- `List<Task<I, W, R, O>> taskList`: List of all defined Tasks. 
- `List<Task<I, W, R, O>> viewList`: List of all defined Views.
- `Map<DataStream<I>, List<Task<I, W, R, O>>> registeredViews`: Maps an input stream to the interested Views.
- `Map<DataStream<I>, List<Task<I, W, R, O>>> registeredTasks`: Maps an input stream to the interested Tasks.
- `Map<Task<I, W, R, O>, List<DataStream<O>>> taskToOutMap`: Maps a Task to the interested output streams.\
The difference between a Task and a View is that a Task can 'push' a result when ready, while a View must be explicitely queried in order to trigger a computation and obtain a result. A user can define a View and use it as part of another query.

Our default Continuous Program overrides three methods, two are used to populate the data structures mentioned above, and one is a method it inherits from the interface `Consumer`, which is the `notify(InputStream<I>, I element, long timestamp)`, used by an `InputStream` to notify the Continuous Program that a new event entered the stream.\
The logic of the latter is pretty simple: when a new event enters the stream, the first to be notified are the Views, then the Tasks. The reason is simple, by notifying a Task we might trigger a computation, and the computation might involve a View, which is yet to be updated. By notifying the Views first, we make sure that they are up to date, and we can then proceed to notify the Tasks.\
After a computation occurred, we take the result and send it to the interested output streams through the `taskToOutMap` object defined above.
### Task
### Operators
#### Stream To Relation
#### Relation To Relation
#### Relation To Stream
### Time Varying
