# Generic Implementations

This module contains some generic implementation of stream processors created with the polyflow APIs.

You can find them in the [base](src/main/java/org/streamreasoning/polyflow/base) module, which contains some implementations general enough to be used in multiple [examples](https://github.com/riccardotommasini/polyflow-examples/).

The hopping and sliding window microbenchmarks write CSV samples to
`polyflow.bench.csv` (or the operator's default path under `target/bench`).
They measure three operations:

- `insert`: append to the single shared buffer, or create/update the maintained
  per-window buffers for multi buffer.
- `evict`: remove expired elements or window buffers. Multi-buffer operators and
  single-buffer hopping also measure direct calls to `evict()`. Single-buffer
  hopping uses the shared application clock for this call; single-buffer sliding
  only resets its reported-window reference until `evict(timestamp)` is called.
- `extract`: the complete `materialize(timestamp)` call on the time-varying
  object returned by `operator.get()`, including segment retrieval and
  `Segment.coalesce()`.

```java
// Create this view once, then materialize whenever the benchmark requests content.
TimeVarying<R> view = operator.get();
operator.compute(element, timestamp); // Records insert.
view.materialize(timestamp);         // Records extract, including coalesce().
R result = view.get();               // Reads the already materialized result.
operator.evict(timestamp);           // Records evict.
```

Multi buffer retrieves an existing per-window segment during extraction. Single
buffer scans its shared segment, selects the window's elements, builds a new
segment, and then coalesces it. Both implementations include coalescing in the
extraction measurement; maintaining a segment does not make coalescing free.

Reporting-policy checks still run during `compute()` and can request window
content, including an earlier reconstruction for single buffer. They are outside
these three timers and do not emit an `extract` sample. Retrieval reconstructs
the single-buffer segment again rather than reusing that policy-check segment.
Direct `content(timestamp)`/`getContents(timestamp)` calls are not timed: use
the view's `materialize(timestamp)` to measure full extraction. Existing CSV
files are appended to, so use a fresh file when comparing the new operation names
and timing boundaries with older measurements.
