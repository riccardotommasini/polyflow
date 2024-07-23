# API

API contains all the necessary interfaces to build a stream processor compliant with RSP4J architecture.


In [polyflow](../polyflow/README.md) it is possible to find custom implementations of various components (Continuous Program, Task, Operators, TimeVaryingObjects, SDS, DAG etc..), they all use the [default Task](../polyflow/src/main/java/shared/querying/TaskImpl.java)
and [ContinuousProgram](../polyflow/src/main/java/shared/coordinators/ContinuousProgramImpl.java). Please, refer to these examples in order to better understand how to instantiate and link the various components.

This package is divided as follows:
- coordinators: contains the ContinuousProgram interface
- enums: contains some default enums used in various implementations
- exceptions: a collection of custom exceptions
- operators: contains the core interfaces used by RSP4J to define the operators of the stream processor: S2R, R2R and R2S.
  * S2R contains the StreamToRelationOperator interface, the Consumer interface and the Window interface, alongside a Window implementation
  * R2R contains the RelationToRelationOperator interface, the DAG interface and the DAGNode interface
  * R2S contains the RelationToStreamOperator interface
- querying: contains the Task interface (which represents a query, push or pull)
- SDS: contains the SDS interface, the TimeVarying interface, the TimeVaryingFactory interface and a LazyTimeVarying implementation
- Secret: contains the components defined in the Secret paper (content, report, tick, time)
- Stream: contains the DataStream interface

### Components interaction

Although the core API module provides interfaces, and it is potentially possible to redefine all the components behaviours, the system was engineered
with the following high-level architecture in mind:

- The ContinuousProgram is the coordinator of the system, it receives queries, parses them (WIP) and creates the components needed to answer the query.
- The Task represents a query, it contains the S2R operators (windows), the R2R operators (chains operations on the
  windowed data) and the R2S operator (stream the result of the R2R operators in an output stream).
- The S2R operator represents a way to go from the streaming world (dynamic) to the materialized world (static). This concept is then realized by means of
  windowing operators, which accumulate data from the input stream and return them when needed (which data they return is dependent on the specific windowing strategy).
- The R2R operator represents an operation that should be applied on the materialized data.
  The set of data handled by the R2R operator must be closed under the defined operation (meaning that the operator takes as input a type R and outputs a type R)
- The R2S operator iterates over the result of the R2R operator and transforms it into a stream
