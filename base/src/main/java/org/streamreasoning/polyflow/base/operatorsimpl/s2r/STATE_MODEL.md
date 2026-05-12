# Polyflow State Model

This document explains the state model used by Polyflow's stream-to-relation
(S2R) operators. It is meant as a technical user guide for implementing new
windows, new state backends, and new datatypes.

Polyflow is data-model agnostic: the core library does not know whether a stream
element is a tuple, an RDF graph, a property-graph edge, a JSON document, or
some domain object. The state model is the layer that lets an S2R operator
maintain window contents without depending on a concrete datatype.

## The CsvWriter Abstractions

The S2R layer is built around three concepts:

- `Segment<I, R>`: the material content of one buffer.
- `SingleBufferState<I, R>`: a state with one current buffer.
- `MultiBufferState<I, R>`: a state with many buffers, one per logical window.

The generic parameters are:

- `I`: the input stream element type.
- `R`: the relation type consumed by R2R operators. It must extend
  `Iterable<?>`.

The older `Content<I, W, R>` abstraction is deprecated. In the new model, S2R
state is expressed as `I -> R`. If legacy content code still needs `W`, it
should treat `W` as an internal compatibility detail, not as part of the new
S2R state contract.

## Segment

`Segment<I, R>` is the datatype-specific buffer. It knows how to accept input
elements and how to materialize them as an `R`.

```java
public interface Segment<I, R> {
    int size();
    void add(I e);
    R coalesce();

    default boolean toReport() { return false; }
    default void remove(I e) { ... }
    default Iterator<I> iterator() { ... }
}
```

Important responsibilities:

- `add(I e)` inserts an input element into the segment.
- `coalesce()` materializes the buffer into an `R` relation.
- `toReport()` lets segment-level state participate in reporting strategies
  such as `OnStateReady`.
- `iterator()` and `remove(I e)` are optional, but needed by state
  implementations that trim elements from a single buffer.

The default base implementation is `ListSegment<I, R>`. It stores input
elements in a list and uses:

- a lifting function `Function<I, R>` to turn each input element into an `R`;
- a reducer `BiFunction<R, R, R>` to merge materialized values;
- an empty `R` value as the neutral element.

Use `ListSegment` when a simple accumulator is enough. Use a custom segment
when your datatype benefits from a specialized representation, such as a graph
index, table builder, hash structure, or domain-specific aggregate.

## SegmentFactory

`SegmentFactory<I, R>` creates segment instances for states and operators.

```java
public interface SegmentFactory<I, R> {
    Segment<I, R> createEmpty();
    Segment<I, R> create();
}
```

Multi-buffer states use the factory to create one segment per logical window.
Single-buffer states may use a factory, a prebuilt segment, or a custom segment
depending on their implementation.

## Multi-Buffer State

`MultiBufferState<I, R>` models a collection of logical windows. Each window has
its own segment.

```java
public interface MultiBufferState<I, R> {
    Segment<I, R> create(Window w);
    Segment<I, R> get(Window w);
    Iterable<Window> windows();
    void evict(Window w);
    Segment<I, R> emptySegment();
    int size();
    boolean isEmpty();
}
```

This model is appropriate when one input element may belong to multiple
simultaneous windows. The window operator is responsible for deciding which
windows an element belongs to, and then it inserts the element into each matching
segment.

Typical behavior:

1. The operator opens all windows that may contain the current timestamp.
2. It iterates over `state.windows()`.
3. For every matching window, it calls `state.get(window).add(element)`.
4. Expired windows are evicted at window level with `state.evict(window)`.

The default implementation is `MapMultiBufferState<I, R>`, backed by:

```java
Map<Window, Segment<I, R>>
```

Use `MapMultiBufferState` for overlapping or multi-window operators such as
multi-buffer hopping and sliding windows.

## Single-Buffer State

`SingleBufferState<I, R>` models one current buffer and one current logical
window.

```java
public interface SingleBufferState<I, R> {
    void setWindow(Window w);
    Window getWindow();
    Segment<I, R> segment();
    void append(I e);
    void evict(long ts);
    void clear();
    Segment<I, R> emptySegment();
    int size();
    boolean isEmpty();
}
```

This model is appropriate when each input element is inserted at most once.
There is no replication across logical windows. The operator uses the state as a
single moving buffer.

Single-buffer operators are responsible for deciding when the current logical
window starts, moves, closes, or resets. The state is responsible for maintaining
the actual buffer.

Typical behavior:

1. The operator sets or updates the current `Window`.
2. It calls `state.append(element)` once.
3. For sliding single-buffer windows, it calls `state.evict(cutoff)` to trim old
   elements.
4. For frame-like windows, it calls `state.clear()` when a frame is closed.

The default implementation is `ListSingleBufferState<I, R>`, which uses a
single segment and removes elements through `Segment.iterator()` or
`Segment.remove(...)`.

Use `ListSingleBufferState` for single-buffer sliding windows or frame windows
whose segment supports being cleared or rebuilt by the state.

## Choosing Single Buffer or Multi Buffer

The choice is not only an optimization. It changes the operational semantics of
the window.

Use a multi-buffer state when:

- many logical windows may be alive at the same time;
- one input element may be replicated into multiple windows;
- eviction is naturally done by dropping a whole window;
- each `Window` should have an independent `Segment`.

