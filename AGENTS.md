# NowInWiki

Unofficial, read-only Wikipedia / MediaWiki client adapted from
[Now in Android](https://github.com/android/nowinandroid). Not affiliated with Wikimedia.

## Architecture

This project follows Google’s official Android architecture guidance. It is a reactive,
single-activity app that uses:

- **UI:** Jetpack Compose, Material 3, adaptive layouts
- **State:** UDF with Coroutines / Flow and ViewModels
- **DI:** Hilt
- **Navigation:** Navigation 3
- **Data:** Repository pattern with Room, DataStore, Retrofit / OkHttp
- **Content:** Wikipedia / MediaWiki public APIs (no self-hosted news backend)

## Modules

The main Android app lives in `app/`. Feature modules live in `feature/` and shared code in `core/`.

## Commands to Build & Test

Product flavors: `demo` and `prod`. Build types: `debug` and `release`.

- Build: `./gradlew assemble{Variant}` (typically `assembleDemoDebug`)
- Format: `./gradlew spotlessApply`
- Unit tests: `./gradlew {variant}Test`
- Screenshot tests: `./gradlew verifyRoborazziDemoDebug`

### Instrumented tests

- Gradle-managed devices: `./gradlew pixel6api31aospDebugAndroidTest`

## Continuous integration

- Workflows live in `.github/workflows/*.yaml` (inherited from upstream; may need local tuning).

## Version control

- Hosted at https://github.com/Analoggpixel/NowInWiki
