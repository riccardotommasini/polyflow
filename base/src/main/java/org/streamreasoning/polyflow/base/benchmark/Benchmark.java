package org.streamreasoning.polyflow.base.benchmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

public final class Benchmark implements AutoCloseable {
    private final BufferedWriter out;

    public Benchmark(Path path) {
        BufferedWriter writer = null;
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            boolean writeHeader = Files.notExists(path) || Files.size(path) == 0;

            writer = Files.newBufferedWriter(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            if (writeHeader) {
                writer.write("operation,s2r_type,state_type,segment_type,element_ts,duration_ns\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.out = writer;
    }

    public void row(
            String operation,
            String s2rType,
            String stateType,
            String segmentType,
            long elementTs,
            long durationNs
    ) {
        if (out == null) {
            return;
        }
        try {
            out.write(operation);
            out.write(',');
            out.write(s2rType);
            out.write(',');
            out.write(stateType);
            out.write(',');
            out.write(segmentType);
            out.write(',');
            out.write(Long.toString(elementTs));
            out.write(',');
            out.write(Long.toString(durationNs));
            out.write('\n');
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void measure(
            String operation,
            String s2rType,
            String stateType,
            String segmentType,
        long elementTs,
        Runnable runnable
    ) {
        long start = System.nanoTime();
        try {
            runnable.run();
        } finally {
            row(operation, s2rType, stateType, segmentType, elementTs, System.nanoTime() - start);
        }
    }

    public <T> T measure(
            String operation,
            String s2rType,
            String stateType,
            String segmentType,
            long elementTs,
            Supplier<T> supplier
    ) {
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            row(operation, s2rType, stateType, segmentType, elementTs, System.nanoTime() - start);
        }
    }

    @Override
    public void close() {
        if (out == null) {
            return;
        }
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
