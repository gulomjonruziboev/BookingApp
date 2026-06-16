# Technical Mission: Buron Android App (User / Client Side)

**Document version:** 1.0  
**Date:** June 15, 2026  
**Project:** Buron — To'yxona (wedding hall) booking platform  
**Scope:** Android mobile application — **client (Mijoz) features only**  
**Reference systems:**
- Frontend: `toyxona-frontend` (Next.js 15 web app)
- Backend: `toyxona-backend` (Node.js / Express REST API)

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Context](#2-business-context)
3. [Scope Definition](#3-scope-definition)
4. [Target Technology Stack](#4-target-technology-stack)
5. [System Architecture](#5-system-architecture)
6. [Backend API Contract](#6-backend-api-contract)
7. [Data Models](#7-data-models)
8. [Screen-by-Screen Specification](#8-screen-by-screen-specification)
9. [Shared UI Components](#9-shared-ui-components)
10. [Validation Rules](#10-validation-rules)
11. [Authentication & Session Management](#11-authentication--session-management)
12. [Business Logic & Edge Cases](#12-business-logic--edge-cases)
13. [Localization & Content](#13-localization--content)
14. [Permissions & Device Integration](#14-permissions--device-integration)
15. [Error Handling](#15-error-handling)
16. [Non-Functional Requirements](#16-non-functional-requirements)
17. [Recommended Project Structure](#17-recommended-project-structure)
18. [Development Phases](#18-development-phases)
19. [Acceptance Criteria](#19-acceptance-criteria)
20. [Known Limitations](#20-known-limitations)
21. [Reference File Index](#21-reference-file-index)

---

## 1. Executive Summary

This document defines the **technical mission** for building a native **Android application** that replicates the **user-facing (client)** experience of the existing Buron web application. The Android app must connect to the **existing `toyxona-backend` REST API without any backend modifications**.

Buron is **not** an e-commerce platform. It is a **venue booking system** where clients browse approved wedding halls (to'yxonalar), check session availability on a calendar, submit booking requests, manage their bookings, and leave reviews.

**Primary goal:** Achieve **functional and UX parity** with the web client's 7 user routes, using **Kotlin + Jetpack Compose**, while explicitly excluding all owner and admin functionality.

---

## 2. Business Context

### 2.1 What the platform does

| Concept | Description |
|---------|-------------|
| **Venue (To'yxona)** | A wedding/event hall listed by an owner, moderated and approved by admin |
| **Session** | One of three daily time slots: morning, afternoon, evening |
| **Booking (Bron)** | A client's request to reserve one or more sessions on a specific date |
| **Booking status** | `pending` → owner confirms → `confirmed`; either party can cancel → `cancelled` |
| **Review (Sharh)** | Star rating (1–5) + text comment left by a logged-in client |

### 2.2 Booking flow (end-to-end)

```
Client browses venues
    → Opens venue detail
    → Selects date on availability calendar
    → Selects available session(s)
    → Submits booking (guest or logged-in)
    → Booking created with status "pending"
    → Owner confirms on web dashboard (out of Android scope)
    → Client sees updated status in "Mening bronlarim"
```

There is **no online payment**. Price is informational (`pricePerSession`); revenue is not collected through the app.

### 2.3 User roles in the system

| Role | Web access | Android scope |
|------|------------|---------------|
| `client` (Mijoz) | Full user site | **In scope** |
| `owner` (To'yxona egasi) | `/dashboard/*` | **Out of scope** |
| `admin` (Administrator) | `/admin/*` | **Out of scope** |

If a user with role `owner` or `admin` logs in on Android, the app must **reject the session** and display an error — those users should use the web dashboard.

---

## 3. Scope Definition

### 3.1 In scope (must be implemented)

| # | Feature | Web reference route |
|---|---------|---------------------|
| 1 | Home — hero search, popular venues, geo-based region filter | `/` |
| 2 | Venue search with filters and pagination | `/search` |
| 3 | Venue detail — images, info, map link, calendar, booking, reviews | `/venues/[id]` |
| 4 | Login (phone + password) | `/login` |
| 5 | Register as client only | `/register` (client path only) |
| 6 | My bookings — list and cancel | `/my-bookings` |
| 7 | Contact page — static info + local-only form | `/contact` |
| 8 | App navigation shell (equivalent to Navbar + Footer) | Global layout |

### 3.2 Explicitly out of scope

| Feature | Reason |
|---------|--------|
| Owner dashboard (`/dashboard/*`) | User requested exclusion |
| Admin panel (`/admin/*`) | User requested exclusion |
| Venue create/edit/delete | Owner-only API |
| Booking status management (confirm/reject) | Owner-only API |
| Statistics and exports | Owner/admin-only API |
| User profile editing | Not available for clients on web |
| OTP / SMS verification | Not implemented on web or backend |
| Password reset | No backend endpoint |
| Push notifications | No backend support; API unchanged |
| Online payments | Not implemented |
| Cart, checkout, wishlist, product catalog | Not part of this domain |
| Real-time updates (WebSocket) | Not implemented |
| File uploads | Owner-only (venue images) |

### 3.3 Backend constraint

The Android app **must use the existing API as-is**. No new endpoints, no schema changes, no mobile-specific backend layer.

**Production API URL (from web config):** `https://toyxona-backend-qb3x.onrender.com/api`  
**Local development:** `http://localhost:5000/api` (emulator: `http://10.0.2.2:5000/api`)

---

## 4. Target Technology Stack

| Layer | Technology | Notes |
|-------|------------|-------|
| Language | **Kotlin 2.x** | Official Android language |
| UI framework | **Jetpack Compose** | Declarative UI |
| Design system | **Material 3** | Theming aligned with web brand colors |
| Architecture | **MVVM** | ViewModel + UI State + Repository |
| Dependency injection | **Hilt** | Standard Android DI |
| HTTP client | **Retrofit 2 + OkHttp** | REST communication |
| JSON parsing | **kotlinx.serialization** or **Moshi** | Match API response shapes |
| Image loading | **Coil** | Venue image carousels |
| Navigation | **Navigation Compose** | Type-safe routes |
| Async | **Coroutines + StateFlow** | Reactive UI updates |
| Secure storage | **EncryptedSharedPreferences** or **DataStore** | JWT token + user JSON |
| Location | **FusedLocationProviderClient** | Region detection on home |
| Min SDK | **API 26** (Android 8.0) | Broad device support |
| Target SDK | **API 35** (or latest stable) | Play Store requirement |

---

## 5. System Architecture

### 5.1 High-level diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Android App (Buron)                   │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐ │
│  │ Compose UI  │→ │  ViewModels  │→ │  Repositories   │ │
│  └─────────────┘  └──────────────┘  └────────┬────────┘ │
│                                               │          │
│  ┌─────────────────┐  ┌──────────────────────┴────────┐ │
│  │ TokenStore      │  │ Retrofit API Service           │ │
│  │ (EncryptedPrefs)│  └──────────────────────┬────────┘ │
│  └─────────────────┘                         │          │
└────────────────────────────────────────────────┼──────────┘
                                                 │ HTTPS
                                                 ▼
                              ┌──────────────────────────────┐
                              │   toyxona-backend (Express)  │
                              │   /api/*  +  /uploads/*      │
                              └──────────────┬───────────────┘
                                             │
                                             ▼
                                    ┌────────────────┐
                                    │    MongoDB     │
                                    └────────────────┘

External (optional):
  OpenStreetMap Nominatim — reverse geocoding for region detection
```

### 5.2 Layer responsibilities

| Layer | Responsibility |
|-------|----------------|
| **UI (Compose Screens)** | Render state, handle user input, navigate |
| **ViewModel** | Hold `UiState`, call repositories, expose events |
| **Repository** | Single source of truth; combine API + local auth |
| **API Service** | Retrofit interface definitions |
| **DTO** | Data transfer objects matching JSON responses |
| **TokenStore** | Persist and retrieve JWT + user object |
| **Validators** | Client-side validation (ported from web) |

### 5.3 State management approach

Mirror the web app's simplicity:
- **No global state library** (no Redux equivalent required)
- Per-screen `ViewModel` with `StateFlow<UiState>`
- Auth state read from `TokenStore`; refresh on login/logout/navigation
- Search filters held in `SearchViewModel`; reset page to 1 on filter change

---

## 6. Backend API Contract

**Base path:** `/api`  
**Auth header:** `Authorization: Bearer <jwt_token>`  
**Content-Type:** `application/json`  
**Error format:** `{ "message": "Human-readable error" }` (sometimes with `errors` array on validation failure)

### 6.1 Complete endpoint list (client scope only)

| # | Method | Endpoint | Auth | Purpose |
|---|--------|----------|------|---------|
| 1 | GET | `/health` | No | Server health check |
| 2 | POST | `/auth/register` | No | Create client account |
| 3 | POST | `/auth/login` | No | Authenticate |
| 4 | GET | `/auth/me` | **Required** | Validate token / get profile |
| 5 | GET | `/venues` | Optional | List approved venues |
| 6 | GET | `/venues/:id` | Optional | Single venue detail |
| 7 | GET | `/bookings/venue/:venueId/calendar` | No | Monthly availability |
| 8 | POST | `/bookings` | Optional | Create booking |
| 9 | GET | `/bookings/my` | **Required** | List client's bookings |
| 10 | PUT | `/bookings/:id/cancel` | **Required** | Cancel own booking |
| 11 | GET | `/reviews/venue/:venueId` | No | List venue reviews |
| 12 | POST | `/reviews` | Optional | Submit review |
| — | GET | `/uploads/:filename` | No | Serve venue images (static) |

**Rate limiting on auth routes:** 50 requests per 15 minutes per IP. On HTTP 429, show: `Too many requests, please try again later`.

---

### 6.2 Authentication endpoints

#### POST `/auth/register`

**Request body (Android — always `client`):**
```json
{
  "role": "client",
  "firstName": "Ali",
  "lastName": "Valiyev",
  "phone": "+998901234567",
  "password": "secret123"
}
```

**Success response (201):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "_id": "665f1a2b3c4d5e6f7a8b9c0d",
    "role": "client",
    "firstName": "Ali",
    "lastName": "Valiyev",
    "phone": "+998901234567",
    "telegram": "",
    "isEnabled": true,
    "createdAt": "2026-06-15T10:00:00.000Z"
  }
}
```

**Error responses:**
| Status | Message |
|--------|---------|
| 400 | `Phone number already registered` |
| 400 | `Validation failed` (+ `errors` array) |
| 429 | `Too many requests, please try again later` |

#### POST `/auth/login`

**Request body:**
```json
{
  "phone": "+998901234567",
  "password": "secret123"
}
```

**Success response (200):** Same shape as register (`token` + `user`).

**Error responses:**
| Status | Message |
|--------|---------|
| 401 | `Invalid credentials` |
| 400 | `Validation failed` |

#### GET `/auth/me`

**Success response (200):** User object (same fields as in login response, without token).

**Error responses:**
| Status | Message |
|--------|---------|
| 401 | Token missing or invalid |
| 404 | `User not found` |

**JWT payload:** `{ userId: string, role: string }`  
**JWT expiry:** `JWT_EXPIRES_IN` env var, default `7d`

---

### 6.3 Venue endpoints

#### GET `/venues`

**Query parameters:**

| Param | Type | Description |
|-------|------|-------------|
| `q` | string | Text search (name, description, address) |
| `region` | string | Filter by Uzbek region name |
| `minPrice` | number | Minimum `pricePerSession` |
| `maxPrice` | number | Maximum `pricePerSession` |
| `minCapacity` | number | Minimum guest capacity |
| `minRating` | number | Minimum average rating (e.g. 3, 4) |
| `sort` | string | `rating` \| `price` \| `name` |
| `order` | string | `asc` \| `desc` |
| `page` | number | Page number (default 1) |
| `limit` | number | Items per page (web uses 12 on search) |

**Success response (200):**
```json
{
  "venues": [ /* Venue objects */ ],
  "pagination": {
    "page": 1,
    "limit": 12,
    "total": 48,
    "pages": 4
  }
}
```

Only venues with `status: "approved"` and `isEnabled: true` are returned to clients.

#### GET `/venues/:id`

**Success response (200):** Full `Venue` object with populated `owner` sub-document.

**Error responses:**
| Status | Message |
|--------|---------|
| 404 | Venue not found or not approved |

---

### 6.4 Booking endpoints

#### GET `/bookings/venue/:venueId/calendar?month=YYYY-MM`

**Query:** `month` required, format `YYYY-MM` (e.g. `2026-06`).

**Success response (200):**
```json
{
  "month": "2026-06",
  "calendar": {
    "2026-06-01": {
      "booked": ["morning"],
      "available": ["afternoon", "evening"],
      "status": "partial"
    },
    "2026-06-02": {
      "booked": [],
      "available": ["morning", "afternoon", "evening"],
      "status": "available"
    },
    "2026-06-03": {
      "booked": ["morning", "afternoon", "evening"],
      "available": [],
      "status": "full"
    }
  }
}
```

**Day status logic (from backend):**
- `full` — all 3 sessions booked
- `partial` — 1 or 2 sessions booked
- `available` — no sessions booked

Only bookings with status `pending` or `confirmed` block sessions.

#### POST `/bookings`

**Request body:**
```json
{
  "venueId": "665f1a2b3c4d5e6f7a8b9c0d",
  "clientName": "Ali Valiyev",
  "clientPhone": "+998901234567",
  "date": "2026-06-20T00:00:00.000Z",
  "sessions": ["morning", "evening"]
}
```

**Auth behavior:**
- **Guest (no token):** `clientName` and `clientPhone` are **required**
- **Logged-in user:** backend auto-fills name and phone from user profile if omitted

**Success response (201):** Created `Booking` object (populated with venue name).

**Error responses:**
| Status | Message (examples) |
|--------|-------------------|
| 400 | `Ism va telefon talab qilinadi` |
| 400 | Session already booked / unavailable |
| 404 | `Venue not found` |

New bookings are created with `status: "pending"`.

#### GET `/bookings/my`

**Auth:** Required.

**Success response (200):** Array of `Booking` objects, populated with `venue` (name, region, district). Matched by **logged-in user's phone number**.

#### PUT `/bookings/:id/cancel`

**Auth:** Required. User's phone must match `booking.clientPhone`.

**Success response (200):** Updated booking with `status: "cancelled"`.

**Error responses:**
| Status | Message |
|--------|---------|
| 403 | `Bu bronni bekor qila olmaysiz` |
| 400 | `Bron allaqachon bekor qilingan` |
| 404 | `Bron topilmadi` |

---

### 6.5 Review endpoints

#### GET `/reviews/venue/:venueId`

**Success response (200):** Array of reviews, newest first.
```json
[
  {
    "_id": "...",
    "venue": "...",
    "authorName": "Ali Valiyev",
    "rating": 5,
    "comment": "Juda yaxshi joy!",
    "createdAt": "2026-06-10T12:00:00.000Z"
  }
]
```

#### POST `/reviews`

**Request body:**
```json
{
  "venueId": "665f1a2b3c4d5e6f7a8b9c0d",
  "authorName": "Ali Valiyev",
  "rating": 5,
  "comment": "Juda yaxshi joy!"
}
```

**Auth:** Optional but Android should only show form when logged in (match web).

**Success response (201):** Created review object. Backend recalculates venue `rating`.

---

## 7. Data Models

Port from `toyxona-frontend/lib/types.ts`:

### 7.1 User
```kotlin
data class User(
    val id: String,           // maps from _id
    val role: String,         // "client" | "owner" | "admin"
    val firstName: String,
    val lastName: String,
    val phone: String,
    val telegram: String?,
    val isEnabled: Boolean
)
```

### 7.2 Venue
```kotlin
data class Venue(
    val id: String,
    val owner: VenueOwner,
    val name: String,
    val description: String,
    val address: String,
    val mapLink: String?,
    val location: Location?,
    val region: String,
    val district: String,
    val phone: String,
    val images: List<String>,
    val pricePerSession: Long,
    val capacity: Int,
    val rating: Double,
    val totalBookings: Int,
    val status: String,
    val isEnabled: Boolean
)

data class VenueOwner(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val telegram: String?
)

data class Location(val lat: Double, val lng: Double)
```

### 7.3 Booking
```kotlin
data class Booking(
    val id: String,
    val venue: Venue,          // populated object or id string
    val clientName: String,
    val clientPhone: String,
    val date: String,          // ISO date string
    val sessions: List<String>, // "morning" | "afternoon" | "evening"
    val status: String,        // "pending" | "confirmed" | "cancelled"
    val createdAt: String?
)
```

### 7.4 Review
```kotlin
data class Review(
    val id: String,
    val venue: String,
    val authorName: String,
    val rating: Int,           // 1–5
    val comment: String,
    val createdAt: String
)
```

### 7.5 Calendar
```kotlin
data class CalendarDay(
    val booked: List<String>,
    val available: List<String>,
    val status: String         // "available" | "partial" | "full"
)

data class CalendarResponse(
    val month: String,
    val calendar: Map<String, CalendarDay>
)
```

### 7.6 Venues list response
```kotlin
data class VenuesResponse(
    val venues: List<Venue>,
    val pagination: Pagination
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)
```

---

## 8. Screen-by-Screen Specification

### 8.1 HomeScreen

**Web reference:** `toyxona-frontend/app/page.tsx`

**Layout sections:**

1. **Hero / Search bar**
   - Text input with placeholder for venue search
   - Submit navigates to `SearchScreen` with query parameter `q`
   - Equivalent to `SearchBar` component

2. **Popular venues ("Mashhur to'yxonalar")**
   - On first load:
     - Request `ACCESS_FINE_LOCATION` permission (with rationale dialog in Uzbek)
     - If granted: get GPS coordinates → call Nominatim reverse geocode API:
       ```
       GET https://nominatim.openstreetmap.org/reverse
         ?lat={lat}&lon={lon}&format=json
       Headers: Accept-Language: en
       ```
     - Map result to Uzbek region using alias table from `lib/geolocation.ts`
     - Fetch: `GET /venues?region={region}&sort=rating&order=desc&limit=6`
     - If geolocation fails: fetch without region filter
   - Display venue cards in horizontal scroll or vertical list
   - "Ko'proq ko'rish" button → `SearchScreen`

3. **Inline search on popular section** (if present on web home)
   - Debounced text filter
   - Sort dropdown: rating / price / name

4. **Contact teaser**
   - Brief contact info snippet
   - Link to `ContactScreen`

**Loading state:** Skeleton cards (`VenueCardSkeleton` equivalent)  
**Empty state:** Message when no venues found

---

### 8.2 SearchScreen

**Web reference:** `toyxona-frontend/app/search/page.tsx`

**UI elements:**

1. **Page title:** "Qidiruv natijalari"
2. **Search bar** — pre-filled from navigation `q` param; updates results on submit
3. **Filter panel** (sidebar on web → bottom sheet or expandable panel on mobile):

| Filter | UI control | API param |
|--------|------------|-----------|
| Viloyat (Region) | Dropdown | `region` |
| Min narx | Number input | `minPrice` |
| Max narx | Number input | `maxPrice` |
| Min sig'im | Number input | `minCapacity` |
| Min reyting | Dropdown: Barchasi / 3+ / 4+ / 5 | `minRating` |
| Tartiblash | Dropdown: Reyting / Narx / Nomi | `sort` |
| Tartib | Dropdown: Kamayish / O'sish | `order` |

**Region options** (exact list from `lib/geolocation.ts`):
```
Toshkent, Samarqand, Buxoro, Farg'ona, Andijon, Namangan,
Qashqadaryo, Surxondaryo, Jizzax, Sirdaryo, Navoiy, Xorazm, Qoraqalpog'iston
```

4. **Results grid:** 2 columns on phone; each cell is a `VenueCard`
5. **Pagination:** `limit=12`, show "Oldingi" / "Keyingi" buttons; disable at boundaries
6. **Reset page to 1** when any filter changes

**States:**
- Loading: "Yuklanmoqda..."
- Empty: "Hech narsa topilmadi."
- Error: API message or generic network error

---

### 8.3 VenueDetailScreen

**Web reference:** `toyxona-frontend/app/venues/[id]/page.tsx`

**Data loading:**
- `GET /venues/:id`
- `GET /reviews/venue/:id`

**Sections:**

1. **Image carousel**
   - Swipeable gallery of `venue.images[]`
   - Load with Coil; handle broken image fallback

2. **Venue header**
   - Name (large title)
   - Star rating display (`venue.rating`)
   - Price: `{pricePerSession}` formatted as Uzbek currency + "sessiya uchun"

3. **Details block**
   - Description (full text)
   - Address with map pin icon
   - Region + district
   - Phone (tappable → dialer intent)
   - Capacity: "{capacity} kishi"
   - Owner name: "{firstName} {lastName}"

4. **Map link button**
   - Visible only if `venue.mapLink` is non-empty
   - Opens URL via `Intent.ACTION_VIEW`
   - Component equivalent: `VenueMapLink`

5. **Availability calendar**
   - Month selector (prev/next month arrows)
   - On month change: `GET /bookings/venue/:id/calendar?month=YYYY-MM`
   - Day cells color-coded:
     - `available` → green tint, selectable
     - `partial` → yellow tint, selectable
     - `full` → gray/red, not selectable
   - On day tap: show available session chips below calendar
   - User can select one or more sessions from `available` list
   - Session labels:
     - `morning` → "Nahorgi (09:00–14:00)"
     - `afternoon` → "Abetgi (14:00–18:00)"
     - `evening` → "Kechgi (18:00–23:00)"
   - "Bron qilish" button enabled only when date + ≥1 session selected

6. **Booking bottom sheet / dialog**
   - Opens on "Bron qilish" tap
   - See section 8.4

7. **Reviews section**
   - List all reviews: author name, stars, comment, date
   - Review form (logged-in only) — see section 8.5

---

### 8.4 BookingBottomSheet

**Web reference:** `toyxona-frontend/components/BookingModal/BookingModal.tsx`

**Display:**
- Selected date (formatted `uz-UZ` locale)
- Selected sessions (localized labels, comma-separated)
- Close button (X)

**Form behavior:**

| User state | Name field | Phone field | Submit data |
|------------|------------|-------------|-------------|
| Logged in | Hidden (auto: firstName + lastName) | Hidden (auto: user.phone) | `POST /bookings` with auto values |
| Guest | Required text input | Required phone input (+998 mask) | `POST /bookings` with entered values |

**Validation (guest only):**
- Name: `validateName` rules
- Phone: `validatePhoneUz` + `normalizePhoneUz`

**Submit:**
```json
{
  "venueId": "...",
  "clientName": "...",
  "clientPhone": "+998...",
  "date": "<ISO 8601 string>",
  "sessions": ["morning"]
}
```

**Success:**
- Show confirmation UI inside sheet
- Snackbar: "Bron qabul qilindi!"
- Close sheet; optionally refresh calendar for selected month

**Error:**
- Display `response.message` from API
- Keep sheet open for retry

---

### 8.5 ReviewForm (embedded in VenueDetailScreen)

**Web reference:** `toyxona-frontend/components/ReviewForm/ReviewForm.tsx`

**Visibility:** Only when user is logged in. Hidden for guests.

**Fields:**
- Rating selector: dropdown or star picker, values 5 down to 1
- Comment textarea, required, minimum 5 characters

**Submit:**
```json
{
  "venueId": "...",
  "authorName": "{firstName} {lastName}",
  "rating": 5,
  "comment": "..."
}
```

**On success:**
- Snackbar: "Sharh qo'shildi"
- Clear comment field
- Reload reviews list
- Venue rating on detail screen will update on next venue fetch

---

### 8.6 MyBookingsScreen

**Web reference:** `toyxona-frontend/app/my-bookings/page.tsx`

**Auth gate:** If no token → redirect to `LoginScreen`

**Data:** `GET /bookings/my`

**Page title:** "Mening bronlarim"

**Booking card content:**
- Venue name (tappable → `VenueDetailScreen`)
- Status badge with color:
  - `pending` → yellow — "Kutilmoqda"
  - `confirmed` → green — "Tasdiqlangan"
  - `cancelled` → red — "Bekor qilingan"
- Region + district (if venue populated)
- Date formatted `uz-UZ`
- Session chips with localized labels

**Cancel action:**
- Visible for `pending` and `confirmed` bookings
- Confirmation dialog: "Bronni bekor qilishni tasdiqlaysizmi?"
- On confirm: `PUT /bookings/:id/cancel`
- Success snackbar: "Bron bekor qilindi"
- Reload list

**Empty state:**
- Message: "Hali bron qilmagansiz"
- Button: "To'yxona qidirish" → `SearchScreen`

**Pull-to-refresh:** Recommended for mobile UX

---

### 8.7 LoginScreen

**Web reference:** `toyxona-frontend/app/login/page.tsx`

**Fields:**
- Phone (`FormPhoneInput` equivalent with +998 prefix)
- Password (masked input)

**Validation before submit:**
- Phone: Uzbek format rules
- Password: non-empty

**Submit:** `POST /auth/login`

**On success:**
- If `user.role == "client"`: save token + user → navigate to `HomeScreen`
- If `user.role == "owner"` or `"admin"`: **do not save**; show error:
  - "Bu ilova faqat mijozlar uchun" (or equivalent)

**On error:** Display `Invalid credentials` or validation message

**Links:**
- "Ro'yxatdan o'tish" → `RegisterScreen`

---

### 8.8 RegisterScreen

**Web reference:** `toyxona-frontend/app/register/page.tsx` — **client path only**

**Simplification vs web:** Web has step 1 (role selection: Mijoz / To'yxona egasi). Android **skips role selection** and always registers as `client`. No Telegram field.

**Fields:**
- Ism (firstName)
- Familiya (lastName)
- Telefon (phone)
- Parol (password)

**Validation:**
- firstName, lastName: name rules
- phone: Uzbek phone rules + normalization
- password: 6–64 characters

**Submit:**
```json
{
  "role": "client",
  "firstName": "...",
  "lastName": "...",
  "phone": "+998...",
  "password": "..."
}
```

**On success:** Auto-login (save token + user) → navigate to `HomeScreen`  
**Snackbar:** Registration success message (match web toast)

**Links:**
- "Kirish" → `LoginScreen`

---

### 8.9 ContactScreen

**Web reference:** `toyxona-frontend/app/contact/page.tsx`

**Static contact info** (from `lib/constants.ts` → `CONTACT_INFO`):

| Field | Value |
|-------|-------|
| Address | Toshkent sh., Yunusobod tumani |
| Phone | +998 (90) 000-00-00 |
| Telegram | @buron_uz |
| Email | info@buron.uz |
| Hours | 09:00 – 22:00 (har kuni) |

**Tappable actions:**
- Phone → `tel:` intent
- Telegram → open Telegram app or web
- Email → `mailto:` intent

**Contact form** (no API call — match web `ContactForm`):
- Name, phone, message fields
- Client-side validation
- On submit: show success snackbar only
- Form clears after success

---

### 8.10 App Shell / Navigation

**Web reference:** `components/Navbar/Navbar.tsx`, `components/Footer/Footer.tsx`

**Recommended Android navigation:**

**Bottom navigation bar (4 tabs):**
| Tab | Destination | Icon |
|-----|-------------|------|
| Bosh sahifa | HomeScreen | Home |
| Qidiruv | SearchScreen | Search |
| Bronlarim | MyBookingsScreen (auth-gated) | Calendar |
| Aloqa | ContactScreen | Phone/Mail |

**Top app bar:**
- App name / logo: "Buron"
- When logged in: show user first name or avatar placeholder
- Login / Register buttons when logged out
- Logout action in overflow menu when logged in

**Must NOT include:**
- Links to `/dashboard`
- Links to `/admin`
- Owner registration option

---

## 9. Shared UI Components

Build reusable Compose components mirroring web components:

| Component | Web reference | Android responsibility |
|-----------|---------------|------------------------|
| `VenueCard` | `components/VenueCard/VenueCard.tsx` | Image, name, rating, price, address, owner; tap → detail |
| `VenueCardSkeleton` | `components/VenueCardSkeleton/` | Shimmer loading placeholder |
| `StarRating` | `components/StarRating/StarRating.tsx` | Display 0–5 stars (full/half/empty) |
| `SearchBar` | `components/SearchBar/SearchBar.tsx` | Text input + search action |
| `FilterPanel` | `components/FilterPanel/FilterPanel.tsx` | All search filters |
| `AvailabilityCalendar` | `components/Calendar/AvailabilityCalendar.tsx` | Month grid with color-coded days |
| `FormPhoneField` | `components/ui/FormPhoneInput.tsx` | +998 masked phone input |
| `FormTextField` | `components/ui/FormInput.tsx` | Styled text input with label + error |
| `PrimaryButton` | `components/ui/Button.tsx` | Main CTA button |
| `StatusBadge` | constants `STATUS_BADGE` | Colored pill for booking status |
| `SessionChip` | session labels in constants | Small tag for session name |
| `ReviewCard` | inline in venue page | Single review display |
| `EmptyState` | various pages | Illustration + message + action button |

---

## 10. Validation Rules

Port exactly from `toyxona-frontend/lib/validation.ts`:

### 10.1 Phone (Uzbekistan mobile)
```
Regex: ^\+998(33|50|55|77|88|90|91|93|94|95|97|98|99)\d{7}$
Error: "Telefon: +998 va 9 ta raqam (masalan +998901234567)"
```

**Normalization (`normalizePhoneUz`):**
- Strip non-digits
- If starts with `998` → prepend `+`
- If 9 digits → prepend `+998`
- If starts with `+998` → keep prefix + next 9 digits

### 10.2 Name
```
Regex: ^[\u0400-\u04FFa-zA-Z\s'-]{2,50}$
Error: "{label}: faqat harflar, 2–50 belgi"
```

### 10.3 Password
```
Regex: ^.{6,64}$
Error: "Parol kamida 6 belgidan iborat bo'lishi kerak"
```

### 10.4 Search query
```
Regex: ^[\u0400-\u04FFa-zA-Z0-9\s'-]{0,80}$
```

### 10.5 Review comment
- Minimum 5 characters (client-side only)
- Error: "Sharh kamida 5 belgi"

---

## 11. Authentication & Session Management

**Web reference:** `lib/auth.ts`, `lib/api.ts`

### 11.1 Storage

| Key | Content |
|-----|---------|
| `buron_token` | JWT string |
| `buron_user` | JSON-serialized User object |

Use **EncryptedSharedPreferences** on Android (web uses localStorage/sessionStorage).

### 11.2 HTTP interceptor

Attach to every OkHttp request:
```
Authorization: Bearer <token>
```
Only when token exists.

### 11.3 Session lifecycle

```
App launch
  → Read token from storage
  → If token exists: optional GET /auth/me to validate
  → If 401: clear storage, treat as logged out

Login success
  → Save token + user
  → Navigate to Home

Logout
  → Clear storage
  → Navigate to Home

Protected screen access (MyBookings)
  → If no token: redirect to Login
```

### 11.4 Role enforcement

Android app is **client-only**. After login/register:
```kotlin
if (user.role != "client") {
    clearAuth()
    showError("Bu ilova faqat mijozlar uchun")
    return
}
```

---

## 12. Business Logic & Edge Cases

### 12.1 Guest booking → account linking

`GET /bookings/my` matches bookings by **phone number**, not user ID. A guest who books with phone `+998901234567` will see that booking after registering/logging in with the same phone.

**Document for QA:** Guest bookings are linked by phone.

### 12.2 Calendar session selection

- Only sessions in `calendar[date].available` may be selected
- User can select multiple sessions on the same day
- Cannot book on `full` days
- On `partial` days, only remaining sessions are selectable

### 12.3 Booking cancellation rules

- Client can cancel `pending` and `confirmed` bookings
- Cannot cancel already `cancelled` bookings
- Must own the booking (phone match)

### 12.4 Venue visibility

- Only `status: "approved"` and `isEnabled: true` venues appear in lists and detail
- Attempting to book a non-approved venue returns 404

### 12.5 Image URLs

Venue `images[]` contains full URLs (e.g. `https://api.example.com/uploads/filename.jpg`). Load directly — no URL construction needed on client.

### 12.6 Date handling

- Send booking date as ISO 8601 string (web uses `date.toISOString()`)
- Display dates with `uz-UZ` locale
- Calendar month format: `YYYY-MM`

---

## 13. Localization & Content

### 13.1 Primary language

**Uzbek** — all UI strings must match the web application.

### 13.2 Key labels (from `lib/constants.ts`)

**Sessions:**
| Key | Label |
|-----|-------|
| morning | Nahorgi (09:00–14:00) |
| afternoon | Abetgi (14:00–18:00) |
| evening | Kechgi (18:00–23:00) |

**Booking status:**
| Key | Label |
|-----|-------|
| pending | Kutilmoqda |
| confirmed | Tasdiqlangan |
| cancelled | Bekor qilingan |

### 13.3 Price formatting

Format `pricePerSession` with Uzbek locale grouping:
- Example: `1 500 000 so'm`
- Suffix: "sessiya uchun" or equivalent web label

### 13.4 Date formatting

Use `Locale("uz", "UZ")` for booking dates and review dates.

---

## 14. Permissions & Device Integration

| Permission | Required | Purpose |
|------------|----------|---------|
| `INTERNET` | Yes | API calls |
| `ACCESS_FINE_LOCATION` | Optional (runtime) | Region detection on home screen |
| `ACCESS_COARSE_LOCATION` | Optional | Fallback for region detection |

**No permissions needed for:** camera, storage, notifications, contacts.

**External intents:**
| Action | Trigger |
|--------|---------|
| Dialer | Tap venue phone or contact phone |
| Browser/Maps | Tap `mapLink` |
| Telegram | Tap contact telegram |
| Email | Tap contact email |

---

## 15. Error Handling

| Scenario | User-facing message |
|----------|---------------------|
| No internet | "Internet aloqasi yo'q" |
| API 400 | Show `response.message` |
| API 401 on protected route | Redirect to login |
| API 403 | Show `response.message` |
| API 404 | Show `response.message` or "Topilmadi" |
| API 429 | "Juda ko'p so'rov. Keyinroq urinib ko'ring." |
| API 500 | "Server xatosi. Keyinroq urinib ko'ring." |
| Image load failure | Placeholder image |
| Location denied | Skip region filter; show all popular venues |

All error/success feedback via **Snackbar** (equivalent to Sonner toasts on web).

---

## 16. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Cold start | < 3 seconds on mid-range device |
| API timeout | 30 seconds |
| Offline | Show error state; no offline cache required for v1 |
| Security | Token in encrypted storage; no logging of passwords |
| APK size | < 15 MB (reasonable for v1) |
| Accessibility | Content descriptions on images and buttons |
| Orientation | Portrait primary; landscape optional |
| Min device | Android 8.0 (API 26) |

---

## 17. Recommended Project Structure

```
buron-android/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/uz/buron/
│       │   ├── BuronApplication.kt
│       │   ├── di/
│       │   │   ├── AppModule.kt
│       │   │   ├── NetworkModule.kt
│       │   │   └── RepositoryModule.kt
│       │   ├── data/
│       │   │   ├── api/
│       │   │   │   ├── BuronApiService.kt
│       │   │   │   └── AuthInterceptor.kt
│       │   │   ├── dto/
│       │   │   │   ├── UserDto.kt
│       │   │   │   ├── VenueDto.kt
│       │   │   │   ├── BookingDto.kt
│       │   │   │   └── ReviewDto.kt
│       │   │   ├── repository/
│       │   │   │   ├── AuthRepository.kt
│       │   │   │   ├── VenueRepository.kt
│       │   │   │   ├── BookingRepository.kt
│       │   │   │   └── ReviewRepository.kt
│       │   │   └── local/
│       │   │       └── TokenStore.kt
│       │   ├── domain/
│       │   │   └── model/          # Optional domain models
│       │   ├── ui/
│       │   │   ├── navigation/
│       │   │   │   ├── BuronNavGraph.kt
│       │   │   │   └── Screen.kt
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Type.kt
│       │   │   │   └── Theme.kt
│       │   │   ├── components/
│       │   │   │   ├── VenueCard.kt
│       │   │   │   ├── StarRating.kt
│       │   │   │   ├── AvailabilityCalendar.kt
│       │   │   │   ├── FormPhoneField.kt
│       │   │   │   └── ...
│       │   │   └── screens/
│       │   │       ├── home/
│       │   │       ├── search/
│       │   │       ├── venue/
│       │   │       ├── booking/
│       │   │       ├── auth/
│       │   │       ├── mybookings/
│       │   │       └── contact/
│       │   └── util/
│       │       ├── Validation.kt
│       │       ├── PhoneUtils.kt
│       │       ├── RegionDetector.kt
│       │       └── DateUtils.kt
│       └── res/
│           ├── values/
│           │   └── strings.xml     # Uzbek strings
│           └── ...
├── build.gradle.kts
└── gradle/libs.versions.toml
```

---

## 18. Development Phases

### Phase 1 — Foundation
- Project setup (Compose, Hilt, Retrofit, Navigation, Theme)
- API service + all DTOs
- TokenStore + AuthInterceptor
- Validation utilities ported from web

### Phase 2 — Discovery
- HomeScreen with geolocation + popular venues
- SearchScreen with full filter panel + pagination
- VenueCard + skeleton components

### Phase 3 — Venue Detail & Booking
- VenueDetailScreen with image carousel
- AvailabilityCalendar component
- Session selection + BookingBottomSheet
- POST /bookings integration

### Phase 4 — Auth & Bookings Management
- LoginScreen + RegisterScreen (client-only)
- Role guard for non-client users
- MyBookingsScreen with cancel flow

### Phase 5 — Reviews & Contact
- Review list + ReviewForm on venue detail
- ContactScreen with static info + local form

### Phase 6 — Polish & QA
- Error/empty/loading states on all screens
- Pull-to-refresh on bookings
- Parity testing against web
- Signed release build (APK/AAB)

---

## 19. Acceptance Criteria

The Android app is **accepted** when all items below pass manual QA against the live web app and backend:

- [ ] Home loads popular venues; region filter works when location granted
- [ ] Search returns same results as web for identical filters
- [ ] Venue detail shows all fields, images, and reviews
- [ ] Calendar correctly colors days and lists available sessions
- [ ] Guest can complete a booking with name + phone
- [ ] Logged-in client can book with auto-filled details
- [ ] Client can register a new account (role always `client`)
- [ ] Client can log in with phone + password
- [ ] Owner/admin login is rejected on Android
- [ ] My bookings lists all bookings for user's phone
- [ ] Client can cancel pending and confirmed bookings
- [ ] Logged-in client can submit a review (min 5 chars)
- [ ] Guest cannot see review form
- [ ] Contact page shows correct static info
- [ ] Contact form shows success without API call
- [ ] Map link opens external app
- [ ] Phone numbers open dialer
- [ ] All validation messages match web (Uzbek)
- [ ] No owner/admin screens or API calls exist in the app

---

## 20. Known Limitations

Because the backend cannot be modified:

| Limitation | Impact |
|------------|--------|
| No push notifications | User must manually check booking status |
| No password reset | Users cannot recover password in-app |
| No client profile edit | Name/phone changes require re-registration or web (not available) |
| No OTP verification | Phone numbers are not verified via SMS |
| No payment | Bookings are requests only; no in-app payment |
| Bookings linked by phone | Changing phone loses access to old guest bookings |
| Auth rate limit (50/15min) | Heavy testing may trigger 429 |
| No offline mode | App requires internet for all data |

---

## 21. Reference File Index

### Frontend (UI/UX reference)
| File | Purpose |
|------|---------|
| `toyxona-frontend/app/page.tsx` | Home screen |
| `toyxona-frontend/app/search/page.tsx` | Search screen |
| `toyxona-frontend/app/venues/[id]/page.tsx` | Venue detail |
| `toyxona-frontend/app/login/page.tsx` | Login |
| `toyxona-frontend/app/register/page.tsx` | Register |
| `toyxona-frontend/app/my-bookings/page.tsx` | My bookings |
| `toyxona-frontend/app/contact/page.tsx` | Contact |
| `toyxona-frontend/lib/types.ts` | Data models |
| `toyxona-frontend/lib/validation.ts` | Validation rules |
| `toyxona-frontend/lib/constants.ts` | Labels and contact info |
| `toyxona-frontend/lib/auth.ts` | Auth storage pattern |
| `toyxona-frontend/lib/api.ts` | Axios + interceptor pattern |
| `toyxona-frontend/lib/geolocation.ts` | Region list + detection |
| `toyxona-frontend/components/BookingModal/BookingModal.tsx` | Booking flow |
| `toyxona-frontend/components/ReviewForm/ReviewForm.tsx` | Review submission |
| `toyxona-frontend/components/FilterPanel/FilterPanel.tsx` | Search filters |
| `toyxona-frontend/components/Calendar/AvailabilityCalendar.tsx` | Calendar UI |

### Backend (API reference)
| File | Purpose |
|------|---------|
| `toyxona-backend/src/routes/auth.js` | Auth routes |
| `toyxona-backend/src/routes/venues.js` | Venue routes |
| `toyxona-backend/src/routes/bookings.js` | Booking routes |
| `toyxona-backend/src/routes/reviews.js` | Review routes |
| `toyxona-backend/src/controllers/authController.js` | Auth logic |
| `toyxona-backend/src/controllers/bookingController.js` | Booking + calendar logic |
| `toyxona-backend/src/controllers/venueController.js` | Venue listing logic |
| `toyxona-backend/src/controllers/reviewController.js` | Review logic |
| `toyxona-backend/src/models/User.js` | User schema |
| `toyxona-backend/src/models/Venue.js` | Venue schema |
| `toyxona-backend/src/models/Booking.js` | Booking schema |
| `toyxona-backend/src/models/Review.js` | Review schema |
| `toyxona-backend/src/middleware/auth.js` | JWT verification |

---

*End of technical mission document.*
