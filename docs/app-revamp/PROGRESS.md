# PROGRESS — SSPL App Revamp

> **Rule:** after every finished task tick the box, and add one line to the Session Log (date · task id · files · build result).
> Status keys: `[ ]` todo · `[~]` in progress · `[x]` done · `[!]` blocked (write reason)
> Feature ids refer to `FEATURE_MAP.md`.

## Current status
- **Phase:** 4 — Events & Conferences
- **Last build:** `:androidApp:assembleDebug` SUCCESS (BUILD SUCCESSFUL in 3m 58s)
- **Blockers / open questions:** see bottom

---

## Phase 0 — Discovery & setup
- [x] 0.1 Read root + app CLAUDE.md, FEATURE_MAP, DESIGN_SYSTEM
- [x] 0.2 Confirm the project builds as-is (`:androidApp:assembleDebug`) and record result
- [x] 0.3 Set `BASE_URL = "https://backendssplportal.switch2itech.cloud"` in `utils/Constants.kt` (confirmed by owner), clean up old-IP cleartext config, verify `/banners` + login respond
- [x] 0.4 Run `web-logic-analyst` on login response to see if `refresh_token` is returned

## Phase 1 — Foundation
- [x] 1.1 `designsystem/theme`: SsplColors, Typography, Shapes, Spacing, fix `onPrimary`
- [x] 1.2 `designsystem/components` (all listed in DESIGN_SYSTEM §7)
- [x] 1.3 `core/auth/RoleUtils.kt` (port `dashboardUtils.js`)
- [x] 1.4 Token refresh (`/auth/refresh`) + 401 handling (A6)
- [x] 1.5 New navigation: `Routes.kt`, `AppNavGraph.kt`, auth graph, main graph
- [x] 1.6 Scaffold: `SsplTopBar`, `SsplBottomBar` (Home/Events/Fellowship/My Portal/More), `SsplDrawer` (grouped, guest lock)
- [x] 1.7 Remove excluded modules: Forum (`ForumScreen`, `ui/posts`, `ui/postcreation`, Post use cases/repo/DI), Exhibition (`ui/exhibition`, repo/DI), `FuckingViewModel.kt`, `dummyPostList.kt`
- [x] 1.8 Build green

## Phase 2 — Auth & Account
- [x] A1 Splash
- [x] A2 Login (+ guest)
- [x] A3 Sign up (multi-step)
- [x] A4 OTP
- [x] A5 Forgot / reset password
- [x] A7 Profile view + edit (photo)
- [x] A8 Settings
- [x] A9 Delete account

## Phase 3 — Home & Public info
- [x] B1 Home (banners unchanged + sections)
- [x] B2 About Us
- [x] B3 History
- [x] B4 Executive Council
- [x] B5 Elections
- [x] B6 News / Notifications
- [x] B7 FAQs
- [x] B8 Member Guide
- [x] B9 Contact Us
- [x] B10 Terms
- [x] B11 Downloads
- [x] B12 Verify Certificate
- [x] B13 Members Directory

## Phase 4 — Events & Conferences
- [x] C1 Events list
- [x] C2 Event detail
- [x] C3 Event registration + payment
- [x] C4 Annual conferences list + detail
- [x] C5 Conference registration + payment
- [x] C6 Scientific programme / session detail
- [x] C7 Workshops
- [x] C8 Event archive
- [x] C9 Payment status

## Phase 5 — My Portal
- [ ] D1 Overview
- [ ] D2 Membership
- [ ] D3 Membership plans + application
- [ ] D4 My registrations (+ badge/QR)
- [ ] D5 Event portal (sessions, join, attendance GPS/selfie, feedback, certificates)
- [ ] D6 Submit abstract
- [ ] D7 My submissions
- [ ] D8 Presentations
- [ ] D9 My certificates

## Phase 6 — Fellowship
- [ ] E1 Hub
- [ ] E2 Training centres
- [ ] E3 Guidelines
- [ ] E4 Apply as fellow
- [ ] E5 Apply as training centre
- [ ] E6 Fellow dashboard
- [ ] E7 Trainer dashboard
- [ ] E8 Fellowship feedback

## Phase 7 — System & polish
- [ ] F1 Push notifications + device token
- [ ] F2 Deep links
- [ ] F3 Guest mode audit
- [ ] F4 Role gating audit
- [ ] 7.1 Strings moved to resources
- [ ] 7.2 Accessibility pass
- [ ] 7.3 iOS compile check (`:shared:compileKotlinIosArm64` or Xcode build)
- [ ] 7.4 Full QA run (`qa-reviewer`) + exclusions grep
- [ ] 7.5 Release build `:androidApp:assembleRelease`

