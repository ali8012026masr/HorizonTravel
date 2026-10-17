# Horizon Travel Android App - Build Plan and Antigravity CLI Prompts (all roles)

## 0. How to use this file
1. Start the backend (PostgreSQL `horizon` DB, Spring Boot on 58080, `DataSeeder` test data). Confirm `http://localhost:58080/api/guides` returns JSON.
2. Open Antigravity CLI in `C:\Users\MT\OneDrive\Documents\JAVA\1295200\HorizonTravel`.
3. Paste prompts one phase at a time, in order. Do not move to the next phase until the current one builds clean and you tested it on the emulator.
4. Commit to git after each green phase.
5. If the CLI needs `AGENTS.md` under another filename (e.g. `GEMINI.md`), copy it; I could not verify which file Antigravity CLI auto-loads.
6. Gradle here is **Kotlin DSL** (`.kts`). Keep saying so in prompts — it is the one thing most likely to trip a model that defaults to Groovy snippets.

## 1. Architecture decisions
| Topic | Decision | Reason |
|---|---|---|
| Language/UI | Java, XML, ViewBinding | Requested |
| Gradle | Kotlin DSL scripts, Java app code | Matches the project as created; no Kotlin source files |
| Networking | Retrofit 2 + Gson + OkHttp | Standard |
| Structure | One LoginActivity; after login, route by `role` to one of 4 separate "shell" Activities, each with its own BottomNavigationView and Fragments | Keeps each role's nav simple, avoids one giant shared nav with visibility toggling bugs |
| State | No ViewModel/LiveData/Room/Compose/Hilt/Coroutines. Fragments call Retrofit directly. SessionManager via SharedPreferences | Fewer generated-code failure points |
| Dates | Strings, no `java.time` | minSdk 24 |
| Images | None | Backend has no image support |
| Map | `geo:` intent to Google Maps for guide location | No API key needed |
| Verification gate | After login, PENDING_VERIFICATION/REJECTED/SUSPENDED/REVOKED users see a status screen, not the shell | Matches backend workflow |

## 2. Role shells and screens
### Shared (all roles)
- LoginActivity, RegisterChooserActivity (pick Tourist / Guide / Provider — Admin has no self-registration), PendingVerificationActivity, ForgotPasswordActivity, ResetPasswordActivity, ChangePasswordActivity, NotificationsActivity.

### TOURIST shell (bottom nav: Home, Explore, Guides, Bookings, Profile)
Same feature set as the standalone HorizonTourist app: browse/search slots, cart, checkout, bookings + cancel + rate, guides browse/request/pay/rate/live-location, groups + chat, profile, AI concierge.

### TOUR_GUIDE shell (bottom nav: Dashboard, Availability, Requests, Profile)
- Dashboard: rating summary, upcoming accepted bookings, GPS broadcast toggle (calls PUT location on a timer while ON).
- Availability: list own slots (GET guide-availability/guide/{id}), create (POST), edit (PUT), cancel (DELETE).
- Requests: incoming guide-bookings (GET guide-bookings/guide/{id}) with Accept/Decline (PUT status) and, once ACCEPTED and paid in cash, "Mark payment received" (PUT payment-received); view tourist name/price/date.
- Ratings tab (or section under Profile): GET ratings/guide/{id}, read-only.
- Profile: view/edit own Guide fields (PUT guides/{id}), availability toggle isAvailable, change password, logout.

### SERVICE_PROVIDER shell (bottom nav: Dashboard, Slots, Bookings, Profile)
- Dashboard: rating summary, open slot count, pending booking count.
- Slots: "My slots" list (GET slots/provider/{id}), create (POST), edit (PUT), cancel (DELETE), status chip.
- Bookings: incoming bookings for own slots — list via GET bookings filtered client-side by slots owned (no single provider-bookings endpoint exists; fetch own slot ids then show bookings whose serviceSlotId matches, after calling an available list endpoint — see Phase 5 note on this gap), confirm/complete via PUT bookings/{id}/status, view payments (GET payments/booking/{id}).
- Commissions ledger: GET providers/{id}/commissions, read-only list with settlement status chip.
- Profile: view/edit own Provider fields, change password, logout.

