# MikiX

MikiX is an offline-first gym workout tracker built with Kotlin + Jetpack Compose + Material 3.

## How to run
1. Open project in Android Studio Iguana+.
2. Let Gradle sync.
3. Run `app` on API 26+ device/emulator.


## Run the app quickly
- Preferred command (from repo root):
  - `./scripts/run_debug.sh`
- Manual fallback:
  - `JAVA_HOME=/path/to/jdk17 ./gradlew --no-daemon :app:assembleDebug`
  - Install/run with `adb install -r app/build/outputs/apk/debug/app-debug.apk` then launch `com.mikix/.MainActivity`.

### Environment notes
- Use **JDK 17** for this project (newer JDKs like 25 can break Gradle Kotlin DSL parsing).
- Android Gradle Plugin artifacts are fetched from `google()`; if your environment blocks Google Maven, builds will fail until network/proxy access is fixed.

## Architecture overview
- **MVVM + Repository + UseCases/Domain calculators**
- **Room** for all core workout entities and analytics queries.
- **DataStore** for UI/preferences + workout autosave snapshot.
- **WorkManager** scaffold for rest timer notifications.
- **Hilt** dependency injection.
- **Compose** premium dark UI with animated cards/charts/micro-interactions.

## Feature checklist
- [x] Splash with rotating MikiX mark.
- [x] Onboarding slides.
- [x] Home command center sections.
- [x] Log flow (session card, set completion interactions).
- [x] Progress dashboard + strength index chart visuals.
- [x] Community challenges/feed mock.
- [x] Profile settings/integrations placeholders.
- [x] Exercise library with 60 seeded exercises.
- [x] Room entities + required aggregate queries.
- [x] Adaptive suggestion local logic.
- [x] CSV export utility.
- [x] Unit tests for volume, 1RM, and strength index.
- [x] Basic UI navigation smoke test.

## Cloud + Community (implemented foundation)
- Retrofit APIs added for auth, session sync, groups, feed, and challenges.
- WorkManager cloud sync worker (`CloudSyncWorker`) syncs sessions + community data when online.
- Profile screen now exposes cloud-sync toggle, login demo action, sync-now action, and Filament 3D feature flag controls.
- Community tab now renders cached backend data from Room when available.

## Future roadmap
- Phase 2: production API hardening (pagination, retries, refresh token rotation, websocket live feed).
- Phase 3: true AI routine generation and personalized recovery scoring.
- Filament-powered real-time 3D branding model (optional toggle by device capability).


## Binary-safe repository note
- `gradle/wrapper/gradle-wrapper.jar` is intentionally omitted to keep PRs binary-free in environments that reject binary patches.
- Regenerate it locally with `gradle wrapper` (using JDK 17) if you want to run `./gradlew` directly.


## Production hardening added
- Token refresh path via `/v1/auth/refresh` and `AuthSessionManager` token persistence/expiry management.
- Retry with exponential backoff for sync/community API calls.
- Pagination support for groups/challenges and cursor feed fetching.
- WebSocket live feed client scaffold for real-time community updates.
- AI Coach engine for routine generation + personalized recovery score.
- Filament realtime 3D X is performance-gated by device RAM capability and user toggle.
