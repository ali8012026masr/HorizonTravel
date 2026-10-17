# Horizon Travel (Android App)

A native Android client application built in Java for the **Horizon Travel** platform, supporting four distinct roles within a unified APK: **Tourist**, **Tour Guide**, **Service Provider**, and **Admin**.

Each role is routed upon authentication into its own dedicated shell with bottom navigation, role-appropriate permissions, and full lifecycle handling.

---

## 1. Prerequisites

- **Java Development Kit (JDK)**: JDK 17 (or Java 11 minimum).
- **Android SDK**:
  - `minSdk`: 24 (Android 7.0 Nougat)
  - `compileSdk`: 37
  - `targetSdk`: 37
- **Gradle**: Gradle 8+ with Kotlin DSL (`build.gradle.kts`).
- **Android Studio**: Ladybug / Iguana or later recommended, or Gradle command-line tools.
- **Backend API**: The Horizon Travel Spring Boot backend (`horizon.example.demo`) running on port `58080`.

---

## 2. Running the Spring Boot Backend

1. Ensure PostgreSQL is running and the database `horizontravel` is configured.
2. In the backend directory:
   ```bash
   ./mvnw spring-boot:run
   ```
   Or run the application through your Java IDE.
3. The server starts on port `58080` (API context path: `/api`).
4. To test backend health:
   ```bash
   curl http://localhost:58080/api/actuator/health
   ```

---

## 3. Configuring `BASE_URL` (Emulator vs. Physical Device)

The API base URL is configured in `app/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:58080/api/\"")
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://api.horizontravel.example.com/api/\"")
    }
}
```

### A. Android Emulator (Default)
The Android Emulator uses `10.0.2.2` as an alias to the host machine's loopback interface (`127.0.0.1`).
- Default debug URL: `http://10.0.2.2:58080/api/` (no changes required).

### B. Real Physical Android Device
When running on a physical Android device connected via Wi-Fi:
1. Find your development machine's local LAN IP:
   - On Windows: Run `ipconfig` (e.g. `192.168.1.100`).
2. Update `buildConfigField` in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:58080/api/\"")
   ```
3. **Bind to all interfaces**: Ensure your Spring Boot backend binds to `0.0.0.0` (in `application.properties`: `server.address=0.0.0.0`).
4. **Windows Firewall Rule**: Allow inbound TCP traffic on port `58080`:
   ```powershell
   New-NetFirewallRule -DisplayName "Horizon Backend 58080" -Direction Inbound -LocalPort 58080 -Protocol TCP -Action Allow
   ```

---

## 4. Test Accounts & Role Setup

The platform uses role-based authentication. Token, role, and user IDs are stored securely in `SessionManager`.

### Role Account Types:
1. **TOURIST**:
   - Self-registration available via the app registration screen (`RegisterTouristActivity`).
   - Status defaults to `ACTIVE` upon registration.
2. **TOUR_GUIDE**:
   - Self-registration available via `RegisterGuideActivity`.
   - Initial verification status is `PENDING_VERIFICATION`. Must be approved by an Admin before accessing the guide dashboard.
3. **SERVICE_PROVIDER**:
   - Self-registration available via `RegisterProviderActivity`.
   - Initial verification status is `PENDING_VERIFICATION`. Must be approved by an Admin before accessing provider features.
4. **ADMIN**:
   - **Crucial Note**: There is **no public self-registration** for `ADMIN` accounts.
   - An Admin account must be created via the backend's `DataSeeder.java` or inserted directly into the PostgreSQL `users` table:
     ```sql
     INSERT INTO users (email, password_hash, full_name, role, status, created_at)
     VALUES ('admin@horizon.com', '$2a$10$...', 'System Administrator', 'ADMIN', 'ACTIVE', NOW());
     ```

### Seeded Credentials (from `DataSeeder`):
| Role | Email | Password | Status / Note |
|---|---|---|---|
| **Admin** | `admin@horizon.com` | `admin123` | Active admin account |
| **Tourist** | `tourist@example.com` | `tourist123` | Verified tourist |
| **Guide** | `guide@example.com` | `guide123` | Approved tour guide |
| **Provider** | `provider@example.com` | `provider123` | Approved service provider |

---

## 5. Architectural Overview

### Single-APK, Multi-Role Architecture
- **Unified Login (`LoginActivity`)**: Reads `LoginResponse.role` and `LoginResponse.verificationStatus`.
- **Role Verification & Pending States**: If a guide or provider is `PENDING_VERIFICATION` or `REJECTED`, they are routed to `PendingVerificationActivity` with logout and refresh actions.
- **`RoleRouter` Dispatch**:
  - `Constants.Role.TOURIST` $\to$ `TouristMainActivity`
  - `Constants.Role.TOUR_GUIDE` $\to$ `GuideMainActivity`
  - `Constants.Role.SERVICE_PROVIDER` $\to$ `ProviderMainActivity`
  - `Constants.Role.ADMIN` $\to$ `AdminMainActivity`
- **Defense-in-depth Role Guards**: Each shell Activity and write screen verifies the active session role at startup and prevents cross-role execution.

### Tech Stack & Hardening
- **Java 11 + XML Views**: 100% Java source code.
- **ViewBinding**: Strict ViewBinding across all 40 Activities, Fragments, and custom ViewHolders (zero `findViewById` across the entire codebase).
- **Date Handling**: All dates are managed as ISO `String` representations formatted with custom formatters (zero `java.time` imports for legacy Android compatibility).
- **Networking**: Retrofit 2 + Gson + OkHttp logging interceptor.
- **Lifecycle Guards**: All async network callbacks check `isFinishing()` / `isDestroyed()` / `isSafe()` before modifying views.
- **No Background Leaks**: Polling handlers (`GroupChatActivity`, `GuideLocationActivity`) and GPS broadcast handlers (`GuideDashboardFragment`) stop strictly in `onPause()` and `onDestroy()`.
- **Zero Static Activity/View Leaks**: No static Activity or Context references in memory.
- **Backup & Security**: `android:allowBackup="false"`, with explicit `data_extraction_rules.xml` and `backup_rules.xml` exclusion policies.

---

## 6. Known Limitations & Design Decisions

1. **Remote Image Support**:
   - The backend does not serve remote image URLs or binary assets. The app uses vector icons, Material tint palettes, and category badges for visuals without requiring third-party image loaders.
2. **Polling vs. WebSockets / Push Notifications**:
   - Group chat messaging uses client-side polling (5-second interval) while active on screen.
   - Tour guide real-time location broadcast uses 15-second intervals.
   - All polling loops strictly stop in `onPause()` / `onDestroy()` to prevent background battery drain.
3. **Service Provider Bookings Gap**:
   - The backend API contract currently does not provide a `GET /api/bookings/provider/{providerId}` endpoint.
   - The provider shell handles this by providing a Booking ID search lookup and displaying an informative empty/gap notice rather than inventing fictitious endpoints.
4. **Offline Mode**:
   - The app requires an active network connection to authenticate and communicate with the backend. Clean error states, swipe-to-refresh, and retry options are presented when offline.

---

## 7. Build & Verification Commands

```powershell
# Clean and assemble debug APK
.\gradlew.bat clean assembleDebug

# Run static analysis and lint
.\gradlew.bat lint

# Assemble release APK (R8 minification and resource shrinking verification)
.\gradlew.bat assembleRelease
```
