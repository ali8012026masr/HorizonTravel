# Horizon Travel (Android) - agent rules

Native Android app, Java + XML views, covering ALL four Horizon roles: TOURIST, TOUR_GUIDE, SERVICE_PROVIDER, ADMIN. One login, role read from `LoginResponse.role`, app routes to a role-specific shell (own bottom nav + screens). Backend contract is in `docs/API_CONTRACT.md`; build plan and prompts are in `docs/BUILD_PLAN.md`.

## Project facts
- Package/namespace/applicationId: `com.example.horizontravel`
- minSdk 24, targetSdk 37, compileSdk release(37), Java 11
- **Gradle is Kotlin DSL** (`build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`) even though the app code is Java. Do not convert it to Groovy. Do not add the Kotlin Android plugin or write any `.kt` source file — Kotlin DSL for Gradle scripts only, Java for all app code.
- Version catalog at `gradle/libs.versions.toml`. Use `.\gradlew.bat` on Windows.

## Hard rules
1. Java only for app code. XML layouts only. ViewBinding for every screen (no `findViewById`).
2. Gradle edits are Kotlin-DSL syntax (`implementation(libs.retrofit)`, not Groovy `implementation libs.retrofit`).
3. Dependencies: Retrofit 2 + Gson converter, OkHttp + logging-interceptor, Material Components, RecyclerView, SwipeRefreshLayout, ConstraintLayout, CardView, ViewPager2. Nothing else without asking. Versions go in `gradle/libs.versions.toml`.
4. Before using any library API, check the actual version in the catalog. Do not invent methods.
5. Every class referenced must exist. Every `R.id.*`/`R.string.*`/`R.layout.*`/`R.drawable.*`/`R.menu.*` referenced must exist. Every activity declared in `AndroidManifest.xml`.
6. All user-visible text in `res/values/strings.xml`. Colors in `colors.xml`, theme in `themes.xml`.
7. Network calls only through `ApiClient`/`ApiService`. Callbacks check `isAdded()`/`isFinishing()`/`isDestroyed()` before touching views.
8. Handle every failure path: no network, timeout, 4xx/5xx with server `message`, 401 (clear session -> login), empty lists, loading state.
9. The app supports 4 roles but each install is one logged-in user with one role. Never show another role's write actions. The backend does NOT enforce ownership or role on most endpoints (see API_CONTRACT.md section "Server-side route guards") — the app is the only safety net, so always source ids (touristId/guideId/providerId) from the active session, never from a text field the user can edit.
10. New guide/provider accounts are `PENDING_VERIFICATION` until an admin approves them. After login, if `status != ACTIVE` or (for guide/provider) `verificationStatus != APPROVED`, show an "Awaiting verification"/"Rejected: <reason>" screen instead of the normal shell, with Logout only.
11. Keep dates as `String` from the API. No `java.time`.
12. No image loading library. No remote images (backend has none).
13. After every phase run `.\gradlew.bat assembleDebug` and `.\gradlew.bat lint`. Fix all errors before the phase is done. Show the final build result line.
14. Do not rewrite files from earlier phases unless the current phase needs a specific change; then make a minimal edit.
15. Do not create placeholder or TODO screens except where a phase explicitly allows it.

## Package layout
```
com.example.horizontravel
  data/model          shared + per-role request/response POJOs (Gson)
  data/api             ApiService, ApiClient, AuthInterceptor, ApiCallback
  util                 SessionManager, CartManager, Formatters, Validators, RoleRouter
  ui/auth              LoginActivity, RegisterChooserActivity, RegisterTouristActivity,
                        RegisterGuideActivity, RegisterProviderActivity,
                        ForgotPasswordActivity, ResetPasswordActivity, PendingVerificationActivity
  ui/tourist/...       same screen set as the HorizonTourist app (home, slots, cart, checkout,
                        bookings, guides, groups, profile, ai) under its own package + its own shell
  ui/guide             GuideMainActivity (bottom nav: Dashboard, Availability, Bookings, Profile)
                        + availability create/edit, booking requests list/detail (accept/decline/
                        complete/payment-received), location broadcast toggle, profile edit, ratings view
  ui/provider          ProviderMainActivity (bottom nav: Dashboard, Slots, Bookings, Profile)
                        + slot create/edit/manage list, incoming bookings list/detail (confirm/
                        complete), payments view, commissions ledger, profile edit
  ui/admin             AdminMainActivity (bottom nav: Dashboard, Verifications, Users, Finance, Reports)
                        + pending registrations (approve/reject guide+provider), user list/status,
                        payments browser, commissions browser + settle, revenue/booking/commission reports
  ui/shared            NotificationsActivity, ChangePasswordActivity (reused by every role)
```

Role shells are separate Activities with separate bottom nav menus; do not try to share one BottomNavigationView across roles. Shared pieces (notifications, change password, Retrofit layer, models used by more than one role) live once and are reused.