---

## Session Log
| Date | Task | Files touched | Build | Notes |
|---|---|---|---|---|
| 2026-11-19 | Setup | CLAUDE.md files, agents, docs/app-revamp/* | — | Planning docs created; no app code changed yet |
| 2026-11-19 | Phase 0 | Constants.kt, Info.plist | SUCCESS | Baseline build verified, BASE_URL updated to https://backendssplportal.switch2itech.cloud, auth/refresh verified |
| 2026-11-19 | Phase 1 (1.1-1.6) | SsplTheme.kt, BadgesAndChips.kt, Buttons.kt, CardsAndRows.kt, FeedbackAndStates.kt, InputFields.kt, BannerCarousel.kt, RoleUtils.kt, Routes.kt, SsplTopBar.kt, SsplBottomBar.kt, SsplDrawer.kt, MainScaffold.kt, User.kt, UserSession.kt, AuthRequest.kt, AuthRepository.kt | SUCCESS | Implemented full design system token/component library, role utilities, refresh token handling, and navigation scaffold |
| 2026-11-19 | Phase 1 (1.7-1.8) | Deleted Forum/Exhibition files, updated DI modules, cleaned HomeScreen/MainScreen/SignInScreen/MarkAttendanceButton | SUCCESS | Excluded modules removed cleanly, debug APK assembled successfully |
| 2026-11-19 | Phase 2 (A1-A9) | SplashScreen, SignInScreen, SignUpScreen, UserDetailScreen, OtpScreen, ForgotPasswordScreen, ProfileScreen, EditProfileScreen, SettingsScreen, DeleteAccountScreen | SUCCESS | Complete Auth & Account module revamp with guest support and modern design system components |
| 2026-11-20 | Phase 3 (B1-B13) | HomeScreen, HomeScreenViewModel, AboutScreen, HistoryScreen, ExecutiveCouncilScreen, ElectionsScreen, NotificationsScreen, NotificationsViewModel, FaqsScreen, MemberGuideScreen, ContactUsScreen, TermsScreen, DownloadsScreen, VerifyCertificateScreen, MembersDirectoryScreen, PublicRepository, PublicModels, MainScreen, Screen.kt, SsplTheme.kt, BadgesAndChips.kt, CardsAndRows.kt, FeedbackAndStates.kt | SUCCESS | Implemented full Home & Public Info suite with live CMS API endpoints, responsive cards, and MainScaffold integration |
| 2026-11-20 | Phase 4 (C1-C9) | EventModels.kt, EventRepository.kt, EventsListViewModel.kt, EventDetailViewModel.kt, EventArchiveViewModel.kt, PaymentStatusViewModel.kt, EventsListScreen.kt, EventDetailScreen.kt, EventRegistrationScreen.kt, ConferencesScreen.kt, WorkshopsScreenV2.kt, EventArchiveScreen.kt, PaymentStatusScreen.kt, ConferenceRegistrationScreen.kt, ScientificProgramsScreen.kt, Screen.kt, MainScreen.kt, Tokens.kt, Buttons.kt, CardsAndRows.kt, User.kt | SUCCESS | Complete Events & Conferences module revamp (C1-C9): unified events list with category & status tabs, event detail, registration flow with roles & fees, Swich payment integration, conferences, workshops, event archive, and payment status handling |
| 2026-11-20 | Crash Fix | SsplTheme.kt, App.kt, UserSession.kt, MainActivity.kt | SUCCESS | Resolved app startup crash: added fallback typography in SsplTheme, wrapped App in SsplTheme, guarded UserSession JSON decoding against schema differences, and protected Pusher/FCM init in MainActivity |

## Open questions for the user
1. ✅ RESOLVED (2026-11-19): base URL for all endpoints = `https://backendssplportal.switch2itech.cloud`.
2. ✅ RESOLVED: CMS endpoints (`/settings`, `/members`, `/society-news`) use the same base URL. If any 404s, log here and ask.
3. E-Journal & Newsletter sit under the excluded Learning Hub — confirm they are excluded too (current assumption: **yes, excluded**).
4. Trainer users: native trainer dashboard in app, or open the admin portal link like web does? (assumption: native for basics + "Open full portal" link)
