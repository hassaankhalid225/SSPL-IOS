# SSPL Mobile App — CLAUDE.md

> Read the root `../CLAUDE.md` first. This file adds app-specific rules.
> Web reference (read-only): `../sspl-frotend/`. Planning docs: `../docs/app-revamp/`.

## Stack
- Kotlin 2.3, Compose Multiplatform 1.9.x, Material 3, Navigation Compose 2.9
- Koin 4.1 (BOM), Ktor 3.3 (`Resources`, `ContentNegotiation`, `Logging`), kotlinx-serialization, kotlinx-datetime
- Coil 3 (network via ktor3), Peekaboo image picker, Compass geolocation, Firebase (push, crashlytics), Pusher (Android)
- Font: Poppins (`composeResources/font/`)

## Keep (reuse as-is, extend if needed)
| Area | Files |
|---|---|
| HTTP client | `core/HttpClientConfigurations.kt` (base URL from `utils/Constants.kt`, bearer token from `UserSession`) |
| Session | `session/UserSession.kt`, `storage/Storage.kt`, platform storages |
| Data layer | `core/apis/*`, `core/models/*`, `core/repositories/*`, `core/usecases/*` |
| DI | `di/*Modules.kt` — register every new repo/usecase/viewmodel here |
| Push | `core/push/*`, `MyFirebaseMessagingService.kt`, `IosPushHelper.kt` |
| Payments | `PaymentWebView` (expect/actual), `InitiatePaymentUseCase`, `/swich/initiate-payment`, `PaymentStatusScreen` logic |
| Attendance | `MarkAttendanceViaImageUseCase`, `MarkAttendanceViaLocationUseCase`, camera/permission managers |
| Session join | `JoinSessionUseCase`, `AutoJoinSessionUseCase`, `ScenarioRequest` |
| Banners | `BannerRequest`, `BannerRepository`, `GetBannersUseCase`, `Banner` model, the auto-scroll + pager behaviour |
| Guest login | `EMAIL_GUEST_USER`, `PASSWORD_GUEST_USER`, `PHONE_GUEST_USER` — **never change** |

## Replace (old UI — rebuild)
Everything in `ui/` (screens + components), `Screen.kt`, `MainScreen.kt`, `App.kt` nav wiring, `AppTopAppBar`, `AppBottomBar`, `HomeScreen` grid.
Delete when the replacement is live: `ui/ForumScreen.kt`, `ui/posts/*`, `ui/postcreation/*`, `ui/exhibition/*`, `ui/homescreen/FuckingViewModel.kt`, `utils/dummyPostList.kt`, and their DI/use-case registrations (Post*, Like/Dislike/Comment, Exhibition*), because Forum & Exhibition are **excluded modules**.

## New app architecture
```
com.sspl
  navigation/   AppNavGraph.kt, Routes.kt (type-safe @Serializable routes), BottomNav.kt, AppDrawer.kt
  designsystem/ theme/ (tokens) + components/ (SsplCard, SectionHeader, StatChip, InfoRow, StatusBadge,
                EmptyState, ErrorState, ShimmerList, PrimaryButton, AppSearchBar, FilterChips, BannerCarousel…)
  feature/<name>/  Screen.kt, ViewModel.kt, UiState.kt, components/
  core/ (unchanged layout: apis, models, repositories, usecases)
```
- Single-activity, one `NavHost`. Auth graph (splash → login/signup/otp/forgot) and Main graph (scaffold with bottom bar + modal drawer).
- Bottom bar (5): **Home · Events · Fellowship · My Portal · More** (More opens the drawer).
- Drawer = full catalogue of modules, grouped exactly like the web's `dashboardNav.js` + public nav (minus excluded). Header shows avatar, name, membership badge, roles.
- Role/guest gating mirrors web `dashboardUtils.js` (`getUserRoles`, `hasUserRole`, `hasActiveMembershipPlan`, `getMembershipLabel`). Port these into `core/auth/RoleUtils.kt`.
- Token refresh: web uses `POST /auth/refresh` with `refresh_token`. If the login response gives a refresh token, store it in `UserSession` and add a Ktor `HttpResponseValidator`/`Auth` plugin that refreshes on 401 once, then logs out.

## UI rules
- Follow `../docs/app-revamp/DESIGN_SYSTEM.md` strictly.
- Every list screen: pull-to-refresh, shimmer loading, empty state, error state with retry, search/filter where the web has it, pagination where API supports it.
- Every detail screen: hero header, key facts as chips/info rows, primary CTA pinned at bottom.
- Show **more data, compactly**: 2-column info grids, chips, expandable sections, tabs — never giant empty cards like the old 2×2 grid.

## Commands (from this folder)
```bash
./gradlew :shared:compileKotlinMetadata
./gradlew :androidApp:assembleDebug
```
