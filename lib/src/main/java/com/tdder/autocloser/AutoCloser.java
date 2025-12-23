package com.tdder.autocloser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

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

    private final Deque<AutoCloseable> resources = new ArrayDeque<>();

    /**
     * Registers a resource to be closed.
     * <p>
     * <p>
     * @param resource the resource to manage, must not be null
     * @param <T>      the type of the resource
     * @return the registered resource
     * @throws NullPointerException  if the resource is null
     */
    public synchronized <T extends AutoCloseable> T register(final T resource) {
        Objects.requireNonNull(resource, "resource must not be null");
        resources.push(resource);
        return resource;
    }

    /**
     * Closes all registered resources in LIFO (Last-In-First-Out) order.
     * <p>
     * This method can be called multiple times; each call closes the resources registered since the previous call.
     * <p>
     * If any resource throws an exception during closing, the first exception is thrown after all resources have been attempted to close.
     * Subsequent exceptions are added as suppressed exceptions to the first one.
     */
    @Override
    public void close() throws Exception {
        final Deque<AutoCloseable> toClose;
        synchronized (this) {
            toClose = new ArrayDeque<>(resources);
            resources.clear();
        }

        Exception firstException = null;

        while (!toClose.isEmpty()) {
            final AutoCloseable resource = toClose.poll();
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

        if (firstException != null) {
            throw firstException;
        }
    }
}
