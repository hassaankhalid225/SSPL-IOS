# DESIGN_SYSTEM — SSPL App (new UI)

Goal: **clean, professional medical-society look**; lots of information visible but never cluttered. Same SSPL blue theme as today (app `theme/Colors.kt` ≈ web `index.css` tokens).

## 1. Colour tokens (keep the theme)
| Token | Value | Source / use |
|---|---|---|
| `primary` | `#0A528F` (web `#07538E`) | app current primary — buttons, active tab, links |
| `primaryDark` / `secondary` | `#053762` (web `#064371`) | top bars, headers, gradients end |
| `tertiary` | `#032746` | deep text on light blue, drawer header gradient end |
| `accent` | `#0095B8` | web accent/cyan — highlights, progress, chips selected |
| `primaryLight` | `#0876A8` | secondary buttons, icons |
| `surface` | `#FFFFFF` | cards |
| `background` | `#F6F9FC` | screen background (web `ssp-soft`) |
| `surfaceTint` / `boxColor` | `#EAF5FA` / `#EBF1F6` | info tiles, chip backgrounds |
| `border` | `#D8E5ED` | card/divider strokes |
| `heading` | `#19364D` | titles |
| `body` | `#374B5E` | body text |
| `muted` | `#617487` | captions, meta |
| `success` | `#0A520F` → use `#1E7B34` for text on white | paid / approved / present |
| `warning` | `#C27705` | pending |
| `error` | `#B3261E` | failed / rejected |

Implement as `SsplColors` + `MaterialTheme.colorScheme` mapping in `designsystem/theme/`. Fix the existing bug where `onPrimary = textColor` (should be white). Light theme only for v1; structure tokens so dark mode can be added later.

## 2. Typography (Poppins — already bundled)
| Style | Size / weight |
|---|---|
| displaySmall (home greeting) | 22sp SemiBold |
| titleLarge (screen title) | 18sp SemiBold |
| titleMedium (card title) | 15sp SemiBold |
| bodyLarge | 14sp Regular |
| bodyMedium | 13sp Regular |
| labelMedium (chips, meta) | 12sp Medium |
| labelSmall (badges, overlines, ALL CAPS) | 10.5sp SemiBold, letterSpacing 0.6 |

## 3. Spacing, shape, elevation
- 4-pt grid: 4, 8, 12, 16, 20, 24, 32. Screen horizontal padding **16dp**.
- Corner radius: cards 16dp, chips 50%, buttons 12dp, banners 16dp, bottom sheets 24dp top.
- Elevation: flat cards with 1dp `border` stroke + very soft shadow (2dp). No heavy shadows.

## 4. Layout patterns (dense but decent)
- **Section** = `SectionHeader(title, actionText="See all")` + content. 20dp between sections.
- **Info grid**: 2 columns of `InfoRow(icon, label, value)` — used in every detail screen to show many facts compactly.
- **Stat chips row**: horizontally scrollable `StatChip(value, label)`.
- **Horizontal carousels** for events, news, council members (card width 260dp / 140dp).
- **Tabs** (`ScrollableTabRow`) inside detail screens instead of very long pages (e.g. Event: Overview · Schedule · Speakers · Fees).
- **Expandable cards** for long text (guidelines, FAQ, abstracts).
- **Sticky bottom CTA bar** on detail/registration screens.
- **Status badges** everywhere status exists (Paid / Pending / Approved / Rejected / Active / Expired).
- Lists: `LazyColumn` with item spacing 10dp; card shows title, 2–3 meta chips, trailing status badge, chevron.

## 5. Navigation shell
- **Top app bar**: left = drawer/back, centre = title (or SSPL logo on Home), right = notifications bell with badge + avatar.
- **Bottom bar (5)**: Home · Events · Fellowship · My Portal · More(drawer).
- **Drawer (ModalNavigationDrawer)**: gradient header (`primary → tertiary`) with avatar, name, membership badge, role chips, "View profile". Then grouped items with group labels:
  - **Account**: Overview, Profile, Membership, My Certificates, Settings
  - **Events**: Browse Events, Annual Conferences, Workshops, My Registrations, Event Portal, Event Archive
  - **Scholarship**: Submit Abstract, My Submissions, Presentations
  - **Fellowship**: Overview, Training Centres, Guidelines, Apply as Fellow, Apply as Training Centre, Fellowship Dashboard
  - **Society**: About Us, History, Executive Council, Elections, Members Directory, Membership Plans, News
  - **Resources**: Downloads, Verify Certificate, Member Guide, FAQs, Contact Us, Terms
  - Footer: Logout / Sign in (guest), app version
  - Guest: portal items show lock icon → bottom sheet "Sign in to continue".
  - ❌ No Exhibition, Past Sessions, Learning Hub (public/member), Forum.

## 6. Banner carousel (must keep behaviour)
- Source `GET /banners`, filter `type == "SLIDER" || type == null`, URL = `BASE_URL` + (`/` if missing) + `image_url`.
- `HorizontalPager` with peek padding 20dp, page spacing 5dp, alpha 0.5→1 and scaleY 0.85→1 on offset, auto-advance every **4000ms**, loop to 0.
- Polish allowed: 16dp radius, 16:7 aspect with `ContentScale.Crop` fallback to `FillWidth`, small pill indicators below, shimmer while loading, hide section when empty.

## 7. Components to build (designsystem/components)
`SsplTopBar, SsplBottomBar, SsplDrawer, BannerCarousel, SectionHeader, SsplCard, EventCard, NewsCard, MemberCard, StatChip, InfoRow, InfoGrid, StatusBadge, RoleChip, MembershipBadge, PrimaryButton, SecondaryButton, TextButtonLink, SsplTextField, PasswordField, PhoneField(reuse CountryPicker), DropdownField, DatePickerField, FilePickerField, StepIndicator (wizards), SearchBar, FilterChipsRow, EmptyState, ErrorState(retry), ShimmerBox/ShimmerList, PullToRefreshBox, ConfirmDialog, SsplBottomSheet, GuestLockSheet, QrCodeView, AvatarImage, ExpandableText, StickyCtaBar`.

## 8. Motion & feedback
- Screen transitions: fade + slight slide (200–250ms). No flashy animations.
- Buttons show inline progress when submitting; disable double-submit (web uses `useSubmitLock`).
- Snackbar for success/error messages; map server errors like web `utils/apiError.js`.

## 9. Accessibility
- Min touch target 48dp, text contrast ≥ 4.5:1, contentDescription on all icons/images, support font scaling (no fixed-height text containers).
