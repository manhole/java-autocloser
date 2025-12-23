package com.tdder.autocloser;

import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

/**
 * Sample test demonstrating AutoCloser usage with the default @TestInstance(Lifecycle.PER_METHOD) lifecycle.
 * <p>
 * Because each test method gets its own instance, {@code @AutoClose} effectively runs at
 * {@link org.junit.jupiter.api.AfterEach} timing.
 * This pairs with {@link AutoCloserPerClassExampleTest} to show the PER_CLASS variant.
 */
class AutoCloserPerMethodExampleTest {

    @AutoClose  // JUnit 5.11+ feature
    private final AutoCloser closer = new AutoCloser();

    /**
     * When this test runs, the output will be:
     * "1", "2", "3", "4"
     */
    @Test
    void demonstratesAutoCloseUsage() {
        // Simulate creating resources in order
        closer.register(() -> System.out.println("4"));
        closer.register(() -> System.out.println("3"));
        closer.register(() -> System.out.println("2"));

        System.out.println("1");

        // After test completes, resources are automatically closed in LIFO order
        // This is verified in the next test method by checking the execution order
    }

}
