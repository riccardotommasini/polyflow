# Polyflow

Polyflow is a small Java framework for building data-model agnostic stream
reasoning processors. It follows the RSP/SECRET-style processing pipeline:

```text
DataStream -> S2R windows -> SDS / time-varying relations -> R2R DAG -> R2S output stream
```

The project is designed around interfaces first. The `api` module defines the
contracts for streams, tasks, windows, time, reporting, state, and operators.
The `base` module provides reusable implementations that can be specialized for
tuples, RDF graphs, tables, domain events, or any other relation type that
implements `Iterable<?>`.

## Repository Layout

```text
.
+-- api/                         Core interfaces and SECRET/RSP abstractions
+-- base/                        Generic implementations built on the API
+-- rsp4j-documentation.md       Historical/architecture documentation
+-- pom.xml                      Maven parent project
`-- LICENSE                      Apache License 2.0
```

Important packages:

- `org.streamreasoning.polyflow.api.processing`: `ContinuousProgram` and
  `Task`, the high-level orchestration interfaces.
- `org.streamreasoning.polyflow.api.operators`: S2R, R2R, R2S, and DAG
  contracts.
- `org.streamreasoning.polyflow.api.secret`: time, tick, reporting, and content
  abstractions inspired by SECRET.
- `org.streamreasoning.polyflow.api.sds`: Stream Data Store and
  `TimeVarying` interfaces.
- `org.streamreasoning.polyflow.base.processing`: default `Task` and
  continuous-program implementations.
- `org.streamreasoning.polyflow.base.operatorsimpl.s2r`: sliding, hopping, and
  frame window implementations.
- `org.streamreasoning.polyflow.base.operatorsimpl.dag`: default DAG nodes and
  execution.
- `org.streamreasoning.polyflow.base.operatorsimpl.s2r.state`: single-buffer and
  multi-buffer state backends.
- `org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment`: simple
  list-backed segment implementations.

See also:

- [API module notes](api/Readme.md)
- [Base module notes](base/README.md)
- [S2R state model guide](base/src/main/java/org/streamreasoning/polyflow/base/operatorsimpl/s2r/STATE_MODEL.md)

## Core Concepts

Polyflow separates stream reasoning into four composable layers:

1. `DataStream<I>` receives timestamped input elements and notifies registered
   consumers.
2. `StreamToRelationOperator<I, R>` materializes stream elements into relation
   snapshots. These are the S2R window operators.
3. `RelationToRelationOperator<R>` transforms one or more materialized
   relations into another relation. `TaskImpl` wires these operators into a
   `DAG<R>`.
4. `RelationToStreamOperator<R, O>` turns a materialized relation into output
   stream elements.

The generic type `R` must extend `Iterable<?>`. Polyflow does not require `R` to
be an RDF graph or table. Datatype-specific behavior belongs in:

- `Segment<I, R>` implementations, which buffer input and coalesce it into `R`;
- R2R operators, which know how to query or transform `R`;
- R2S operators, which know how to emit output elements.

## Modules

### `api`

The API module contains the stable contracts:

- `ContinuousProgram<I, W, R, O>`: manages tasks and receives stream
  notifications.
- `Task<I, W, R, O>`: groups S2R, R2R, R2S, SDS, DAG, and time state for a
  query or view.
- `StreamToRelationOperator<I, R>`: window/state assigner interface.
- `RelationToRelationOperator<R>`: relational transformation interface.
- `RelationToStreamOperator<R, O>`: output stream conversion interface.
- `SDS<R>` and `TimeVarying<R>`: stream data store abstractions.
- `Report`, `Tick`, and `Time`: execution policy primitives.

### `base`

The base module provides reusable implementations:

- `defaultDataStream<I>`: simple in-memory stream that forwards `put(e, ts)` to
  consumers.
- `TaskImpl`: stores operators, initializes time-varying objects in an SDS,
  evaluates the R2R DAG, applies R2S output, and evicts windows after push
  computations.
- `ContinuousProgramImpl`: registers tasks against input and output streams and
  computes push-query results when stream events arrive.
- `ParallelContinuousProgram`: partitions a single logical task across multiple
  task copies using a key function.
- `DAGImpl`: links time-varying roots to unary or binary R2R operators.
- S2R operators:
  - `SBSlidingWindowOpImpl`
  - `SBHoppingWindowOpImpl`
  - `SBFramesWindowOpImpl`
  - `MBSlidingWindowOpImpl`
  - `MBHoppingWindowOpImpl`
- State and segment helpers:
  - `SingleBufferState`
  - `MapMultiBufferState`
  - `ListSegmentFactory`
  - `ListSegment`

## Requirements

- Java 26 or newer
- Maven 3.x

The parent `pom.xml` configures `maven-compiler-plugin` with release level
`26`, so older JDKs cannot build the project.

## Build

From the repository root:

```bash
mvn clean install
```

Run checks:

```bash
mvn test
```

Build only one module:

```bash
mvn -pl api test
mvn -pl base -am test
```

## Use as a Maven Dependency

After installing locally with `mvn clean install`, depend on the modules you
need:

```xml
<dependency>
  <groupId>org.streamreasoning.polyflow</groupId>
  <artifactId>api</artifactId>
  <version>1.1</version>
