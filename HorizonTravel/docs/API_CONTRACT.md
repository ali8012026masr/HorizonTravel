# Horizon Backend API Contract (all roles)

Source: Spring Boot backend, `horizon.example.demo`, base path `/api`, port 58080.

## Connection
- Emulator base URL: `http://10.0.2.2:58080/api/`. Real device: `http://<PC-LAN-IP>:58080/api/`.
- Plain HTTP. Needs network security config for cleartext + `INTERNET` permission.
- Error body: `{timestamp, status, message, errors?: {field: msg}}`.
- Codes: 400 validation, 401 bad credentials/token, 403 forbidden, 404 not found, 409 conflict, 423 locked, 429 AI rate limit, 503 email failure.

## Roles
`TOURIST`, `TOUR_GUIDE`, `SERVICE_PROVIDER`, `ADMIN`. Stored as single string on `LoginResponse.role`. One login screen, one `LoginResponse`; app routes to a role-specific shell (bottom nav set) based on that string. A user has exactly one role — no switching.

## Server-side route guards (only these are enforced by the backend; everything else only requires "logged in" — the app MUST still enforce role-correct UI since the server does not)
```
/api/auth/**                                                 permitAll
GET /api/slots/search, /api/slots/{id}                       permitAll
GET /api/guide-availability/search, /guide-availability/guide/{id}  permitAll
PUT /api/guides/{id}/location                                TOUR_GUIDE only
GET /api/guides/{id}/location                                any authenticated
GET /api/guides, /api/guides/{id}                             permitAll
GET /api/ratings/**                                           permitAll
/api/ai/**                                                    TOURIST only
/api/admin/**                                                 ADMIN only
anything else                                                 authenticated (no role/ownership check)
```
Consequence: the app must never let a guide/provider reach tourist-only writes (booking creation, AI chat) or let a tourist reach provider/guide/admin write screens, purely by UI routing — the server will accept the call either way.

