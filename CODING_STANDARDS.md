# Coding Standards

## Java
- **Final Modifier**: Use `final` for variables and parameters whenever possible.
- **Lambdas**: Omit braces `{}` in lambda expressions when possible.
- **Assertions**: Use Hamcrest `assertThat` for assertions instead of JUnit's `assertEquals`, etc.

## Dependencies
- **Version Management**: Define library versions in `gradle/libs.versions.toml`.