</dependency>

<dependency>
  <groupId>org.streamreasoning.polyflow</groupId>
  <artifactId>base</artifactId>
  <version>1.1</version>
</dependency>
```

Use `api` when implementing your own processor from scratch. Use `base` when
you want the default task, continuous-program, DAG, stream, state, segment, or
window implementations.

## Minimal Example

This example builds a task that windows timestamped events, applies an identity
R2R operator, and emits the event values.

```java
import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.report.ReportImpl;
import org.streamreasoning.polyflow.api.secret.report.strategies.NonEmptyContent;
import org.streamreasoning.polyflow.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeImpl;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.operatorsimpl.dag.DAGImpl;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.SBSlidingWindowOpImpl;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.ListSegmentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.state.SingleBufferState;
import org.streamreasoning.polyflow.base.processing.TaskImpl;
import org.streamreasoning.polyflow.base.sds.SDSDefault;
import org.streamreasoning.polyflow.base.stream.defaultDataStream;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

record Event(String value, long timestamp) {}

DataStream<Event> input = new defaultDataStream<>("events");
Time time = TimeImpl.forStartTime(0);

SegmentFactory<Event, List<Event>> segments = new ListSegmentFactory<>(
        List::of,
        (left, right) -> Stream.concat(left.stream(), right.stream()).toList(),
        List.of());

SingleBufferState<Event, List<Event>> state = new SingleBufferState<>(
        segments,
        (event, cutoff) -> event.timestamp() < cutoff,
        (event, open) -> event.timestamp() >= open);

Report report = ReportImpl.fromStrategies(
        new OnWindowClose(),
        new NonEmptyContent());

var window = new SBSlidingWindowOpImpl<>(
        Tick.TIME_DRIVEN,
        time,
        "recent-events",
        state,
        report,
        1_000);

RelationToRelationOperator<List<Event>> identity =
        new RelationToRelationOperator<>() {
            public List<Event> eval(List<List<Event>> datasets) {
                return datasets.get(0);
            }

            public List<String> getTvgNames() {
                return List.of("recent-events");
            }

            public String getResName() {
                return "identity";
            }
        };

RelationToStreamOperator<List<Event>, String> toValues =
        new RelationToStreamOperator<>() {
            public String transform(Object event, long ts) {
                return ((Event) event).value();
            }
        };

Task<Event, Event, List<Event>, String> task = new TaskImpl<>("demo");
task.addS2ROperator(window, input)
        .addR2ROperator(identity)
        .addR2SOperator(toValues)
        .addDAG(new DAGImpl<>())
        .addSDS(new SDSDefault<>())
        .addTime(time);
task.initialize();

task.elaborateElement(input, new Event("a", 100), 100);
task.elaborateElement(input, new Event("b", 1_200), 1_200);

Collection<Collection<String>> results = task.compute();
```

For push-style processing, register the initialized task with
`ContinuousProgramImpl` and call `put` on the input stream. The continuous
program forwards output records to the task's configured output streams.

## Window and State Model

S2R operators are split between window logic and datatype storage:

- Single-buffer operators keep one moving buffer and append each element once.
- Multi-buffer operators keep one segment per active window and may replicate an
  element into several windows.
- `Segment<I, R>` owns materialization from stream element type `I` to relation
  type `R`.

Use `SingleBufferState` for frame windows and moving-buffer sliding windows. Use
`MapMultiBufferState` when overlapping windows need independent buffers.

The detailed guide is in
[STATE_MODEL.md](base/src/main/java/org/streamreasoning/polyflow/base/operatorsimpl/s2r/STATE_MODEL.md).

## Implementing Your Own Datatype

To add a new stream datatype:

1. Choose an input event type `I`.
2. Choose a materialized relation type `R extends Iterable<?>`.
3. Implement `Segment<I, R>` or provide a `SegmentFactory<I, R>`.
4. Implement one or more `RelationToRelationOperator<R>` classes.
5. Optionally implement `RelationToStreamOperator<R, O>` for custom output
   records.
6. Assemble these pieces in a `TaskImpl` with a `DAGImpl`, `SDSDefault`, and
   `TimeImpl`.

## Testing Status

The repository contains historical JUnit test files under `base/src/test/java`,
but they are currently commented out. As a result, `mvn test` may compile the
project without exercising meaningful assertions once a Java 26+ JDK is active.

## Current Limitations

- `ContinuousProgram.buildTask(String query)` is a placeholder; tasks are
  currently assembled programmatically.
- The base implementations are generic infrastructure, not a complete
  datatype-specific engine.
- `SDSDefault.add(String iri, TimeVarying<R>)` and `SDSDefault.toStream()` are
  currently no-op/null placeholders.
- `BatchTicker` requires a batch size through `setBatch(int)` before useful
  batch-driven execution.

## License

Polyflow is released under the [Apache License 2.0](LICENSE).
