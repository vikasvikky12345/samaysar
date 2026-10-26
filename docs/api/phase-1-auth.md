# Phase 1 API contract — auth, clinic signup, profile, staff

Shared contract for backend (`backend/`) and frontend (`frontend/`). Change it here first.
All paths are under `/api`, same origin (Angular dev proxy / nginx → backend :8085).
Errors use Spring `ProblemDetail` (RFC 7807): `{ "type": "…/<code>", "title", "status", "detail", "code": "<code>" }`.

## Conventions
- **Mobile**: client sends 10 digits (`9876543210`); server validates Indian mobile `^[6-9]\d{9}$`,
  stores E.164 `+919876543210`, returns E.164. Never log full numbers — mask as `+91******3210`.
- **Access token**: JWT (HS256), 15 min, in JSON body only; frontend keeps it **in memory**.
  Claims: `sub` = user id (UUID), `clinic_id` (UUID), `role` (`OWNER|DOCTOR|RECEPTION|ADMIN`).
- **Refresh token**: random 256-bit, 30 days, sent **only** as cookie
  `sq_refresh` — `HttpOnly; Secure; SameSite=Strict; Path=/api/auth`. Stored hashed (SHA-256). Rotated on every refresh; reuse of a revoked token revokes the whole family for that user.
- IDs are UUIDs. Times are ISO-8601 UTC.

## Endpoints

### `POST /api/auth/otp/send` (public)
Body `{ "mobile": "9876543210" }` → `202 { "expiresInSeconds": 300 }`
- 6-digit code, stored hashed, 5-min expiry, max 5 verify attempts, max 3 sends per mobile per 15 min.
- Errors: `400 invalid_mobile`, `429 otp_rate_limited`.

### `POST /api/auth/otp/verify` (public)
Body `{ "mobile": "9876543210", "code": "123456" }`
- Existing active user → `200 { "status": "LOGGED_IN", "accessToken", "expiresInSeconds": 900, "user": Me }` + sets refresh cookie.
- No user for this mobile → `200 { "status": "SIGNUP_REQUIRED", "signupToken" }` (JWT, 15 min, claim `purpose=signup`, `sub`=E.164 mobile).
- Errors: `400 otp_invalid` (wrong/expired), `429 otp_attempts_exceeded`, `403 user_inactive`.

### `POST /api/auth/signup` (public, needs signupToken)
Body `{ "signupToken", "ownerName": "Dr Amit Sharma", "clinicName": "Apollo Dental", "city": "Indore" }`
→ `201 { "accessToken", "expiresInSeconds", "user": Me }` + refresh cookie.
- Creates `clinic` (slug from name, e.g. `apollo-dental`; on clash append city, then `-2`, `-3`…;
  reserved slugs rejected → suffix) and `app_user` with role `OWNER`. Trial: `trial_ends_at = now + 14 days`.
- Errors: `400 validation_failed`, `401 signup_token_invalid`, `409 mobile_already_registered`.

### `POST /api/auth/refresh` (cookie)
→ `200 { "accessToken", "expiresInSeconds", "user": Me }` + new cookie. `401 refresh_invalid` if missing/expired/revoked.

### `POST /api/auth/logout` (cookie) → `204`, revokes token, clears cookie.

### `GET /api/me` (any role) → `200 Me`
```json
Me = { "id", "name", "mobile": "+919876543210", "role": "OWNER",
       "clinic": { "id", "name", "slug", "planStatus": "TRIAL", "trialEndsAt" } }
```

### `GET /api/clinic` (any role) → `200 Clinic`
### `PUT /api/clinic` (OWNER) body = editable fields → `200 Clinic`
```json
Clinic = { "id", "slug", "name", "address", "city", "phone", "whatsapp", "mapUrl", "about",
           "timezone": "Asia/Kolkata", "planStatus", "trialEndsAt" }
```
Editable: `name` (required, ≤120), `address` (≤300), `city` (≤80), `phone`/`whatsapp` (10-digit, optional), `mapUrl` (https URL, optional), `about` (≤1000). Slug not editable in Phase 1.

### Staff (OWNER only)
- `GET /api/staff` → `200 [Staff]` (own clinic only)
- `POST /api/staff` body `{ "name", "mobile", "role": "DOCTOR|RECEPTION" }` → `201 Staff`; `409 mobile_already_registered`
- `PATCH /api/staff/{id}` body `{ "active": false }` or `{ "role": "RECEPTION" }` → `200 Staff`; owner cannot deactivate self (`400 cannot_modify_self`)
- Another clinic's id → **`404`** (never 403 — don't reveal existence)
```json
Staff = { "id", "name", "mobile", "role", "active", "createdAt" }
```
Staff log in with the same OTP flow; their mobile maps to their clinic.

## Roles
| Endpoint | OWNER | DOCTOR | RECEPTION |
|---|---|---|---|
| `/me`, `GET /clinic` | ✓ | ✓ | ✓ |
| `PUT /clinic`, `/staff*` | ✓ | – | – |

## Frontend routes
`/login` (mobile → OTP) · `/signup` (after SIGNUP_REQUIRED) · `/app` (guarded shell) ·
`/app/settings/clinic` · `/app/staff` (OWNER only in nav).
