package com.tdder.autocloser;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility class that ensures multiple {@link AutoCloseable} resources are closed reliably.
 * <p>
 * This class implements {@link AutoCloseable}, so it can be used in a try-with-resources statement or with JUnit's {@code @AutoClose} annotation.
 * When {@link #close()} is called, it closes all registered resources in LIFO order, similar to try-with-resources.
 * <p>
 * If any resource throws an exception during closing, the first exception is thrown after all resources have been attempted to close.
 * Subsequent exceptions are added as suppressed exceptions to the first one.
 * <p>
 * <strong>This class is thread-safe.</strong> Multiple threads can safely register resources concurrently.
 */
public class AutoCloser implements AutoCloseable {

    private final Deque<AutoCloseable> resources = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Registers a resource to be closed.
     * <p>
     * <strong>Note:</strong> This method must not be called after {@link #close()} has been called.
     *
     * @param resource the resource to manage, must not be null
     * @param <T>      the type of the resource
     * @return the registered resource
     * @throws NullPointerException  if the resource is null
     * @throws IllegalStateException if this AutoCloser has already been closed
     */
    public <T extends AutoCloseable> T register(final T resource) {
        Objects.requireNonNull(resource, "resource must not be null");
        if (closed.get()) {
            throw new IllegalStateException("AutoCloser is already closed");
        }
        resources.push(resource);
        return resource;
    }

    @Override
    public void close() throws Exception {
        // This method is expected to be called from a single thread (e.g., try-with-resources or test framework).
        // However, we use compareAndSet to ensure idempotency and safety just in case.
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        Exception firstException = null;

        while (!resources.isEmpty()) {
            final AutoCloseable resource = resources.poll(); // Use poll() to be safe
            if (resource != null) {
                try {
                    resource.close();
                } catch (final Exception e) {
                    if (firstException == null) {
                        firstException = e;
                    } else {
                        firstException.addSuppressed(e);
                    }
                }
            }
        }

        if (firstException != null) {
            throw firstException;
        }
    }
}
