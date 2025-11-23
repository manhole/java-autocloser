package com.tdder.autocloser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutoCloserTest {

    @Test
    void closesResourcesInReverseOrder() throws Exception {
        final List<String> closed = new ArrayList<>();
        try (final AutoCloser closer = new AutoCloser()) {
            closer.register(() -> closed.add("1"));
            closer.register(() -> closed.add("2"));
            closer.register(() -> closed.add("3"));
        }

        assertThat(closed, contains("3", "2", "1"));
    }

    @Test
    void handlesExceptionsAndSuppressedExceptions() {
        final RuntimeException firstException = new RuntimeException();
        final RuntimeException secondException = new RuntimeException();

        final Exception thrown = assertThrows(Exception.class, () -> {
            try (final AutoCloser closer = new AutoCloser()) {
                closer.register(() -> {
                    throw firstException;
                });
                closer.register(() -> {
                    throw secondException;
                });
            }
        });

        // LIFO order: second registered is closed first
        assertThat(thrown, sameInstance(secondException));
        assertThat(thrown.getSuppressed(), arrayWithSize(1));
        assertThat(thrown.getSuppressed()[0], sameInstance(firstException));
    }

    @Test
    void throwsNpeWhenRegisteringNull() throws Exception {
        try (final AutoCloser closer = new AutoCloser()) {
            assertThrows(NullPointerException.class, () -> closer.register(null));
        }
    }

    @Test
    void returnsRegisteredResource() throws Exception {
        try (final AutoCloser closer = new AutoCloser()) {
            final AutoCloseable resource = () -> {};
            final AutoCloseable returned = closer.register(resource);
            assertThat(returned, sameInstance(resource));
        }
    }

    @Test
    void handlesConcurrentRegistration() throws Exception {
        final int threadCount = 10;
        final int resourcesPerTask = 100;
        final List<String> closed = new ArrayList<>();

        try (final AutoCloser closer = new AutoCloser()) {
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            final List<Callable<Void>> tasks = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                final int taskId = i;
                tasks.add(() -> {
                    for (int j = 0; j < resourcesPerTask; j++) {
                        final int resourceNum = j;
                        closer.register(() -> {
                            synchronized (closed) {
                                closed.add(taskId + "-" + resourceNum);
                            }
                        });
                    }
                    return null;
                });
            }

            executor.invokeAll(tasks);
            executor.shutdown();
        }

        // Verify all resources were registered and closed
        assertThat(closed, hasSize(threadCount * resourcesPerTask));
    }

    @Test
    void throwsExceptionWhenRegisteringAfterClose() throws Exception {
        final AutoCloser closer = new AutoCloser();
        closer.close();

        assertThrows(IllegalStateException.class, () -> {
            closer.register(() -> {
            });
        });
    }

    @Test
    void closeIsIdempotent() throws Exception {
        final List<String> closed = new ArrayList<>();
        final AutoCloser closer = new AutoCloser();
        closer.register(() -> closed.add("resource"));

        closer.close();
        assertThat(closed, hasSize(1));

        // Second close should do nothing
        closer.close();
        assertThat(closed, hasSize(1));
    }
}
