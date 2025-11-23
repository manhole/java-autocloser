# AutoCloser

AutoCloser は、複数の `AutoCloseable` を登録しておき、スコープ終了時に逆順で確実にクローズするクラスです。

特に JUnit 5.11+ の `@AutoClose` と組み合わせると、テストでのクリーンアップを try-with-resources より簡潔に記述できますが、通常コードでも `try (AutoCloser closer = …)` という形でそのまま利用できます。

[English README](README.md)

## 特徴

- **LIFO クローズ**: Go の defer や Java の try-with-resources と同様に、登録と逆順でクローズします
- **スレッドセーフ**: マルチスレッドから登録できます
- **例外処理**: クローズ中に例外が発生しても、全リソースを確実にクローズします

## ユースケース

### JUnit テストのクリーンアップを簡素化

JUnit テストでリソースを確実にクリーンアップしようとすると、煩雑な try-finally になってしまいます。AutoCloser を使うと、これをシンプルに記述できます：

**変更前（手動クリーンアップ）:**
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
                
                // グループとユーザーでテスト...
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

**変更後（AutoCloser を使用）:**
```java
@AutoClose  // JUnit 5.11+ の機能
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
    
    // グループとユーザーでテスト...
    assertThat(group.getMembers()).hasSize(2);
    
    // 自動的に LIFO 順でクローズされます: user2, user1, group
}
```

## インストール

*Coming Soon* (Maven Central への公開を計画中)

<!--
`build.gradle` に依存関係を追加します:

```gradle
dependencies {
    testImplementation 'com.tdder:autocloser:1.0.0'
}
```
-->

## 使い方

### 基本的な使い方

```java
try (AutoCloser closer = new AutoCloser()) {
    closer.register(resource1);
    closer.register(resource2);
    closer.register(resource3);
    // リソースは逆順でクローズされます: resource3, resource2, resource1
}
```

### JUnit の @AutoClose と組み合わせる

```java
class MyTest {
    @AutoClose
    private final AutoCloser closer = new AutoCloser();
    
    @Test
    void test() {
        closer.register(() -> cleanup1());
        closer.register(() -> cleanup2());
        // テスト後に自動的にクローズされます
    }
}
```

## 要件

- Java 17 以上

## ライセンス

MIT License