### ADMIN shell (bottom nav: Verifications, Users, Finance, Reports)
- Verifications: GET admin/pending-registrations, two sections (guides/providers), approve/reject dialogs (PUT verify with rejectionReason when rejecting).
- Users: GET admin/users with role/status filters, tap to change status (PUT users/{id}/status) with a confirm dialog for SUSPENDED/REVOKED.
- Finance: tabs/segments for Payments (GET admin/payments with date/status/method filters) and Commissions (GET admin/commissions with date/settlement filters + Settle action PUT commissions/{id}/settle).
- Reports: 3 cards calling revenue-summary, bookings-summary, commission-summary with a shared date-range picker.
- No self-profile-edit screen for Admin (backend has none); Profile menu item only has Change password and Logout.

## 3. Phases
| # | Phase | Scope | Done when |
|---|---|---|---|
| 1 | Foundation | Gradle (kts), theme, manifest, network layer, ALL models (every role), session, RoleRouter | Debug build succeeds, splash routes correctly (stub targets ok) |
| 2 | Shared auth | Login, role-aware register chooser + 3 register forms, forgot/reset, pending-verification screen, logout | All 4 roles can register (except admin), log in, see correct next screen |
| 3 | Shared infra | NotificationsActivity, ChangePasswordActivity, global 401 handling, BaseActivity/BaseFragment | Works from any role shell |
| 4 | Tourist shell | Full tourist feature set (ports the HorizonTourist app's logic into ui/tourist/*) | Full booking/guide/group/AI flow works |
| 5 | Guide shell | Dashboard, availability CRUD, requests accept/decline/payment-received, GPS toggle, profile | Full guide flow works |
| 6 | Provider shell | Dashboard, slot CRUD, incoming bookings confirm/complete, commissions, profile | Full provider flow works |
| 7 | Admin shell | Verifications, users, finance, reports | Full admin flow works |
| 8 | Hardening audit | Build, lint, rule audit, README | Clean release build, documented |

## 4. Prompts

### Prompt 0 - Orientation (paste first, every new session)
```
You are building the Horizon Travel Android app in this project (Java app code, Kotlin-DSL Gradle scripts, package com.example.horizontravel). This single app serves 4 roles: TOURIST, TOUR_GUIDE, SERVICE_PROVIDER, ADMIN.
Read fully before doing anything: AGENTS.md, docs/API_CONTRACT.md, docs/BUILD_PLAN.md, build.gradle.kts, app/build.gradle.kts, settings.gradle.kts, gradle/libs.versions.toml, app/src/main/AndroidManifest.xml, and everything under app/src/main/res/values.
Reply with: (1) a 10-line summary of the rules you must follow, (2) confirmation that you will write Gradle changes in Kotlin DSL syntax and Java-only app code, (3) anything unclear or conflicting in AGENTS.md or the API contract. Do not write or change code in this step.
```

### Prompt 1 - Foundation
```
PHASE 1: Foundation. Follow AGENTS.md strictly. Gradle files are Kotlin DSL - use `implementation(libs.x)` syntax, version catalog entries as [libraries] tables in TOML, not Groovy syntax anywhere.

Gradle (gradle/libs.versions.toml, app/build.gradle.kts, build.gradle.kts):
- Add Retrofit 2.11.0, converter-gson 2.11.0, OkHttp 4.12.0, logging-interceptor 4.12.0, recyclerview, swiperefreshlayout, cardview, viewpager2. Keep existing entries (do not touch AGP/appcompat/material/activityKtx/constraintlayout versions unless a real conflict forces it - tell me if so).
- app/build.gradle.kts: buildFeatures { viewBinding = true; buildConfig = true }; buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:58080/api/\"") for debug, a placeholder HTTPS URL for release.
- Do not add the Kotlin Android plugin and do not create any .kt file.

Manifest/resources:
- INTERNET + ACCESS_NETWORK_STATE permissions, network_security_config.xml allowing cleartext to 10.0.2.2/localhost/private LAN for debug, usesCleartextTraffic wired.
- Material3 theme (deep blue primary / teal secondary, travel feel), colors.xml, strings.xml with app_name "Horizon Travel".
- Keep a minimal compilable placeholder MainActivity for now (replaced in Phase 2/4-7).

Code (package com.example.horizontravel):
- data/model: ALL POJOs needed across every role from docs/API_CONTRACT.md: LoginRequest, LoginResponse, RegisterTouristRequest, RegisterGuideRequest, RegisterProviderRequest, Tourist, Guide, Provider, AdminUser, UpdateTouristRequest, UpdateGuideRequest, UpdateProviderRequest, ServiceSlot, CreateServiceSlotRequest, UpdateServiceSlotRequest, Booking, BookingRequest, StatusRequest, Payment, PaymentRequest, GuideAvailability, CreateGuideAvailabilityRequest, UpdateGuideAvailabilityRequest, GuideBooking, GuideBookingRequest, UpdateGuideBookingStatusRequest, GuideRating, GuideRatingRequest, ServiceRating, ServiceRatingRequest, Group, GroupMember, GroupMessage, GroupCreateRequest, JoinGroupRequest, GroupBookingRequest, MessageRequest, Location, UpdateLocationRequest, Notification, Commission, RevenueSummary, BookingsSummary, CommissionSummary, VerifyRequest, UpdateUserStatusRequest, CountResponse, MessageResponse, ChangePasswordRequest, ForgotPasswordRequest, ResetPasswordRequest, AiChatRequest, AiChatMessage, AiChatResponse, ErrorResponse. Use Gson alternate-name annotations per the "Known unknowns" section of the contract. Money as double, dates as String. Put every enum's string values as constants in a Constants class (BookingStatus, PaymentStatus, PaymentTxStatus, PaymentMethod, GuideBookingStatus, ServiceCategory, SlotStatus, AvailabilityStatus, GroupMemberStatus, UserStatus, VerificationStatus, SettlementStatus).
- data/api/ApiService: one Retrofit method per endpoint in the contract (every role), Call<T>, Call<Void> for 204s.
- data/api/ApiClient, AuthInterceptor, ApiCallback<T>: same design as described in AGENTS.md rule 7-8 - add Authorization + x-refresh-token headers except on /auth/** public endpoints, capture x-access-token from responses into SessionManager, global 401 listener, friendly error parsing from ErrorResponse including 423 locked.
- util/SessionManager: stores accessToken, refreshToken, userId, fullName, email, role (String), plus role-specific extra id if needed (it's the same as userId here). save(LoginResponse), updateAccessToken(), clear(), isLoggedIn(), getRole().
- util/RoleRouter: one method `Intent shellIntentFor(Context, String role)` returning the Intent for TouristMainActivity/GuideMainActivity/ProviderMainActivity/AdminMainActivity (create minimal compilable stub Activities for all four now, to be filled in Phases 4-7).
- util/Formatters, util/Validators: same helpers as described in AGENTS.md (money, date/time, enum capitalization, email/password validation).
- Application subclass HorizonApp (init SessionManager + ApiClient), declared in manifest.
- ui/SplashActivity (launcher): if not logged in -> LoginActivity (stub for now is fine, created fully in Phase 2); if logged in -> check status/verification fields saved at login and route to PendingVerificationActivity stub or RoleRouter target.

Verify: .\gradlew.bat assembleDebug then .\gradlew.bat lint, fix every error. Report BUILD SUCCESSFUL and the file tree created. Do not start Phase 2.
```

### Prompt 2 - Shared auth
```
PHASE 2: Shared authentication, all roles. Follow AGENTS.md. Only touch Phase 1 files for minimal necessary fixes; list anything you touch.

- LoginActivity: email, password (show/hide), login button with progress state, links "Register" and "Forgot password". On success, use SessionManager.save, then: if roleDetails status is not ACTIVE, or (role is TOUR_GUIDE/SERVICE_PROVIDER and verificationStatus is not APPROVED), go to PendingVerificationActivity; else go to RoleRouter.shellIntentFor(role) with FLAG_ACTIVITY_CLEAR_TASK. Map 401/423/other errors to friendly messages.
- RegisterChooserActivity: three cards - "I'm a Tourist", "I'm a Tour Guide", "I'm a Service Provider" (no Admin option; add a one-line note in the UI that admin accounts are created by the system). Each opens its register screen.
- RegisterTouristActivity: fullName, email, phone?, address?, password, confirm -> POST register/tourist.
- RegisterGuideActivity: fullName, email, phone?, address?, nationalId?, bio?, languages (comma-separated chip input is fine), location?, experienceYears? (numeric, >=0), defaultPrice? (numeric), negotiable (switch), password, confirm -> POST register/guide.
- RegisterProviderActivity: fullName, email, phone?, address?, businessName (required), tradeLicenseNo?, nationalId?, commissionRate? (numeric), category (dropdown of ServiceCategory), password, confirm -> POST register/provider.
- All three: client-side validation per the contract, 409 -> "Email already registered" on the email field, per-field server errors surfaced, on success show "Registered - awaiting admin verification" (guide/provider) or go straight to Login (tourist) with the email prefilled.
- PendingVerificationActivity: shows a friendly status based on the saved roleDetails (PENDING_VERIFICATION -> "Your account is awaiting verification", REJECTED -> "Your application was rejected: <reason>", SUSPENDED/REVOKED -> matching messages), a "Refresh status" button that re-fetches the own profile (GET tourists|guides|providers/{id} by role) and re-checks, and Logout.
- ForgotPasswordActivity / ResetPasswordActivity: same behavior as described for the tourist-only app (email -> message; token+newPassword+confirm -> success -> Login), generic for all roles.
- BaseActivity with the global 401 listener (register in onStart/unregister in onStop) -> Toast "Session expired" -> Login with FLAG_ACTIVITY_CLEAR_TASK.
- Keyboard handling (adjustResize, scrollable forms), double-tap protection on all submit buttons.

Verify with assembleDebug + lint, fix everything. Give a manual test checklist covering all 3 registrable roles plus the pending-verification screen. Do not start Phase 3.
```

### Prompt 3 - Shared infra
```
PHASE 3: Shared infrastructure usable from every role shell. Follow AGENTS.md.

- NotificationsActivity: GET notifications/user/{userId}, unread bold + dot, tap marks read (PUT), pull-to-refresh, empty state, menu "Mark all read". Launchable from any role shell via a shared toolbar bell icon with an unread-count badge (GET unread-count on resume).
- ChangePasswordActivity: current/new/confirm -> PUT auth/change-password; on success clear session (backend invalidates old tokens) and route to Login with "Password changed, please log in again".
- BaseFragment: showLoading/showError/showEmpty/toast/isSafe() helpers, used by every role's fragments from Phase 4 onward.
- Confirm the global 401 handling works from inside a role shell (not just auth screens): force an invalid token manually and confirm it returns to Login without crashing from at least one role shell stub.

Verify with assembleDebug + lint. Report. Do not start Phase 4.
```

### Prompt 4 - Tourist shell
```
PHASE 4: Full TOURIST shell. Follow AGENTS.md. This ports the already-proven tourist feature set into this multi-role app under ui/tourist/*, with its own TouristMainActivity (bottom nav: Home, Explore, Guides, Bookings, Profile) replacing the Phase-1 stub.

Build, completely and working end-to-end against docs/API_CONTRACT.md:
1. TouristMainActivity + HomeFragment (greeting, loyalty points, unread badge, quick actions, cart icon with count from a new util/CartManager).
2. SlotsFragment (search + category/price/date filters, RecyclerView cards, pull-to-refresh) + SlotDetailActivity (details + provider ratings + add-to-cart/book-now).
3. CartActivity + CheckoutActivity (sequential booking+payment per item, partial-failure handling, no double-booking on retry) + BookingConfirmationActivity.
4. BookingsFragment (services + guide-bookings segmented view) + BookingDetailActivity (cancel, pay now, rate-when-completed) + RateServiceActivity.
5. GuidesFragment (browse/search/sort) + GuideDetailActivity + RequestGuideActivity + MyGuideBookingsActivity (pay, view location, rate) + RateGuideActivity + GuideLocationActivity (geo: intent, 15s auto-refresh while visible).
6. GroupsActivity (list/create/join) + CreateGroupActivity + JoinGroupActivity + GroupDetailActivity (members, mark paid, leave, link booking) + GroupChatActivity (polling every 5s while visible).
7. ProfileFragment (view, edit, change password link, notifications link, logout, delete account) + EditProfileActivity.
8. AiConciergeActivity (chat, 1000-char cap, last-12-turn history, actionType handling: FILTER_SLOTS opens Explore with filters applied, RECOMMEND_BUNDLE/RECOMMEND_GROUP_BUNDLE show mini cards + deep links, 429/403/400 handled as error bubbles).

Reuse shared NotificationsActivity/ChangePasswordActivity/CartManager from earlier phases; do not duplicate them under ui/tourist. Handle every loading/empty/error state. No placeholders - every screen listed must be fully wired.

Verify with assembleDebug + lint, fix everything. Give a full manual test checklist for one tourist account doing a complete journey (register -> browse -> book -> pay -> cancel one, rate one, request a guide, pay the guide, rate the guide, create a group, chat, use AI concierge). Do not start Phase 5.
```

### Prompt 5 - Guide shell
```
PHASE 5: Full TOUR_GUIDE shell. Follow AGENTS.md. Replace the Phase-1 stub GuideMainActivity with bottom nav: Dashboard, Availability, Requests, Profile.

- DashboardFragment: own rating summary (ratingAvg/ratingCount from GET guides/{id}), list of upcoming ACCEPTED bookings (GET guide-bookings/guide/{id} filtered client-side to ACCEPTED and future scheduleDate), a GPS broadcast Switch: when ON, start a foreground-safe repeating Handler (NOT a background Service, to keep this simple and avoid foreground-service permission complexity) that calls PUT guides/{id}/location every 30s using the device's last known location via FusedLocationProviderClient... if that adds a new dependency, instead use android.location.LocationManager's last known location (no extra dependency) and request ACCESS_FINE_LOCATION/ACCESS_COARSE_LOCATION at runtime with a clear rationale dialog; stop the Handler in onPause and when the Switch is OFF; persist the Switch state in SharedPreferences, defaulting OFF.
- AvailabilityFragment: list own slots (GET guide-availability/guide/{id}), status chips, FAB -> CreateAvailabilityActivity (date not in past via MaterialDatePicker, start/end time via MaterialTimePicker, location?, notes?) POST, tap an item -> EditAvailabilityActivity (PUT) with Cancel (DELETE) action; block edits on BOOKED/CANCELLED entries except cancel.
- RequestsFragment: incoming guide-bookings (GET guide-bookings/guide/{id}), segmented by status; REQUESTED rows get Accept/Decline buttons (PUT status with confirm dialog on Decline); ACCEPTED rows show tourist info, price, scheduleDate and, if payment method was CASH and paymentReceived is false, a "Mark payment received" button (PUT payment-received); COMPLETED/DECLINED are read-only.
- GuideProfileFragment: view/edit own fields (fullName, phone, address, bio, languages, location, experienceYears, defaultPrice, negotiable) via PUT guides/{id}; isAvailable toggle; link to shared ChangePasswordActivity and NotificationsActivity; Logout.
- Ratings section (tab inside Profile or Dashboard, your call, but it must exist): GET ratings/guide/{id}, read-only list with stars and comments.

Reuse shared infra from earlier phases. Handle all loading/empty/error states, including location-permission-denied (disable the broadcast switch and explain why).

Verify with assembleDebug + lint, fix everything. Give a manual test checklist (post availability, receive and accept a request from a tourist account, mark cash payment received, toggle GPS, edit profile). Do not start Phase 6.
```

### Prompt 6 - Provider shell
```
PHASE 6: Full SERVICE_PROVIDER shell. Follow AGENTS.md. Replace the Phase-1 stub ProviderMainActivity with bottom nav: Dashboard, Slots, Bookings, Profile.

Known backend gap: there is no single "bookings for my slots" endpoint. Implement BookingsFragment by: GET slots/provider/{providerId} to get my slot ids, then for each OPEN/FULL/CLOSED slot call a per-slot bookings lookup; the contract has no GET /bookings/slot/{slotId} either, so instead do this: cache my slot ids locally and, since there's no listing-by-slot endpoint, show bookings the provider has visibility into only through the Payments path (GET payments/booking/{bookingId} requires a booking id you don't have in bulk). STOP and tell me this gap explicitly instead of guessing a non-existent endpoint; implement the Dashboard and Slots tabs fully now, implement the Bookings tab as: a manual "Look up booking by ID" tool (enter a booking id, GET is not directly exposed per-booking either - check /bookings/{id} existence in the backend source before building this; if no suitable endpoint exists after checking, build the Bookings tab around GET payments/booking/{bookingId} entry-by-id only, and clearly mark the limitation in the UI with a short explanatory text), and ask me whether to add a provider-bookings backend endpoint before finishing this tab for real.

- DashboardFragment: rating summary (GET providers/{id}), open slot count and pending-booking placeholder metric (0 until the gap above is resolved), quick links.
- SlotsFragment: "My slots" (GET slots/provider/{id}), status chips, FAB -> CreateSlotActivity (category dropdown, origin/destination/locationName, start/end date-time via MaterialDatePicker+TimePicker combined to ISO string, capacity, price) POST; tap -> EditSlotActivity (PUT, including status change) with Cancel (DELETE).
- BookingsFragment: implement per the gap-handling instruction above; do not invent an endpoint.
- ProviderProfileFragment: view/edit own fields (fullName, phone, address, businessName, tradeLicenseNo, category, commissionRate) via PUT providers/{id}; commissions ledger section (GET providers/{id}/commissions, read-only, settlement status chip); shared ChangePasswordActivity/NotificationsActivity links; Logout.

Verify with assembleDebug + lint, fix everything. Give a manual test checklist for slot create/edit/cancel and profile edit; flag the bookings-tab limitation clearly in your report. Do not start Phase 7.
```

### Prompt 7 - Admin shell
```
PHASE 7: Full ADMIN shell. Follow AGENTS.md. Replace the Phase-1 stub AdminMainActivity with bottom nav: Verifications, Users, Finance, Reports.

- VerificationsFragment: GET admin/pending-registrations, two sections (Guides, Providers) as RecyclerViews, each row has Approve and Reject; Approve -> PUT verify {approved:true}; Reject opens a dialog requiring a reason -> PUT verify {approved:false, rejectionReason}. Refresh the list after every action. Empty state when both lists are empty.
- UsersFragment: GET admin/users with role filter (chips: All/Tourist/Guide/Provider/Admin) and status filter (chips: All/Active/Pending/Suspended/Revoked), search-by-name/email client-side, tap a row -> status-change bottom sheet (radio buttons for UserStatus) -> PUT users/{id}/status, confirm dialog before Suspend/Revoke.
- FinanceFragment: two segments, Payments and Commissions, a shared date-range picker (MaterialDatePicker range mode) applied to whichever segment is active.
  - Payments segment: GET admin/payments with dateFrom/dateTo + status + method filter chips, list rows (id, amount, method, status, transactionDate, linked booking/guideBooking id).
  - Commissions segment: GET admin/commissions with dateFrom/dateTo + settlementStatus filter, rows (provider name, category, percentage, amount, calculatedDate, status chip), a "Settle" button on PENDING rows -> PUT commissions/{id}/settle with confirm dialog.
- ReportsFragment: one shared date-range picker, 3 cards that call revenue-summary, bookings-summary, commission-summary and render the numbers (plain TextViews/stat cards; no charting library - do not add one).
- Admin Profile menu (toolbar overflow or a 5th "More" nav item - your call, keep bottom nav at 4 items and put this behind the existing Finance/Reports screens' toolbar overflow instead if simpler): Change password (shared), Logout only (no self-profile-edit endpoint exists for Admin).

Verify with assembleDebug + lint, fix everything. Give a manual test checklist (approve a guide, reject a provider with a reason, suspend a user, filter payments by date, settle a commission, view all 3 reports). Do not start Phase 8.
```

### Prompt 8 - Hardening audit
```
PHASE 8: Hardening and audit across all 4 roles. Do not add features.

1. .\gradlew.bat clean assembleDebug lint - fix every Error-severity lint issue and real HardcodedText/UnusedResources/MissingTranslation warnings.
2. Audit every file against AGENTS.md; produce a table: rule -> pass/fail -> fixes made.
3. Grep-verify: every Activity is manifest-declared; every R.* reference exists; no findViewById; no java.time; no leftover TODO/"Coming soon" outside the one documented provider-bookings gap from Phase 6 (confirm whether that gap was ever resolved - if I added a backend endpoint for it, wire it now).
4. Cross-role check: confirm a tourist account can never see guide/provider/admin write screens and vice versa (RoleRouter is the only path to any shell; there is no manual URL/deeplink into another role's Activities without a session of that role).
5. Null-safety and lifecycle pass: Retrofit callback null checks, isAdded()/isFinishing() guards, Handlers stopped in onPause/onStop (location broadcast, group chat polling), no static Activity/View references.
6. Rotation: verify no crash on any screen; lists reload or survive rotation.
7. android:allowBackup="false", add data_extraction_rules.
8. README.md: prerequisites, how to run the backend, BASE_URL for emulator vs real device (PC must listen on 0.0.0.0, Windows Firewall must allow port 58080), test accounts per role (tell me to fill these from DataSeeder or create one of each role manually, including getting an admin account since there's no self-registration for it), architecture overview (shell-per-role), known limitations (no image support, polling instead of push, provider-bookings gap if still open, no offline mode).
9. .\gradlew.bat assembleRelease (unsigned ok) to confirm no resource/build errors.
Report final results with command output summary lines.
```

## 5. Repair prompt (any build failure)
```
The build failed. Do not rewrite the project. Run .\gradlew.bat assembleDebug --stacktrace yourself, read the FIRST error only, open the exact file/line it names, and fix the root cause with the smallest edit. Rebuild, repeat until BUILD SUCCESSFUL. Remember Gradle scripts here are Kotlin DSL - do not paste Groovy syntax as a fix. Do not suppress warnings or delete features to make it compile. Report each error and its fix.
```

## 6. Bug-fix prompt
```
Bug: <screen, steps, expected, actual>. Logcat: <relevant lines>.
Trace the path: fragment/activity -> ApiService call -> model class -> layout. Check field names in docs/API_CONTRACT.md against the Gson model first (most null-value bugs are a @SerializedName mismatch, especially the isX/x boolean fields). Minimal fix, rebuild, tell me how to verify.
```

## 7. Checks Antigravity cannot do for you
- **Admin account**: there is no self-registration for ADMIN. You need at least one seeded admin (check `DataSeeder.java`) or insert one directly in Postgres to test the Admin shell.
- **Provider "my bookings" gap**: confirmed by the backend mapping - there is no endpoint returning bookings for a provider's slots. Decide whether to add `GET /api/bookings/provider/{providerId}` on the backend before Phase 6, or accept the limited by-id lookup the plan falls back to.
- **Ownership holes**: almost every write endpoint only checks "authenticated", not role or ownership (e.g. any logged-in user can call `PUT /guides/{id}` for someone else's id). The Android app enforces this client-side only. Worth hardening server-side before any real deployment.
- **Location permission**: guide GPS broadcast needs a runtime location permission prompt; test denial and re-grant on a real device/emulator, not just happy path.
- **Real device testing**: same LAN/firewall caveat as the tourist app.
- **Wire-name check**: confirm boolean field names (`isAvailable` vs `available`, `isNegotiated` vs `negotiated`, `read` vs `isRead`, `paid`) against a live response before Phase 2, same as the tourist app.
