# AutoCloser

[AutoCloser](lib/src/main/java/com/tdder/autocloser/AutoCloser.java) is a class that registers multiple `AutoCloseable` resources and ensures they are closed reliably in reverse order (LIFO) when the scope ends.

It works particularly well with JUnit 5.11+'s `@AutoClose` annotation, making cleanup in tests more concise than try-with-resources. It can also be used in regular code with the standard `try (AutoCloser closer = …)` pattern.

[日本語版 README](README_ja.md)

## Features

- **LIFO Closing**: Resources are closed in reverse order of registration, like Go's defer and Java's try-with-resources
- **Thread-Safe**: Supports registration from multiple threads
- **Exception Handling**: All resources are closed even if exceptions occur, with proper suppressed exception handling

## Use Cases

### Simplifying JUnit Test Cleanup

In JUnit tests, ensuring proper LIFO cleanup of dependent resources forces you to write unwieldy nested try-finally blocks. `AutoCloser` simplifies this:

**Before (manual cleanup):**
```java
@Test
void test() {
    Group group = createGroup();
    try {
        User user1 = createUser();
        try {
            User user2 = createUser();
            try {
                group.addMembers(user1, user2);
                
                // Test with group and users...
                assertThat(group.getMembers()).hasSize(2);
            } finally {
                deleteUser(user2);
            }
        } finally {
            deleteUser(user1);
        }
    } finally {
        deleteGroup(group);
    }
}
```

**After (with AutoCloser):**
```java
@AutoClose  // JUnit 5.11+ feature
private final AutoCloser closer = new AutoCloser();

@Test
void test() {
    Group group = createGroup();
    closer.register(() -> deleteGroup(group));
    
    User user1 = createUser();
    closer.register(() -> deleteUser(user1));
    
    User user2 = createUser();
    closer.register(() -> deleteUser(user2));
    
    group.addMembers(user1, user2);
    
    // Test with group and users...
    assertThat(group.getMembers()).hasSize(2);
    
    // Automatically closed in LIFO order: user2, user1, group
}
```

## Installation

*Coming Soon* (Not yet published to Maven Central)

<!--
Add the dependency to your `build.gradle`:

```gradle
dependencies {
    testImplementation 'com.tdder:autocloser:1.0.0'
}
```
-->

## Usage

### Basic Usage

```java
try (AutoCloser closer = new AutoCloser()) {
    closer.register(resource1);
    closer.register(resource2);
    closer.register(resource3);
    // Resources closed in reverse order: resource3, resource2, resource1
}
```

### With JUnit's @AutoClose

```java
class MyTest {
    @AutoClose
    private final AutoCloser closer = new AutoCloser();
    
    @Test
    void test() {
        closer.register(() -> cleanup1());
        closer.register(() -> cleanup2());
        // Automatically closed after test
    }
}
```

## Requirements

- Java 17 or higher

## License

MIT License
