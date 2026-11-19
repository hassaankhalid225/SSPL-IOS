# FEATURE_MAP — Web → App (source of truth for scope)

Web paths are in `sspl-frotend/src/`. **Before implementing any row, the agent MUST open the web page + its service file and copy the exact logic** (payload keys, validation, role checks, status labels, empty/error copy).

Legend: 🆕 new in app · ♻️ exists in app (rebuild UI, keep logic) · ❌ excluded

**Base URL (confirmed): `https://backendssplportal.switch2itech.cloud`** — set in `utils/Constants.kt → BASE_URL`, used for ALL endpoints below (web's `VITE_EXTERNAL_API_URL` and `VITE_API_URL_BASE` both map to it).

---

## A. Auth & Account
| # | App screen | Web reference | Endpoints | Status |
|---|---|---|---|---|
| A1 | Splash (session check, guest auto-login) | — | `GET /auth/me` | ♻️ |
| A2 | Login (email/phone + password, "Continue as guest") | `pages/auth/Login.jsx`, `components/auth/*` | `POST /auth/login` | ♻️ |
| A3 | Sign up (multi-step: personal → professional (PMDC, designation, institution, city, country) → password) | `pages/auth/Signup.jsx`, `services/externalAuthApi.js` | `POST /auth/signup`, `POST /external/register` | ♻️ |
| A4 | OTP verify | `pages/OtpPage.jsx` | as web | ♻️ |
| A5 | Forgot / reset password | `pages/auth/ForgotPasswordPage.jsx`, `services/authApi.js` | `POST /auth/reset-password/init`, `POST /auth/reset-password` | ♻️ |
| A6 | Token refresh (silent) | `services/apiClient.js` | `POST /auth/refresh` | 🆕 |
| A7 | Profile view + edit (photo upload, education profile) | `userDashboard/ProfilePage.jsx`, `utils/educationProfile.js`, `services/userApi.js` | `GET /auth/me`, `PUT /profiles/me` (multipart when photo) | ♻️ |
| A8 | Settings (notifications toggle, terms, about app, logout) | — | — | ♻️ |
| A9 | Delete account | — | `POST /delete-account/init` | ♻️ |

## B. Home & Public info
| # | App screen | Web reference | Endpoints | Status |
|---|---|---|---|---|
| B1 | **Home**: banner carousel (unchanged logic) → greeting + membership badge → quick actions row → stats band → upcoming events (horizontal) → latest news → president message teaser → fellowship CTA → executive council strip | `pages/HomePage.jsx`, `components/homepage/*`, `data/homepage/*` | `GET /banners`, `GET /external/events?active=true`, `GET /society-news`, `GET /fellowship/stats/public` | ♻️ |
| B2 | About Us (mission/vision, pillars, president message) | `pages/About.jsx`, `data/homepage/about.ts, missionVision.ts, pillars.ts, president.ts` | static | ♻️ |
| B3 | Society History | `pages/HistoryPage.jsx` | static/archive | 🆕 |
| B4 | Executive Council / Cabinet | `data/homepage/council.ts, cabinet2026.ts`, `components/homepage/ExecutiveCouncil.tsx` | `GET /members` (CMS) or static | 🆕 |
| B5 | Elections list + detail | `pages/ElectionsPage.jsx`, `services/electionsApi.js` | `GET /elections/`, `GET /elections/{id}` | 🆕 |
| B6 | News / Notifications feed | `pages/NotificationsPage.jsx`, `components/notifications/*` | `GET /society-news` + app push list | ♻️ |
| B7 | FAQs (searchable accordion) | `pages/Faqs.jsx`, `data/homepage/faqs.ts` | static | 🆕 |
| B8 | Member Guide | `pages/MemberGuidePage.jsx`, `data/homepage/memberGuide.ts` | static | 🆕 |
| B9 | Contact Us (call / email / map / WhatsApp intents) | `pages/ContactPage.jsx`, `components/contact/Contact.jsx` | `GET /settings` | 🆕 |
| B10 | Terms & Conditions | `pages/TermsPage.jsx`, `data/homepage/terms.ts` | static | ♻️ |
| B11 | Downloads (category filter, search, open/download file) | `pages/DownloadsPage.jsx`, `services/downloadsPublicApi.js` | `GET /downloads?…`, `GET /downloads/{id}/file` | 🆕 |
| B12 | Verify Certificate (serial input + QR/deeplink) | `pages/VerifyCertificatePage.jsx` | `GET /certificates/verify/{serial}`, `GET /fellowship/certificate/verify/{serial}` | 🆕 |
| B13 | Members Directory (search, Life/Regular tabs) | `pages/MembersPage.jsx`, `components/comman/MembersDirectory.jsx, LifeMembers.jsx, RegularMembers.jsx` | as web | 🆕 |

## C. Events & Conferences
| # | App screen | Web reference | Endpoints | Status |
|---|---|---|---|---|
| C1 | Events list (tabs: All / Conferences / Workshops / Meetings; search; type filter) | `pages/UpcommingEventsPaage.jsx`, `userDashboard/EventsView.jsx`, `services/eventsApi.js` | `GET /external/events?type=&active=` | 🆕 |
| C2 | Event detail (hero, date/venue chips, roles & fees, schedule, speakers, register CTA) | `pages/EventDetail.jsx`, `components/events/*` | `GET /external/events/{id}`, `GET /external/events/{id}/check` | 🆕 |
| C3 | Event registration (role picker → payment) | `components/events/RegistrationModal.jsx` | `POST /external/events/{id}/register`, Swich payment | 🆕 |
| C4 | Annual Conferences list + detail | `pages/ConferencesPage.jsx`, `services/conferenceApi.js` | `GET /conferences`, `GET /conferences/{id}`, `GET /conferences/{id}/roles` | ♻️ |
| C5 | Conference registration + payment | app `ConferenceRegistrationScreen`, web `registerForConference` | `POST /conferences/{id}/register`, `/registrations/check`, `/registrations/{id}/payment`, `/swich/initiate-payment`, `/swich/verify-status` | ♻️ |
| C6 | Scientific programme → session detail | app `ui/conference/programs/*`, web `components/events/ConferenceSchedule.jsx` | from conference detail | ♻️ |
| C7 | Pre-event workshops list + detail | app `ui/conference/workshops/*`, web `/conferences/workshops` | from conference detail | ♻️ |
| C8 | Event Archive | `pages/EventArchivePage.jsx`, `services/archivePublicApi.js` | `GET /archive/public` | 🆕 |
| C9 | Payment status (success / failed / pending, retry) | `pages/PaymentStatusView.jsx`, `Success-payment.jsx`, `Failed-payment.jsx` | `POST /external/events/registrations/{id}/pay`, `/swich/verify-status` | ♻️ |

## D. My Portal (logged-in member workspace = web `/dashboard`)
| # | App screen | Web reference | Endpoints | Status |
|---|---|---|---|---|
| D1 | Overview (profile summary, membership card, stats, quick links, recent registrations, trainer portal link if role) | `userDashboard/OverviewPage.jsx`, `dashboardUtils.js` | `GET /auth/me`, `/membership/my`, `/external/events/my-registrations` | 🆕 |
| D2 | Membership (status, plan, expiry, apply / renew / upgrade to life, pay) | `userDashboard/MembershipPage.jsx`, `services/membershipApi.js` | `GET /membership/my`, `GET /membership/plan`, `POST /membership/register`, `POST /membership/upgrade-life` | 🆕 |
| D3 | Membership plans (public) + application wizard | `pages/MembershipPlansPage.jsx`, `pages/MembershipApplicationPage.jsx`, `components/MembershipPlanCard.jsx` | `GET /membership/plan`, `GET /membership/plan/{id}` | 🆕 |
| D4 | My Registrations (filter by status, entry pass/QR badge, retry payment) | `userDashboard/MyRegistrationsView.jsx`, `components/events/BadgeModal.jsx, ParticipationBadge.jsx`, app `MyRegistrationsScreen` | `GET /external/events/my-registrations`, `GET /conferences/{id}/registrations` | ♻️ |
| D5 | Event Portal (active conference: sessions, join code / QR, mark attendance GPS/selfie, session feedback, event feedback, certificates) | `userDashboard/ConferenceView.jsx`, `components/conference/*` | `/conferences/{id}/sessions/{sid}/attendees`, `/sessions/{sid}/feedback`, `/feedback-form`, `/feedback`, `/conferences/certificate/generate`, `/certificates/my`, `/scenarios/sessions/{code}/join|auto-join` | ♻️ |
| D6 | Submit Abstract (eligible event picker, form, file) | `userDashboard/AbstractSubmissionView.jsx`, `services/abstractApi.js` | `POST /abstractSubmission/create`, eligible events via `fetchAbstractEligibleEvents` | 🆕 |
| D7 | My Submissions (status chips, filters) | `userDashboard/MySubmissionsView.jsx` | `GET /abstractSubmission/?…` | 🆕 |
| D8 | Presentations (list + upload file) | `userDashboard/PresentationUploadView.jsx`, `services/presentationsApi.js` | `GET /presentations/my`, `POST /presentations/upload` | 🆕 |
| D9 | My Certificates (all, download/share, verify link) | `components/conference/CertificateDownload.jsx` | `GET /certificates/my` | 🆕 |

## E. Fellowship
| # | App screen | Web reference | Endpoints | Status |
|---|---|---|---|---|
| E1 | Fellowship hub (overview, public stats, open programmes, CTAs) | `pages/fellowship/FellowshipHubPage.jsx` | `GET /fellowship/programs/open`, `GET /fellowship/stats/public` | 🆕 |
| E2 | Training centres directory (search, city filter) | `FellowshipCentersPage.jsx`, `components/fellowship/TrainingCenterDirectory.jsx` | `GET /fellowship/centers/directory`, `GET /fellowship/centers/active` | 🆕 |
| E3 | Guidelines | `FellowshipGuidelinesPage.jsx`, `components/fellowship/FellowshipGuidelines.jsx` | static | 🆕 |
| E4 | Apply as Fellow (multi-step wizard, document upload) | `FellowshipFellowPage.jsx`, `fellowship/application/ApplicationWizard.jsx, programModel.js` | `POST /fellowship/fellows`, `POST /fellowship/upload-document` | 🆕 |
| E5 | Apply as Training Centre (wizard) | `FellowshipTrainingCenterPage.jsx`, `CenterApplicationWizard.jsx` | `POST /fellowship/training-centers` | 🆕 |
| E6 | Fellowship dashboard — **Fellow**: progress, cases log, attendance, assessments, research upload, final evaluation, certificate | `userDashboard/FellowshipDashboardView.jsx, FellowshipClinicalView.jsx, FellowshipCompletionView.jsx` | `/fellowship/dashboard/fellow`, `/dashboard/stats`, `/cases`, `/attendance`, `/assessments`, `/research`, `/final-evaluations`, `/enrollments/{id}/progress`, `/certificates` | 🆕 |
| E7 | Fellowship dashboard — **Trainer**: my fellows, review cases/attendance/research, assessments | same + `dashboardUtils.getTrainerPortalRedirectUrl` | `/fellowship/dashboard/trainer`, `/trainer/fellows`, `/cases/{id}/review`, `/attendance/{id}/verify`, `/research/{id}/review` | 🆕 |
| E8 | Fellowship feedback (part 1 / part 2) | `FellowshipFeedbackView.jsx`, `fellowshipFeedbackSchema.js` | `GET /fellowship/feedback/my`, `POST /feedback/part1`, `POST /feedback/part2` | 🆕 |

## F. System
| # | Item | Notes | Status |
|---|---|---|---|
| F1 | Push notifications + device token | keep `UpdateDeviceTokenUseCase`, Firebase, Pusher | ♻️ |
| F2 | Deep links | `/events/{id}`, `/verify-certificate/{serial}`, `/payment/status` | 🆕 |
| F3 | Guest mode | guest sees public modules; portal items show "Sign in to continue" sheet | ♻️ |
| F4 | Role gating | port `dashboardUtils.js` role logic | 🆕 |

## ❌ Excluded — do NOT build, remove from nav
| Web module | Web path | App action |
|---|---|---|
| Exhibition | `/exhibition`, `/dashboard/exhibition`, `ExhibitionPage.jsx`, `ActiveExhibitions.jsx` | delete `ui/exhibition/*`, `ExhibitionRequest/Repository` DI |
| Past Sessions | `/past-sessions`, `PastSessionsPage.jsx` | don't add |
| Public Learning Hub | `/learning-hub`, `/learning-hub/:slug`, `/learning-hub/ejournal`, `/learning-hub/newsletter` | don't add |
| Member Learning Hub | `/dashboard/learning-hub`, `LearningHubPortalView.jsx` | don't add |
| Discussion Forum | `/dashboard/forum`, `ForumView.jsx`, `forumApi.js` | delete `ui/posts/*`, `ui/postcreation/*`, `ForumScreen.kt`, Post use cases |
| Admin panel | `/admin/*` | not for app |
