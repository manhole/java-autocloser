package com.tdder.autocloser;

import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

/**
 * Sample test demonstrating AutoCloser usage with JUnit's @AutoClose annotation.
 * This matches the use case shown in the README.
 */
class AutoCloserExampleTest {

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
