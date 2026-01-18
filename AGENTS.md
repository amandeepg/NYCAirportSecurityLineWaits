## Technologies
- Android app built with Kotlin.
- UI uses Jetpack Compose and AndroidX libraries.
- Build system is Gradle (Kotlin DSL) with the Android Gradle Plugin.
- Dependency versions are managed with the refreshVersions plugin (`versions.properties`).

### Coding + architecture

- Kotlin first: prefer immutable data, sealed interfaces/classes for UI states and events, clear naming, and small focused functions.
- Follow repo patterns (MVVM/MVI, DI approach, module boundaries). Do not introduce new patterns unless necessary.
- Keep UI logic in UI; keep business logic in ViewModels/use-cases/repositories.

### Jetpack Compose
- Use unidirectional data flow and state hoisting.
- Avoid side effects during composition; use LaunchedEffect, DisposableEffect, etc. intentionally and keyed correctly.
- Prefer stable, immutable parameters; avoid recomposition traps (creating lambdas/collections on every recomposition unless memoized).
- Use rememberSaveable for user-entered state and navigation-related UI state when appropriate.
- Use Material 3 components and the existing theme/tokens. No hardcoded colors/typography; prefer resources and theme.
- Add Previews for new composables when helpful.

### Concurrency + data
- Use coroutines/Flow and structured concurrency (viewModelScope). Never use GlobalScope.
- Keep work off the main thread; inject dispatchers if the project does.
- Handle errors explicitly and model them in UI state; avoid silent failures.

## Build / Compile
- Use the Gradle wrapper from the project root to build the app.
- When making changes, just assemble the debug APK to test compilation, with the --quiet and --console=plain flag.

## Update Dependencies
- Refresh available updates with refreshVersions: `./gradlew refreshVersions`
- Review and adjust version entries in `versions.properties`.