## Auth model (stateless JWT)
- `POST /auth/login` -> `accessToken` (15 min), `refreshToken` (1 year).
- Send `Authorization: Bearer <access>` + `x-refresh-token: <refresh>` on every authenticated call.
- Server may return a refreshed access token in response header `x-access-token` — always read and store it.
- `PUT /auth/change-password` is generic for every role (uses the caller's own id from the token).
- New guide/provider accounts start `PENDING_VERIFICATION` / verification `PENDING` and cannot be used fully until an admin approves them (`PUT /admin/guides/{id}/verify`, `PUT /admin/providers/{id}/verify`). Login still succeeds; the app must show an "awaiting verification" / "rejected: <reason>" state instead of the normal shell when `status != ACTIVE` or `verificationStatus != APPROVED`.

## Public endpoints (no token)
| Method | Path | Notes |
|---|---|---|
| POST | `/auth/register/tourist` | `{fullName, email, password, phone?, address?}` -> 201 Tourist |
| POST | `/auth/register/guide` | `{fullName, email, password, phone?, address?, nationalId?, bio?, languages[]?, location?, experienceYears?(>=0), defaultPrice?, negotiable?}` -> 201 Guide |
| POST | `/auth/register/provider` | `{fullName, email, password, phone?, address?, businessName, tradeLicenseNo?, nationalId?, commissionRate?, category}` -> 201 Provider |
| POST | `/auth/login` | `{email, password}` -> LoginResponse |
| POST | `/auth/forgot-password` | `{email}` -> `{message}` |
| POST | `/auth/reset-password` | `{token, newPassword}`(min 6) -> `{message}` |
| GET | `/slots/search` | query: category, origin, destination, locationName, dateFrom, dateTo, minPrice, maxPrice -> ServiceSlot[] |
| GET | `/slots/{id}` | ServiceSlot |
| GET | `/guide-availability/search` | query: dateFrom, dateTo, location -> GuideAvailability[] |
| GET | `/guide-availability/guide/{guideId}` | GuideAvailability[] |
| GET | `/guides` | query `available`(bool) -> Guide[] |
| GET | `/guides/{id}` | Guide |
| GET | `/ratings/guide/{guideId}` | GuideRating[] |
| GET | `/ratings/provider/{providerId}` | ServiceRating[] |

## Generic authenticated endpoints (all roles)
| Method | Path | Body -> Response |
|---|---|---|
| PUT | `/auth/change-password` | `{currentPassword, newPassword(min 6)}` -> `{message}` |
| GET | `/notifications/user/{userId}` | Notification[] |
| GET | `/notifications/user/{userId}/unread-count` | `{count}` |
| PUT | `/notifications/{id}/read` | Notification |

## TOURIST-specific endpoints
| Method | Path | Body -> Response |
|---|---|---|
| GET/PUT/DELETE | `/tourists/{id}` | Tourist |
| POST | `/bookings` | `{touristId, serviceSlotId}` -> 201 Booking |
| GET | `/bookings/tourist/{touristId}` | Booking[] |
| PUT | `/bookings/{id}/status` | `{status}` -> Booking |
| POST | `/payments` | `{bookingId?, guideBookingId?, amount, method}` -> 201 Payment |
| GET | `/payments/{id}` | Payment |
| GET | `/payments/booking/{bookingId}` | Payment[] |
| POST | `/guide-bookings` | `{touristId, tourGuideId, guideAvailabilityId?, scheduleDate, agreedPrice, isNegotiated}` -> 201 GuideBooking |
| GET | `/guide-bookings/tourist/{touristId}` | GuideBooking[] |
| POST | `/ratings/guide` | `{guideBookingId, score(1-5), comment?}` -> 201 GuideRating |
| POST | `/ratings/service` | `{bookingId, touristId, providerId, score(1-5), comment?}` -> 201 ServiceRating |
| POST | `/groups` | `{groupName, createdByTouristId}` -> 201 Group |
| POST | `/groups/join` | `{joinCode, touristId}` -> Group |
| GET | `/groups/{id}` | Group |
| GET | `/groups/tourist/{touristId}` | Group[] |
| GET | `/groups/{id}/members` | GroupMember[] |
| DELETE | `/groups/{id}/members/{touristId}` | 204 |
| POST | `/groups/{id}/booking` | `{bookingId}` -> Group |
| PUT | `/groups/{id}/members/{touristId}/paid` | GroupMember |
| GET | `/groups/{id}/messages` | GroupMessage[] |
| POST | `/groups/{id}/messages` | `{touristId, message}` -> 201 GroupMessage |
| GET | `/guides/{id}/location` | Location (only after ACCEPTED guide booking) |
| POST | `/ai/chat` | `{userMessage(<=1000), history[](<=12)}` -> `{replyText, actionType, actionPayloadJson}` (10/min limit) |

`actionType`: FILTER_SLOTS, RECOMMEND_BUNDLE, RECOMMEND_GROUP_BUNDLE, NONE. `actionPayloadJson` is a JSON string.

## TOUR_GUIDE-specific endpoints
| Method | Path | Body -> Response |
|---|---|---|
| GET | `/guides/{id}` | Guide |
| PUT | `/guides/{id}` | `{fullName?, phone?, address?, bio?, isAvailable?, languages[]?, location?, experienceYears?, defaultPrice?, negotiable?}` -> Guide (own profile; no server ownership check, app must only let the logged-in guide edit their own id) |
| DELETE | `/guides/{id}` | 204 |
| POST | `/guide-availability` | `{tourGuideId, date(future), startTime, endTime, location?, notes?}` -> 201 GuideAvailability |
| PUT | `/guide-availability/{id}` | `{date?, startTime?, endTime?, location?, notes?, status?}` -> GuideAvailability |
| DELETE | `/guide-availability/{id}` | 204 (cancel) |
| PUT | `/guides/{id}/location` | `{latitude, longitude}` -> Location (TOUR_GUIDE only, server-enforced) |
| GET | `/guide-bookings/guide/{guideId}` | GuideBooking[] (incoming requests) |
| PUT | `/guide-bookings/{id}/status` | `{status}` -> GuideBooking (ACCEPTED/DECLINED/COMPLETED) |
| PUT | `/guide-bookings/{id}/payment-received` | GuideBooking (mark cash payment received) |

## SERVICE_PROVIDER-specific endpoints
| Method | Path | Body -> Response |
|---|---|---|
| GET | `/providers/{id}` | Provider |
| PUT | `/providers/{id}` | `{fullName?, phone?, address?, businessName?, tradeLicenseNo?, category?, commissionRate?}` -> Provider |
| DELETE | `/providers/{id}` | 204 |
| GET | `/providers/{id}/commissions` | Commission[] |
| GET | `/providers/{id}/payments` | query `dateFrom?, dateTo?, status?` -> Payment[] |
| POST | `/slots` | `{providerId, category, origin?, destination?, locationName?, startDateTime(future), endDateTime(future), capacity(>0), price(>0)}` -> 201 ServiceSlot |
| PUT | `/slots/{id}` | `{origin?, destination?, locationName?, startDateTime?, endDateTime?, capacity?, price?, status?}` -> ServiceSlot |
| DELETE | `/slots/{id}` | 204 |
| GET | `/slots/provider/{providerId}` | ServiceSlot[] ("my slots") |
| PUT | `/bookings/{id}/status` | `{status}` -> Booking (provider confirms/completes incoming bookings — same endpoint tourist uses to cancel) |
| GET | `/payments/booking/{bookingId}` | Payment[] |

## ADMIN-specific endpoints (`/admin/**`, server-enforced ADMIN only)
| Method | Path | Body -> Response |
|---|---|---|
| GET | `/admin/pending-registrations` | `{providers: Provider[], guides: Guide[]}` |
| PUT | `/admin/providers/{id}/verify` | `{approved(bool), rejectionReason?(required if approved=false)}` -> Provider |
| PUT | `/admin/guides/{id}/verify` | `{approved(bool), rejectionReason?}` -> Guide |
| GET | `/admin/users` | query `role?, status?` -> AdminUser[] |
| PUT | `/admin/users/{id}/status` | `{status}` -> AdminUser |
| GET | `/admin/payments` | query `dateFrom?, dateTo?, status?, method?` -> Payment[] |
| GET | `/admin/commissions` | query `dateFrom?, dateTo?, settlementStatus?` -> Commission[] |
| PUT | `/admin/commissions/{id}/settle` | Commission |
| GET | `/admin/reports/revenue-summary` | query `dateFrom?, dateTo?` -> `{totalRevenue, totalCommission, paymentCount}` |
| GET | `/admin/reports/bookings-summary` | query `dateFrom?, dateTo?` -> `{totalBookings, pendingCount, confirmedCount, cancelledCount, completedCount}` |
| GET | `/admin/reports/commission-summary` | query `dateFrom?, dateTo?` -> `{totalCommission, pendingCommission, settledCommission}` |

Admin has no self-profile-edit endpoint.

## Response shapes
- **LoginResponse**: `{id, fullName, email, role, roleDetails, accessToken, refreshToken, expiresInSeconds}` (`roleDetails` is Tourist/Guide/Provider shaped per role; Admin has none of those extra fields)
- **Tourist**: `{id, fullName, email, phone, address, status, loyaltyPoints}`
- **Guide**: `{id, fullName, email, phone, address, status, nationalId, bio, ratingAvg, ratingCount, isAvailable, verificationStatus, rejectionReason, languages[], location, experienceYears, defaultPrice, negotiable}`
- **Provider**: `{id, fullName, email, phone, address, status, businessName, tradeLicenseNo, category, verificationStatus, rejectionReason, commissionRate, ratingAvg, ratingCount}`
- **AdminUser**: `{id, fullName, email, role, status, createdAt}`
- **ServiceSlot**: `{id, providerId, providerBusinessName, category, origin, destination, locationName, startDateTime, endDateTime, capacity, availableSeats, price, status, providerRatingAvg, providerRatingCount}`
- **GuideAvailability**: `{id, tourGuideId, tourGuideName, date, startTime, endTime, location, notes, status, tourGuideRatingAvg, tourGuideRatingCount}`
- **Booking**: `{id, touristId, serviceSlotId, bookingDate, status, totalAmount, paymentStatus}`
- **GuideBooking**: `{id, touristId, tourGuideId, guideAvailabilityId, scheduleDate, agreedPrice, isNegotiated, status, paymentReceived}`
- **Payment**: `{id, bookingId, guideBookingId, amount, method, status, transactionDate}`
- **Commission**: `{id, bookingId, providerName, serviceCategory, percentage, amount, calculatedDate, settlementStatus}`
- **GuideRating**: `{id, guideBookingId, score, comment, ratedDate}`
- **ServiceRating**: `{id, bookingId, touristId, providerId, score, comment, ratedDate}`
- **Group**: `{id, groupName, createdByTouristId, joinCode, bookingId, createdAt, members: GroupMember[]}`
- **GroupMember**: `{id, touristId, touristName, status, joinedAt, amountOwed, paid}`
- **GroupMessage**: `{id, senderId, senderName, message, sentAt}`
- **Location**: `{tourGuideId, latitude, longitude, updatedAt}`
- **Notification**: `{id, message, type, read, createdAt}`
- **RevenueSummary**: `{totalRevenue, totalCommission, paymentCount}`
- **BookingsSummary**: `{totalBookings, pendingCount, confirmedCount, cancelledCount, completedCount}`
- **CommissionSummary**: `{totalCommission, pendingCommission, settledCommission}`

## Enums (strings)
- UserStatus: ACTIVE, PENDING_VERIFICATION, SUSPENDED, REVOKED
- VerificationStatus: PENDING, APPROVED, REJECTED
- BookingStatus: PENDING, CONFIRMED, CANCELLED, COMPLETED
- PaymentStatus (booking.paymentStatus): UNPAID, PAID, REFUNDED
- PaymentTxStatus: PENDING, SUCCESS, FAILED, REFUNDED
- PaymentMethod: CARD, MOBILE_BANKING, CASH
- GuideBookingStatus: REQUESTED, ACCEPTED, DECLINED, COMPLETED
- ServiceCategory: BUS, MICROBUS, LAUNCH, TRAIN, AIRPLANE, SHIP, HOTEL, RESORT, CONVENTION_CENTER, BUFFET, AMUSEMENT_PARK
- SlotStatus: OPEN, FULL, CLOSED, CANCELLED
- AvailabilityStatus: OPEN, BOOKED, CANCELLED
- GroupMemberStatus: INVITED, JOINED, DECLINED
- SettlementStatus: PENDING, SETTLED

## Known unknowns (handle defensively)
1. Boolean wire names: `isAvailable`/`available`, `isNegotiated`/`negotiated`, `paid`, `read`/`isRead`. Use Gson `@SerializedName` + `alternate`.
2. Dates/times are ISO-8601 strings. Keep as `String`, no `java.time` (minSdk 24).
3. Money (BigDecimal) arrives as JSON numbers -> use `double`.
4. No image upload anywhere in the backend. No image fields exist. Do not add remote image loading.
5. Server does not enforce ownership on most writes (see route guard table). The app is the only thing stopping a guide from editing another guide's profile, etc. Always source ids from the logged-in session, never from free-text input.
