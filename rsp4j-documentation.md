# Polyflow4J Documentation
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
    - [Content](#content)
    - [DAG and DAGNode](#dag-and-dagnode)

## Introduction
RSP4j is a library that allows to create Stream Processing Engines in a transparent way with respect to the selected data models. In its essence, it's composed by a set of interfaces that split the system in various components, each with its own responsibility. \
The advantages of this library can be found in the [RSP4J paper](https://www.researchgate.net/publication/352796292_Web_stream_processing_with_RSP4J), although it was an old iteration of the system, the reasonings are still valid. Some of the advantages include fair benchmarking among different stream engines and fast prototyping.\
Let's see how the system works from a high-level perspective.
## System overview
### Interfaces
There are a few core interfaces in our library, all of them are implemented using Java generics to capture the type-agnostic philosophy.\
The generics used are: `I`, `W`, `R` and `O`.\
`I` Represents the type of the elements in the input stream (tuples, graphs, documents etc..).\
`W` Represents the type of the elements in the windows. Usually `W`=`I`, but it's not always the case.\
`R` Represents a type on which is defined a relational algebra (it is possible to apply a chain of operations on it).\
`O` Represents the type of the elements in the output stream.

 We will dive deep in each component of the system in the following sections, but we can start to give a general overview of the responisibilities:
- `ContinuousProgram<I, W, R, O>`:  Acts as the coordinator of the system, manages the queries and the input/output streams.
- `Task<I, W, R, O>`: Represents a query, contains the needed operators to answer it.
- `StreamToRelationOperator<I, W, R>`: An operator that represents a bridge between the streaming world and the static world, commonly implemented as a windowing operator.
- `RelationToRelationOperator<R>`: An operator that acts on windowed (static) data. Applies an operation on some data of type R and returns new data of type R as a result.
- `RelationToStreamOperator<R, O>`: An operator that transforms static data back to a stream of data.
- `DAG<R>`: Directed acyclic graph, used to represent a chain of Relation to Relation operators
- `DAGNode<R>`: Building blocks of the DAG
- `TimeVarying<R>`: As explained in the [CQL paper](https://www.researchgate.net/publication/2901127_The_CQL_Continuous_Query_Language_Semantic_Foundations_and_Query_Execution), "the application of an S2R operator over a stream of data returns a function, called Time-Varying Relation" (a relation that changes with time).\
    To make it simple, a Time-Varying Relation is a function that, given a timestamp, returns a Relation. We defined two of such functions by applying the concept to windowing and queries. The first one materializes a window on the data stream (a Relation), while the second one materializes the result of a query (still a Relation). The latter is obtained by chaining the materialization of a window (first function) with a set of Relation to Relation operators.
 

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
- `Map<DataStream<I>, List<Task<I, W, R, O>>> registeredTasks`: Maps an input stream to the interested Tasks.
- `Map<Task<I, W, R, O>, List<DataStream<O>>> taskToOutMap`: Maps a Task to the interested output streams.\

A Task can represent both a push query (automatically outputs the result when it's ready) or a pull query (wait for an external request before performing any computation):
if an Output Stream is associated with a Task in the `taskToOutMap` attribute, then it's treated as a push query, otherwise it's treated as a pull query. The concept of Materialized
Views is realized through the use of pull queries.

Our default Continuous Program overrides three methods, two are used to populate the data structures mentioned above, and one is a method it inherits from the interface `Consumer`, which is the `notify(InputStream<I>, I element, long timestamp)`, used by an `InputStream` to notify the Continuous Program that a new event entered the stream.\
The logic of the latter is pretty simple: when a new event enters the stream, all the Tasks are notified and their windows are updated. Then, all the Tasks with an associated output stream (push queries)
are asked to perform a computation (if any is required) and the (possibly empty) results are sent to the interested streams through the `taskToOutMap` object.
### Task
For the Task interface, we came up with a single default implementation, which can represent both a push query or a pull query.\
The attributes of the Task are the following:
- `List<StreamToRelationOperator<I, W, R>> s2rOperators`: List of all the S2R operators associated with the Task (basically, its windows).
- `List<RelationToRelationOperator<R>> r2rOperators`: List of all the R2R operators, it will be used to create the DAG.
- `Map<DataStream<I>, List<StreamToRelationOperator<I, W, R>>> registeredS2R`: Maps an input stream to all the S2R operators interested in it (windows over that stream)
- `Time time`: Time object, used to keep track of the logical time as well as the times at which a computation must occurr.
- `DAG<R> dag`: DAG object associated with the Task, represents the chain of operations that need to be performed by the Task to answer the query.
- `SDS<R> sds`: The SDS is a container for all the Time Varying of the various S2R operators of a Task.

The most interesting methods (ignoring getters, setters etc..) are three: 
- `void initialize()`: This method is used to build the SDS and the DAG of a Task, there is not much logic in here, most of it is found inside the DAG, so it will be explained later on.
- `TimeVarying<R> apply()`: This is an abuse of notation w.r.t. the explanation given earlier when talking about Time Varying for the first time. By definition, the application of an operator over a stream of data should return a Time Varying Relation. Here, instead, we use this 'apply' method to return a Time Varying Relation associated with the Task, without an explicit 'application of an operator'. In the end, anyways, we end up with a Time Varying Relation that can be queried as needed to obtain the result of the computation in a 'pull' fashion, given a timestamp. This is the logic behind a possible 'Lazy Evaluation'.
- `void elaborateElement(DataStream<I> inputStream, I element, long timestamp)`: This is the method called by the Continuous Program when an Input Stream emits an event and a Task is registered to it. The responsibility of the method is to update all the windows and checking if a computation needs to occur. 
- `Collection<Collection<O>> compute()`: This method is the one responsible of performing a computation (if the `Time` attribute contains some Evaluation Time Instants). It's currently called by the Continuous Program only on push queries (Tasks with an associated output stream).
  The reason why result is a `Collection<Collection<O>>` is the following: the innermost `Collection<O>` represents the result of a computation (a 'stream' of elements of type O), while the outer `Collection` is present because, for a single computation, we might want to report multiple results (for example, if an event makes N windows close, we might want to report all of them, and each window can be seen as an independent result).\
- `Collection<O> computeLazy(long ts)`: This method is used to query the Task at an arbitrary timestamp and obtain the result as a collection of output elements. It does not update any window, it just performs a computation at the given timestamp.


### Operators
This next section explores the various implementations of different operators (S2R, R2R, R2S). These components are highly customizable, especially the R2R operators, whose logic depends on the data model we're working with.
#### Stream To Relation
There are currently two S2R implementations, CQELS and CSPARQL. We will focus on the latter to give an example of how to implement the methods offered by the Stream To Relation interface.\
Let's first make a clarification, the Stream To Relation operator represents a window over the input stream (for example, a window of size 10 seconds sliding every 2 seconds). Does that mean that, for every window over the same input stream, we have multiple Stream To Relation operators? No, not in our implementation. Inside our Stream To Relation operator you will find multiple "window" objects, which represent the various windows with an opening and closing time, each with its own content. The Stream To Relation operator gives the 'blueprint' of the window, then as the time passes, multiple window objects are created inside the S2R, representing the (possibly) multiple active windows. This is the reason why you will find a `Map<Window, Content<I, W, R>>` in our S2R implementation.
- `Ticker ticker`: This object represents the Tick dimension as identified in the [SECRET Paper](https://www.researchgate.net/publication/220538262_SECRET_A_Model_for_Analysis_of_the_Execution_Semantics_of_Stream_Processing_Systems). The responsibility of a Ticker (for example, the Time Ticker) is to add to the Time object the time at which a computation must occurr.
- `Tick tick`: This represents the type of Tick (time driven, batch driven or tuple driven).
- `Time time`: The same type object instance that the Task object and its S2R operators share.
- `String name`: Name of the operator (window name).
- `ContentFactory<I, W, R> cf`: Factory for the Content of the window. We will talk about the implementation details of the Content later, it basically represents how data are stored in a window.
- `TimeVaryingFactory<R> tvFactory`: Factory to create the Time Varying object associated with the S2R operator.
- `ReportGrain grain`: Represents the granularity report of a window (multiple windows can be active and reported or only a single window can be reported), it is not used in this implementation.
- `Report report`: Represents the Report dimension of the [SECRET Paper](https://www.researchgate.net/publication/220538262_SECRET_A_Model_for_Analysis_of_the_Execution_Semantics_of_Stream_Processing_Systems). It is a set of conditions that must be fullfilled in order for a window to be ready to report its content, thus triggering a computation.
- `long width, slide`: Width of the window (in this case, a time width) and sliding parameter (distance between the opening time of two windows, also represented as a time).
- `Map<Window, Content<I, W, R>> active_windows`: Map representing the currently active windows. Each `Window` object has an opening time and a closing time, and has a Content associated to it, which holds the data belonging to that window.
- `Set<Window> to_evict`: Windows that need to be evicted ('expired' windows).
- `long t0`: Time at which the first window opens.

Concerning methods, the most important are:
- `Content<I, W, R> content(long t_e)`: Returns the content of the last window closed
- `void windowing(I arg, long ts)`: The core method of the S2R operator. After receiving an element and a timestamp associated to it, it opens all the windows that contain that element (if not already present) and adds the element to all of them. It then checks if some windows are ready to report, and if it's the case, it adds the timestamp at which the computation must occurr to the Time object. Finally, it schedules from eviction windows that should be removed after the computation.
- `TimeVarying<R> get()`: Returns the Time Varying object associated to the S2R operator.

#### Relation To Relation
As mentioned before, the R2R operator implementation is highly dependent on the choice of the `R` data type we use. We do not provide any defualt implementation for it, but there are a few examples in our repository where you can find some Graph R2R operators and some Relational R2R operators. In any case, each R2R operator *should* have a list of operand names to which the operator is applied (for example, a list of Strings), and a result name (for example, a String).\
It has a `R eval(List<R> operands)` method, which takes as input your operands and returns the result of the R2R operation.
#### Relation To Stream
As for the R2R opeator, the R2S operator is also dependent on the specific types we use in our systems. Indeed, its job is to transform an object of type `R` in a Collection of objects of type `O`, which represent the output stream. 
### Time Varying
So far we've mentioned multiple times the Time Varying objects, and we gave a brief explanation of what they are in the [Interfaces](#interfaces) section. We now see two different implementations of Time Varying objects: one oriented towards retrieving data from a window given a timestamp, and the other oriented towards materializing the results of a full computation given a timestamp.\
The Time Varying object used to materialized the content of a window at a specific time instant is implemented as follows:
- `StreamToRelationOperator<?, ?, R> op`: The operator from which we want to materialize a given window.
- `String name`: The name of the Time Varying, which should correspond to the name of the S2R associated to it.
- `R content`: The materialized content of the window we retrieved at a certain time instant.

The most important methods are: 
- `materialize(long ts)`: retrieves the content from the associated S2R operator at the given timestamp and populates the `content` field.
- `R get()`: returns the `content` field.

To summarize, the responsibility of this type of Time Varying is to return the content of a specific window given a timestamp.

The other implementation is called LazyTimeVarying. It has the same methods as the previous one, but instead of materializing the content of a window given a timestamp, it materializes the result of a full computation. This result is achieved by storing, instead of a S2R operator, a node of the DAG associated to the Task of which we want to retrieve the result. When we call the `materialize` method, we actually trigger a full computation on the DAG and store the final result. We call it 'Lazy' because the result can be polled when needed.\
As we said, everything is the same as the non-lazy implementation, the only difference is the `StreamToRelationOperator<?, ?, R> op` field, which is replaced by the `DAGNode<R> dag` field.\
We will enter into the details of the `DAG` and `DAGNode` in one of the following sections. 
### Content
We described the Content before as something that "holds the data belonging to a window". The description is correct, but incomplete: our Content is indeed a structure that holds the data of a given window, but it is not just a "bag" of elements. What if, for optimization purposes, we just want to store the maximum value for each window? Or just the first element that enters a window? We need a way to optimize such operations, which would take a linear time if we just had a "bag" of elements as a Content.\
The solution we found was to decouple the concept of "Content" from the way in which data are stored inside it by defining custom implementations of the `Content<I, W, R>` interface.\
We provide a few of such implementations, for example:
- `AccumulatorContent`: accumulates all the values of a window in a single data structure, does not provide any particular logic.
- `FilterContent`: this implementation allows to filter elements before they even enter the window, instead of storing all the data and then filtering during the R2R operations.
- `FirstContent`: stores only the first element to enter a window.
- `LastContent`: stores only the last element to enter a window.

Of course, all of these implementations keep the Java Generics `<I, W, R>` since their logic is decoupled from the particular data types. That being said, when using them, the user must provide `Function` objects that describe how to convert the type `I` of the input element into a type `W`, and a function that transform a type `W` into a type `R`.\
`W` is a data type that represents the type of the elements inside the window, and it is common that either `W` = `I` or that `W` = `R`, although sometimes it might be useful to use a custom `W` for memory efficiency (for example, compressing elements when they are in the window, and decompressing them when the R2R are required).\
Let's see how we implemented the `AccumulatorContent`, starting from its attributes:
- `List<W> content = new ArrayList<>()`: the structure that holds the elements of a window.
- `Function<I, W> f1`: function that maps an input element of type `I` to an element of type `W`.
- `Function<W, R> f2`: function that maps an element of type `W` to an element of type `R`.
- `BiFunction<R, R, R> sumR`: function that reduces two `R` elements in a single one.
- `R emptyContent`: neutral element of the `sumR` operation.

And we have two main methods, belonging to the `Content<I, W, R>` interface: 
- `void add(I e) {content.add(f1.apply(e));}`: transforms the element of type `I` to an element of type `W` (through the application of the function `f1`), and adds it to the `content` structure.
- `R coalesce() {return content.stream().map(f2).reduce(emptyContent,  (x, y) -> sumR.apply(x,y));}`: this method is called when the content of a given window needs to be materialized in order to perform the chain of R2R computations. For each element `W`, apply the function `f2` to transform it in a type `R`, and use the `sumR` function to merge all the results.


### DAG and DAGNode
The DAG is one of the most complex part of the system, it's the core component used to chain together sequences of R2R operations and to effectively answer a query.\
In our implementation, the `DAG` class is in charge of building the graph of operations by instantiating and linking together multiple `DAGNode` elements, each of which holds an R2R operator.\
We will first talk about the single building blocks, the `DAGNode` elements, and then move forward with the `DAG`.\
The `DAGNode<R>` interface defines some methods signatures, but the most important are:
- `R eval(long ts)`: computes the result of the current node by applying its R2R operator.
- `TimeVarying<R> apply()`: returns a Time Varying object associated to the current node. 

We defined three different implementations of the `DAGNode` interface, and they override the previous methods in a slightly different way:
- `DAGRootNodeImpl`: represents the starting point of one of the possible paths of the DAG, it's associated to a window (or table) defined in the query.
- `UnaryDAGNodeImpl`: a node of the DAG that holds a unary R2R operator.
- `BinaryDAGNodeImpl`: a node of the DAG that holds a binary R2R operator.

The computation starts at the end of the DAG (by definition, a well-formed `DAG` will converge to a single last `DAGNode`), this last node is a `UnaryDAGNodeImpl` or a `BinaryDAGNodeImpl`.\
The `eval(long ts)` method of this two type of nodes is implemented as follows:
- `R eval(long ts) {return this.r2rOperator.eval(List.of(prev.get(0).eval(ts)));}`
- `R eval(long ts) {return this.r2rOperator.eval(List.of(prev.get(0).eval(ts), prev.get(1).eval(ts)));}`

Of course, the first being the Unary node and the second being the Binary node.\
Basically, each node returns the evaluation of its R2R operator called on the result of the previous `DAGNode` result. Now the question becomes: what happens when we reach the root node?\
This is the reason why we have special nodes acting as root nodes, we need to retrieve the first `R` operand that will kickstart the chain of evaluation.\
The `eval(long ts)` method of the `DAGRootNodeImpl` is implemented as follows:
 ```java
R eval(long ts) {
    tvg.materialize(ts); 
    return tvg.get();
    }
```
The `tvg` is the Time Varying object associated with this specific `DAGNode`. In a "standard" implementation, it materializes the content of a window (it's associated with a S2R operator, as explained above), but it could also be the Time Varying object associated with a full `Task` (the second type of Time Varying we explained), this is the way in which we implemented the concept of Materialized View.

Now that we have explained the building blocks, we can proceed with the implementation of the `DAG` itself, starting by the attributes: 
- `Map<String, DAGNode<R>> root`: map between the name of a partial result and the `DAGNode` that produces that result.
- `DAGNode<R> tail`: tail of the `DAG`, stored for efficiency porpuses since the computation starts from the tail.

The most important methods are the following: 
- `void addTVGs(Collection<TimeVarying<R>> sds)`: this is the first method called when the `DAG` is built, it basically adds all the sources (`DAGRootNodeImpl`) to the `root` map. The `DAGRootNodeImpl` do not have any previous nodes, they are effectively the "root" of the `DAG`, and all the subsequent nodes will be attached to them.
- `void addToDAG(RelationToRelationOperator<R> op)`: second method to be called when building the `DAG`, it adds a `DAGNode` for the corresponding `RelationToRelationOperator` passed as a parameter. The R2R operator knows the name of the partial result that comes before it (the node it should attach to), and using the `root` map we can efficiently retrieve the `DAGNode` associated with it and attach our new `DAGNode`. Of course, the assumption here is that all the R2R operators are added in order when creating the `DAG`.

The `DAG` also has a `eval(long ts)` and `apply()` methods, which just forward the same calls to the tail.