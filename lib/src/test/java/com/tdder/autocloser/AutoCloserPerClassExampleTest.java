package com.tdder.autocloser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * Sample test demonstrating AutoCloser usage with @TestInstance(Lifecycle.PER_CLASS).
 * <p>
 * Because the instance is shared across test methods, this example closes resources in {@link AfterEach}
 * instead of using {@code @AutoClose}.
 * The two test methods are intentionally similar and labeled A/B since their execution order is not guaranteed.
 */
@TestInstance(Lifecycle.PER_CLASS)
class AutoCloserPerClassExampleTest {

    private final AutoCloser closer = new AutoCloser();

    @AfterEach
    void tearDown() throws Exception {
        closer.close();
    }

    @Test
    void perClassExampleA() {
        closer.register(() -> System.out.println("close-2 (perClassExampleA)"));
        closer.register(() -> System.out.println("close-1 (perClassExampleA)"));
        System.out.println("test-body (perClassExampleA)");
    }

    @Test
    void perClassExampleB() {
        closer.register(() -> System.out.println("close-4 (perClassExampleB)"));
        closer.register(() -> System.out.println("close-3 (perClassExampleB)"));
        System.out.println("test-body (perClassExampleB)");
    }
}
