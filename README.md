# MikiX

MikiX is an offline-first gym workout tracker built with Kotlin + Jetpack Compose + Material 3.

## How to run
1. Open project in Android Studio Iguana+.
2. Let Gradle sync.
3. Run `app` on API 26+ device/emulator.

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

## Future roadmap
- Phase 2: backend auth/cloud sync/group APIs.
- Phase 3: true AI routine generation and personalized recovery scoring.
- Filament-powered real-time 3D branding model (optional toggle by device capability).