Use a single-buffer state when:

- there is only one material buffer;
- one input element should be inserted once;
- the buffer slides by removing elements from its head, or resets on frame
  close;
- `Window` describes the current view over the buffer rather than a distinct
  storage object.

Examples:

- `MBHoppingWindowOpImpl`: multi-buffer, because overlapping hopping windows can
  all receive the same element.
- `MBSlidingWindowOpImpl`: multi-buffer implementation of a sliding style where
  windows are materialized separately.
- `SBSlidingWindowOpImpl`: single-buffer, because the state maintains one moving
  buffer and trims old elements.
- `SBFramesWindowOpImpl`: single-buffer, because a frame is one current segment
  that opens, updates, closes, and resets.

## Frame Windows

`SBFramesWindowOpImpl<I, R>` is the concrete single-buffer frame operator.

A frame has a lifecycle:

1. `open`: initialize frame context and create the current logical window.
2. `update`: append an element and update frame context.
3. `close`: close the current window, expose its segment for reporting, and
   clear the state.

The operator supports these frame types:

- `FrameType.THRESHOLD`: compare the current element value with
  `frameParameter`.
- `FrameType.DELTA`: compare `abs(firstValue - currentValue)` with
  `frameParameter`.
- `FrameType.AGGREGATE`: compare a running aggregate with `frameParameter`.
- `FrameType.SESSION`: compare the gap between the current timestamp and the
  last timestamp with `frameParameter`.

For non-session frames, the numeric value is extracted with:

```java
ToDoubleFunction<I> valueExtractor
```

For aggregate frames, the aggregate is selected with:

```java
AggregationFunction.SUM
AggregationFunction.AVG
```

The closing condition is selected with `FrameClosingCondition`, not with a user
provided comparator:

```java
FrameClosingCondition.GREATER_THAN
FrameClosingCondition.LESS_THAN
FrameClosingCondition.LESS_OR_EQUAL
FrameClosingCondition.GREATER_OR_EQUAL
FrameClosingCondition.EQUAL
```

There is also a symbol parser:

```java
FrameClosingCondition.fromSymbol(">=");
```

So a parser or UI can pass `">"`, `"<"`, `"<="`, `">="`, `"="`, or `"=="`.

Conceptually, the closing condition means: close the frame when the observed
value satisfies the condition against `frameParameter`. For example,
`GREATER_OR_EQUAL` closes when:

```java
observedValue >= frameParameter
```

## Data Type Abstraction

Polyflow keeps datatype-specific logic out of the S2R operator by pushing it
into segments and R2R/R2S implementations.

The S2R operator only knows:

- how to create or update windows;
- how to route elements into state;
- when to report a segment.

The segment knows:

- how to store `I`;
- how to materialize its buffer as `R`;
- optionally, how to support removal and iteration.

R2R operators know:

- how to evaluate relational algebra over `R`.

R2S operators know:

- how to turn `R` into output stream elements.

This is why the same window operator can work over tuples, RDF graphs, property
graphs, or domain objects: the operator manipulates `Segment<I, R>` through a
`SingleBufferState<I, R>` or `MultiBufferState<I, R>`, while datatype semantics
live in the segment and downstream operators.

## Implementing a New Datatype

To add a new datatype, implement:

1. A stream element type `I`.
2. A relation/materialized type `R extends Iterable<?>`.
3. A `Segment<I, R>` or a `SegmentFactory<I, R>`.
4. R2R operators over `R`.
5. Optionally, an R2S operator from `R` to output elements.

For a simple datatype, `ListSegmentFactory<I, R>` is often enough:

```java
SegmentFactory<MyEvent, MyRelation> factory =
        new ListSegmentFactory<>(
                event -> MyRelation.from(event),
                MyRelation::merge,
                MyRelation.empty());
```

For a graph datatype, a custom segment may be better:

```java
class GraphSegment implements Segment<GraphObject, GraphOrPath> {
    public void add(GraphObject e) { ... }
    public GraphOrPath coalesce() { ... }
}
```

## Implementing a New Window Operator

Start by choosing the state model.

For a multi-buffer operator:

```java
class MyWindow<I, R extends Iterable<?>>
        implements StreamToRelationOperator<I, R> {

    private final MultiBufferState<I, R> state;

    void compute(I element, long ts) {
        // open/scope windows
        // for each matching window: state.get(w).add(element)
        // evict whole windows
    }
}
```

For a single-buffer operator:

```java
class MyWindow<I, R extends Iterable<?>>
        implements StreamToRelationOperator<I, R> {

    private final SingleBufferState<I, R> state;

    void compute(I element, long ts) {
        // update current window
        // state.append(element)
        // state.evict(cutoff) or state.clear()
    }
}
```

Do not try to make one operator silently support both models. A multi-buffer
operator and a single-buffer operator have different element-routing semantics.

## Design Rules

- A `Segment` is about materialization: `I -> R`.
- A `SingleBufferState` owns one current buffer.
- A `MultiBufferState` owns many window-indexed buffers.
- Multi-buffer operators replicate elements across matching windows.
- Single-buffer operators append each element at most once.
- Eviction in multi-buffer state is window-level.
- Eviction in single-buffer state is buffer-level.
- Datatype-specific storage belongs in segments, not in window operators.
- Legacy `Content<I, W, R>` should be treated as compatibility code.
