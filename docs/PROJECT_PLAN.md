# Samaysar — Clinic Appointment & Queue App

Complete project plan: problem, requirements, tech stack, data model, APIs, phases, launch and business.
Owner: solo full-stack developer. Target market: small clinics in India.

**Brand:** Samaysar ("samay-sar" = on time). Tagline: *Right time. Every time.* /
*Sahi samay par, har baar.* Domains `samaysar.in` + `samaysar.com` were free on 2026-10-26 —
register both. Internal code names stay `clinicq` (Java package, DB) — not user-visible.

---

## 1. Product Summary

**One line:** Patients book a clinic slot from a WhatsApp/web link, get reminders and a live queue
number, and the clinic runs its day from one simple screen.

**Problem**
- Clinics run on paper registers and phone calls; receptionists are overloaded.
- Patients wait 1–3 hours with no idea when their turn comes.
- No-shows waste 10–20% of slots; there's no reminder system.
- Doctors have no record of visit history, follow-ups, or daily earnings.

**Solution**
- Public booking page per clinic (no app install, no patient login) that **doubles as the clinic's
  website**: doctor profile, qualifications, timings, map, call/WhatsApp buttons, findable on Google.
- Automatic WhatsApp confirmation + reminder + "your turn is near" message.
- Reception dashboard: today's queue, walk-ins, mark arrived/done/no-show.
- Simple patient records, follow-up reminders, and daily collection report.

**Not in scope (MVP):** e-prescriptions, lab integration, insurance, video consults, native mobile
apps, inventory/pharmacy, multi-language UI, custom domains, an open public review marketplace,
AI-written profile copy. Revisit after 20 paying clinics.

---

## 2. Users & Personas

| Persona | Goal | Device |
|---------|------|--------|
| **Clinic owner / doctor** | Fewer no-shows, see today's list, see earnings | Phone |
| **Receptionist** | Manage queue fast, add walk-ins, take payment | Desktop / tablet / phone |
| **Patient** | Book slot, know when to come, not wait | Phone (WhatsApp) |
| **Platform admin (you)** | Onboard clinics, manage subscriptions, support | Desktop |

---

## 3. Functional Requirements

Priority: **P0** = MVP must-have, **P1** = first 3 months after launch, **P2** = later.

### 3.1 Clinic onboarding & settings
| ID | Requirement | P |
|----|-------------|---|
| C1 | Owner signs up with mobile number + OTP | P0 |
| C2 | Clinic profile: name, address, map link, phone, logo, specialty | P0 |
| C3 | Add doctors (one clinic → many doctors) with name, specialty, consultation fee | P0 |
| C4 | Weekly schedule per doctor: days, sessions (e.g. 10:00–13:00, 17:00–20:00), slot length | P0 |
| C5 | Holidays / leave blocks (whole day or session) | P0 |
| C6 | Booking mode per doctor: **time slots** or **token queue** (many Indian clinics prefer tokens) | P0 |
| C7 | Max bookings per session; % of slots kept for walk-ins | P0 |
| C8 | Staff users (receptionist) with limited roles | P0 |
| C9 | Unique public booking link + QR code poster (printable PDF) | P0 |
| C10 | Multiple branches under one owner | P2 |

### 3.2 Patient booking (public, no login)
| ID | Requirement | P |
|----|-------------|---|
| B1 | Open clinic / doctor / service link → steps that have only one choice are skipped automatically (see §10A) | P0 |
| B1a | Clinic defines services (e.g. Consultation, Cleaning, X-ray) with duration + fee, mapped to doctors | P0 |
| B2 | Verify mobile via **WhatsApp tap-to-verify** (patient sends a prefilled message, returns to web); SMS OTP fallback | P0 |
| B2a | Enter name, age, gender (prefilled for returning patients) | P0 |
| B3 | Booking confirmation screen + WhatsApp confirmation with token/time | P0 |
| B4 | Cancel / reschedule via link in WhatsApp message | P0 |
| B5 | Book for family member (same mobile, different patient) | P1 |
| B6 | Optional online pre-payment of consultation fee (Razorpay) | P1 |
| B7 | Live queue page: "Now serving #12, you are #17, approx 25 min" | P0 |
| B8 | Booking via WhatsApp chat flow (no web page) | P2 |
| B9 | **Request mode** (third booking mode): patient sends preferred date/time, clinic confirms or proposes another; page says clearly "awaiting confirmation" vs "confirmed" | P1 |

### 3.2A Public profile page (the clinic's website)
Same page as the booking link (`samaysar.in/{clinic}` and `/{clinic}/{doctor}`) — not a separate site builder.

| ID | Requirement | P |
|----|-------------|---|
| W1 | Doctor profile: photo, qualifications, registration no. + council, experience, languages, short bio | P0 |
| W2 | Clinic section: photos, address + map, timings (from schedule), services with fees, **Call** and **WhatsApp click-to-chat** buttons | P0 |
| W3 | SEO basics: server-rendered HTML, page title, meta description, share image (WhatsApp/Facebook preview) | P0 |
| W4 | Preview, publish / unpublish, profile-completeness checklist ("add photo", "add timings") | P0 |
| W5 | Look: clinic logo + one accent colour (P0); 2–3 layout templates (P1) | P0 |
| W6 | schema.org `Physician`/`MedicalClinic` structured data + sitemap.xml | P1 |
| W7 | Per-clinic stats in dashboard: page views, call/WhatsApp clicks, bookings started → booked | P1 |
| W8 | "Registration checked" badge only after a recorded manual check against the state council/NMC register | P1 |
| W9 | Subdomain (`drsharma.samaysar.in`) then doctor-owned custom domain | P2 |
| W10 | Profile content in Hindi / regional language | P2 |

### 3.3 Reception dashboard (core daily screen)
| ID | Requirement | P |
|----|-------------|---|
| R1 | Today's list per doctor/session, sorted by token/time | P0 |
| R2 | Add walk-in in < 10 seconds (mobile + name only) | P0 |
| R3 | Status flow: Booked → Arrived → In consultation → Done / No-show / Cancelled | P0 |
| R4 | "Call next" button → advances queue, sends "your turn is near" to patient N+2 | P0 |
| R5 | Mark payment: cash / UPI / card / online-paid, amount | P0 |
| R6 | Search patient by mobile or name | P0 |
| R7 | Delay broadcast: "Doctor is running 30 min late" to all remaining patients | P1 |
| R8 | TV display mode (big token number for waiting room) | P1 |
| R9 | Works on a slow connection; auto-refresh queue every few seconds | P0 |

### 3.4 Patient records
| ID | Requirement | P |
|----|-------------|---|
| P1 | Patient profile auto-created on first booking (deduped by mobile + name) | P0 |
| P2 | Visit history list | P0 |
| P3 | Short visit note + follow-up date per visit | P1 |
| P4 | Follow-up reminder on WhatsApp on that date | P1 |
| P5 | Upload prescription photo / report to a visit | P2 |

### 3.5 Notifications (WhatsApp first, SMS fallback)
| Event | Message | P |
|-------|---------|---|
| Booking confirmed | Clinic, doctor, date, token/time, map link, cancel link | P0 |
| Reminder | Evening before + 2 hours before | P0 |
| Turn near | When 2 patients ahead remain | P0 |
| Cancelled by clinic | With rebook link | P0 |
| Doctor delayed | Broadcast | P1 |
| Follow-up due | Morning of follow-up date | P1 |
| Review request | After visit → Google review link (clinics love this) | P1 |

### 3.6 Reports
| ID | Requirement | P |
|----|-------------|---|
| RP1 | Daily summary: patients seen, no-shows, collection by payment mode | P0 |
| RP2 | Monthly: patients, new vs returning, revenue, no-show rate trend | P1 |
| RP3 | Export CSV | P1 |

### 3.7 Billing (your revenue)
| ID | Requirement | P |
|----|-------------|---|
| S1 | 14-day free trial, no card | P0 |
| S2 | Plans + monthly/yearly payment via Razorpay Subscriptions | P0 |
| S3 | Message credits counter (WhatsApp costs you money) | P0 |
| S4 | Grace period then read-only lock on non-payment | P0 |
| S5 | GST invoice to clinic | P0 |

### 3.8 Platform admin
| ID | Requirement | P |
|----|-------------|---|
| A1 | List clinics, plan, status, last active, messages used | P0 |
| A2 | Impersonate clinic (read-only) for support | P1 |
| A3 | Extend trial / give credits manually | P0 |

---

## 4. Non-Functional Requirements

| Area | Target |
|------|--------|
| Performance | Booking page loads < 2 s on 4G; dashboard actions < 300 ms |
| SEO | Public profile pages server-rendered; Lighthouse SEO score ≥ 90; images resized + lazy-loaded |
| Availability | 99.5% (clinic hours 8:00–22:00 IST matter most) |
| Scale (year 1) | 500 clinics, ~50k bookings/month — a single server handles this |
| Mobile-first | Every screen usable at 360 px width |
| Concurrency | No double-booking of a slot (DB unique constraint + row lock) |
| Time zone | Store UTC, display IST; schedule logic in clinic's zone |
| Language | English UI in MVP; patient messages in English + Hindi templates (P1) |
| Accessibility | Large tap targets, readable contrast (receptionists use it all day) |

---

## 5. Security, Privacy & Compliance

Health data is sensitive. Get this right from day one.

- **DPDP Act 2023 (India):** collect only what's needed, show a consent line on booking
  ("Your details are shared with <clinic> for this appointment"), allow data deletion on request,
  publish a privacy policy. The clinic is the data fiduciary; you are the processor — say this in
  your terms.
- **Tenant isolation:** every table has `clinic_id`; every query filters by the logged-in user's
  clinic. Add an automated test that tries to read another clinic's data.
- **Auth:** OTP login, JWT (short-lived access + refresh token), role-based access
  (OWNER, DOCTOR, RECEPTION, ADMIN).
- **Public endpoints:** rate-limit OTP + booking per IP and per mobile; CAPTCHA if abused.
- **Booking links** use random, unguessable IDs (not sequential numbers).
- **Encryption:** HTTPS only; RDS encryption at rest; S3 private with presigned URLs.
- **No PII in logs** (mask mobile numbers).
- **Backups:** daily automated DB snapshots, 7–30 day retention; test a restore once a month.
- **Not a medical device:** no diagnosis or prescription logic in MVP — keeps you out of
  medical-device regulation.
- **ABDM (Ayushman Bharat Digital Mission)** integration is optional — P2, only if clinics ask.
- **Truthful profiles:** never show "verified" unless W8's check was done and recorded; no invented
  testimonials, awards, outcomes or patient counts. Follow NMC rules on doctor advertising.
- **Public vs private data:** profile fields are public by design; patient and appointment data never
  appear on public pages or in URLs.
- **Uploads:** accept only JPEG/PNG/WebP, max 5 MB, re-encode server-side (strips EXIF/location).
- Get a lawyer to review privacy policy, consent text and terms before launch — this plan is not
  legal advice.

---

## 6. Tech Stack

Chosen to match existing skills (Angular + Spring Boot + AWS) and stay cheap.

| Layer | Choice | Why |
|-------|--------|-----|
| Frontend | **Angular 22** (standalone components) + **Tailwind CSS** | You know it; one codebase for dashboard + public pages |
| Public page rendering | **Angular SSR** (`@angular/ssr`), hybrid: public profile/booking routes server-rendered, dashboard client-only | Google and WhatsApp link previews need real HTML. Frontend container then runs Node instead of plain nginx |
| Mobile | **PWA** (installable, home-screen icon) | No app store work; native apps only if clinics demand |
| Backend | **Spring Boot 3, Java 21** | Known stack; virtual threads for notification fan-out |
| Database | **PostgreSQL 16** (AWS RDS, or a managed provider) | Reliable, good JSON + constraints |
| Migrations | **Flyway** | Simple versioned SQL |
| Scheduled jobs | Spring `@Scheduled` + **ShedLock** (DB-based lock) | Reminders without Redis/queues in MVP |
| Queue (later) | AWS SQS | Only when message volume needs it |
| Live queue updates | Polling every 5 s (MVP) → Server-Sent Events (P1) | Polling is enough at this scale |
| WhatsApp | **Meta WhatsApp Cloud API** direct, or a BSP (Gupshup / Interakt / AiSensy) | Direct is cheapest; BSP is easier to get approved |
| SMS fallback / OTP | MSG91 or similar (DLT-registered templates required in India) | |
| Payments | **Razorpay** (Subscriptions for your billing, Payment Links for patient fees) | |
| File storage | AWS S3 (private) + CloudFront for public profile images | QR posters, logos, doctor/clinic photos, reports |
| PDF | OpenPDF / Flying Saucer | QR posters, invoices |
| Auth | Spring Security + JWT | |
| Hosting (MVP) | One **AWS Lightsail / EC2** instance in `ap-south-1` running Docker Compose, + RDS | ~₹3–5k/month total |
| Hosting (scale) | ECS Fargate + ALB + RDS Multi-AZ | After ~200 clinics |
| CI/CD | GitHub Actions → build Docker image → deploy | |
| Monitoring | Spring Actuator + UptimeRobot (free) + Sentry (errors) | |
| Email | AWS SES | Invoices, admin alerts |
| Analytics | PostHog or Plausible | Funnel: link opened → booked |

---

## 7. Architecture

```
          Patient (phone)                    Clinic staff (phone / desktop)
                │                                         │
     Public booking + queue page                 Dashboard (Angular PWA)
                │                                         │
                └──────────────┬──────────────────────────┘
                               │ HTTPS
                     ┌─────────▼──────────┐
                     │  Spring Boot API   │  (monolith, modular packages)
                     │  - auth            │
                     │  - clinic/schedule │
                     │  - booking/queue   │
                     │  - patient         │
                     │  - notification    │──► WhatsApp Cloud API / SMS
                     │  - billing         │──► Razorpay (+ webhooks back)
                     │  - scheduler jobs  │
                     └───┬───────────┬────┘
                         │           │
                  PostgreSQL       S3 (files)
```

**Rules**
- One deployable **modular monolith**. Do not start with microservices.
- Package by feature: `auth`, `clinic`, `schedule`, `booking`, `queue`, `patient`,
  `notification`, `billing`, `report`, `admin`.
- Controller → Service → Repository. DTOs at the API edge.
- All outgoing messages go through a `notification_outbox` table, sent by a scheduled job
  → retries, no lost messages, easy audit.
- Razorpay/WhatsApp webhooks are **idempotent** (store event id, ignore repeats).

---

## 8. Data Model (core tables)

```
clinic            (id, slug UNIQUE, name, address, phone, whatsapp, map_url, logo_key, timezone,
                   accent_color, template, about, seo_title, seo_description,
                   published_at NULL,                  -- NULL = unpublished, page returns 404
                   plan, plan_status, trial_ends_at, message_credits, created_at)
app_user          (id, clinic_id, mobile, name, role, active)
doctor            (id, clinic_id, slug, name, specialty, fee, booking_mode[SLOT|TOKEN|REQUEST], active,
                   photo_key, qualifications, registration_no, registration_council,
                   experience_years, languages, bio, registration_checked_at NULL)
                   UNIQUE(clinic_id, slug)
media             (id, clinic_id, s3_key, kind[CLINIC_PHOTO|DOCTOR_PHOTO|LOGO], sort_order)
page_event        (id, clinic_id, doctor_id NULL, type[VIEW|CALL_CLICK|WA_CLICK|BOOK_START], at)
                   -- no IP, no patient data; aggregate daily if it grows
service           (id, clinic_id, slug, name, duration_min, fee, active)
                   UNIQUE(clinic_id, slug)
doctor_service    (doctor_id, service_id)              -- which doctor offers which service
slug_history      (id, entity_type, entity_id, old_slug, clinic_id NULL)
                   -- old links / printed QR posters keep working (301 to new slug)
wa_verification   (id, code UNIQUE, mobile NULL, status[PENDING|VERIFIED|EXPIRED],
                   return_path, expires_at, verified_at)
schedule_rule     (id, doctor_id, day_of_week, start_time, end_time, slot_minutes,
                   max_bookings, walkin_reserve_pct)
schedule_block    (id, doctor_id, date, start_time NULL, end_time NULL, reason)  -- leave/holiday
session           (id, doctor_id, date, start_time, end_time, current_token, status)
                   -- materialized per day from rules; makes queue logic simple
patient           (id, clinic_id, mobile, name, age, gender, created_at)
appointment       (id, clinic_id, session_id, patient_id, service_id NULL, token_no, slot_time NULL,
                   source[ONLINE|WALKIN|PHONE], status, booked_at, arrived_at, done_at,
                   public_ref)
                   UNIQUE(session_id, token_no)
                   UNIQUE(session_id, slot_time) WHERE slot_time IS NOT NULL
payment           (id, appointment_id, amount, mode[CASH|UPI|CARD|ONLINE], razorpay_id, paid_at)
visit_note        (id, appointment_id, note, follow_up_date)
otp               (id, mobile, code_hash, purpose, expires_at, attempts)
notification_outbox (id, clinic_id, appointment_id, channel, template, payload_json,
                   status, attempts, send_after, sent_at, provider_msg_id)
subscription      (id, clinic_id, razorpay_sub_id, plan, status, current_period_end)
invoice           (id, clinic_id, number, amount, gst, pdf_key, issued_at)
webhook_event     (id, provider, event_id UNIQUE, received_at, payload_json)
audit_log         (id, clinic_id, user_id, action, entity, entity_id, at)
```

Every tenant table carries `clinic_id` and an index on it.

---

## 9. API Outline

**Public (no auth, rate-limited)**
```
GET  /api/public/resolve?path=/{clinic}/{doctor|services/x}
                                                     → clinic, services, doctors, next step
                                                       (or 301 target for an old slug)
GET  /api/public/clinics/{slug}/doctors/{id}/availability?date=&service=
POST /api/public/wa-verify                           {returnPath} → {code, waLink}
GET  /api/public/wa-verify/{code}                    poll → PENDING | VERIFIED (+ booking token)
GET  /v/{code}                                       return link sent in WhatsApp reply
POST /api/public/otp/send | /otp/verify              SMS fallback
POST /api/public/appointments                        book
GET  /api/public/appointments/{publicRef}            status + live queue position
POST /api/public/appointments/{publicRef}/cancel
POST /api/public/appointments/{publicRef}/reschedule
POST /api/public/events                              {clinic, doctor?, type} page-view / click stats
```

**Clinic (JWT, role-checked, clinic-scoped)**
```
POST /api/auth/otp/send | /verify | /refresh
GET/PUT  /api/clinic                                 profile & settings
POST     /api/clinic/publish | /unpublish
GET      /api/clinic/completeness                    checklist of missing profile items
POST     /api/media/upload-url                       presigned S3 upload; DELETE /api/media/{id}
GET      /api/reports/page-stats?from=&to=           views, clicks, bookings (W7)
CRUD     /api/doctors, /api/doctors/{id}/schedule-rules, /api/doctors/{id}/blocks
CRUD     /api/staff
GET      /api/sessions?date=                         today's sessions
GET      /api/sessions/{id}/queue
POST     /api/sessions/{id}/walk-ins
POST     /api/sessions/{id}/call-next
PATCH    /api/appointments/{id}/status
POST     /api/appointments/{id}/payments
POST     /api/sessions/{id}/broadcast-delay
GET      /api/patients?q=   GET /api/patients/{id}/visits
POST     /api/appointments/{id}/note
GET      /api/reports/daily?date=   /monthly?month=   /export
GET      /api/clinic/qr-poster.pdf
GET/POST /api/billing/plan, /api/billing/subscribe, /api/billing/invoices
```

**Webhooks**
```
POST /api/webhooks/razorpay        (verify signature)
POST /api/webhooks/whatsapp        (delivery status, inbound replies)
```

**Admin:** `/api/admin/clinics`, `/api/admin/clinics/{id}/credits`, `/trial`.

---

## 10. Key Logic (get these right)

1. **No double booking:** booking runs in a transaction; lock the `session` row
   (`SELECT … FOR UPDATE`), compute next token / check slot free, insert; unique constraints are
   the final safety net.
2. **Session generation:** nightly job creates `session` rows for the next 14 days from
   `schedule_rule` minus `schedule_block`. Editing rules regenerates future sessions that have no
   bookings; warn if bookings exist.
3. **Walk-in reserve:** online booking stops when `online_count >= max * (1 - reserve%)`.
4. **Wait-time estimate:** rolling average of actual consultation time for that doctor today
   (fallback: slot length) × patients ahead.
5. **Reminders:** on booking, insert outbox rows with `send_after` (evening before, 2 h before);
   cancel them if the appointment is cancelled.
6. **"Turn near":** on `call-next`, notify the patient 2 positions behind.
7. **Message credits:** decrement on send; at 0, fall back to "no reminders" and alert owner —
   never silently fail.
8. **OTP (SMS fallback):** 6 digits, hashed, 5-minute expiry, max 5 attempts, max 3 sends per
   mobile per 15 min.

---

## 10A. Booking Links & Patient Flow

### URL structure
Every clinic, doctor and service gets a readable slug. Slugs are generated from the name,
editable by the owner, lowercase, `a-z0-9-` only.

| Link | Example | Opens |
|------|---------|-------|
| Clinic | `samaysar.in/apollo-dental-indore` | Clinic page → service / doctor choice |
| Doctor | `samaysar.in/apollo-dental-indore/dr-amit-sharma` | That doctor directly |
| Service | `samaysar.in/apollo-dental-indore/services/teeth-cleaning` | That service → doctors offering it |
| Doctor + service | `samaysar.in/apollo-dental-indore/dr-amit-sharma?service=teeth-cleaning` | Straight to slots |

Rules
- Clinic slug is unique across the platform; add the city on clash (`apollo-dental-indore`).
- Doctor and service slugs are unique within the clinic.
- Reserved words can't be clinic slugs: `api`, `admin`, `login`, `app`, `v`, `services`, `help`, …
- Renaming keeps the old slug in `slug_history` and 301-redirects, so printed QR posters never break.
- Each link has its own QR code in the dashboard (clinic poster, per-doctor card, per-service).

### Dynamic steps (skip anything with only one option)
```
open link
  │
  ├─ service known from URL?          no → clinic has 1 active service? yes → auto-pick
  │                                         no → show service list
  ├─ doctor known from URL?           no → only 1 doctor offers that service? yes → auto-pick
  │                                         no → show doctor list (filtered by service)
  ▼
verify number on WhatsApp  →  pick date + slot/token  →  name/age (prefilled if returning)  →  done
```
- **Single-doctor, single-service clinic:** link opens straight to "Verify on WhatsApp".
- Service chosen first filters doctors; doctor chosen first filters services.
- Slot length = service duration if set, else the doctor's slot length.
- Selections stay in the URL (`?service=…&doctor=…`) so back button and refresh work.

### WhatsApp tap-to-verify (no OTP typing)
```
1. Web: POST /wa-verify → code "CQ-7KX4P2" (random, 10-min expiry)
2. Web shows button → opens
   https://wa.me/91XXXXXXXXXX?text=Verify%20my%20number%20CQ-7KX4P2
3. Patient taps Send in WhatsApp (message comes FROM their number = proof of ownership)
4. WhatsApp webhook → find code → store sender's mobile → status VERIFIED
5. Bot replies: "✅ Verified. Tap to continue booking: samaysar.in/v/CQ-7KX4P2"
6a. Same phone: patient taps link → back on web with booking token → slot page
6b. Web page also polls GET /wa-verify/{code} every 2 s → auto-continues if the tab is still open
```
Why this way
- Patient never types a number or OTP; one tap to send.
- Patient-initiated message opens a free 24-hour service window, so the reply link and the
  booking confirmation cost little or nothing (check Meta's current pricing).
- The same chat becomes the channel for reminders.

Edge cases
- **Desktop / no WhatsApp:** show "Use SMS instead" → normal SMS OTP.
- **Code expired or reused:** reply "Link expired, tap here to start again" with the clinic link.
- **Message edited / code removed:** bot replies asking to use the button again.
- **Someone forwards the code:** the verified mobile is whoever sent it — acceptable, since the
  booking goes to that number.
- **Booking token** (JWT, 30 min) is bound to the verified mobile + clinic; required for booking.
- Rate-limit code creation per IP; codes are single-use.

Alternative order (decide during pilot): pick slot first, verify last. It lowers drop-off before
patients see availability, but needs a 10-minute slot hold. Start with verify-first (simpler).

---

## 11. Pricing & Business Model

| Plan | Price (suggested) | Includes |
|------|-------------------|----------|
| **Trial** | Free, 14 days | Everything, 100 messages |
| **Solo** | ₹499/month | 1 doctor, 600 messages/month |
| **Clinic** | ₹999/month | Up to 3 doctors, 1,500 messages, reports, follow-ups |
| **Multi** | ₹2,499/month | Up to 10 doctors, TV display, branches |
| **Profile** (entry, to test) | ₹249/month | Website/profile page, call + WhatsApp buttons, booking *requests* (B9); no automated messages, so almost no running cost |
| Add-on | ~₹199 per 500 extra messages | |

**Competitor prices to beat** (provider-advertised, seen Oct 2026 — re-check before deciding):
DrPage ₹299–499/month + GST · VaidyaX from ~₹1,666/month (annual) · CuraSuite from ~₹9,999/year ·
DocIndia free first year then ₹6,000/year. They sell *websites*; Samaysar sells **fewer no-shows and a
calmer waiting room**, with the website included. Lead with that.

- Yearly = 2 months free (improves cash flow and retention).
- Patient online-payment: optional convenience fee or small margin later.
- **Check your unit cost:** WhatsApp utility/authentication message prices change — check Meta's
  current India rate card before fixing plan limits. Keep gross margin > 70%.
- Break-even: ~10 paying clinics cover hosting + messaging; 100 clinics ≈ ₹1 lakh MRR.

---

## 12. Project Phases & Timeline

Assumes one developer, part-time evenings/weekends (~20 h/week). Halve if full-time.

### Phase 0 — Validate (Week 0–2) — *don't skip*
- Visit 15–20 clinics nearby. Ask: how do you book today? how many no-shows? would you pay
  ₹500/month for reminders + queue?
- Show a clickable Figma/HTML mockup of booking page + reception screen, plus 2–3 demo profile pages
  (fictional doctors, clearly labelled "demo").
- Ask: How do new patients find you? How do appointments arrive (phone / WhatsApp / walk-in /
  Practo)? Do you have a website — cost, who maintains it? What happens with missed calls after
  hours? Who decides on software and what monthly budget feels fine? What would make you cancel?
- Try 30 minutes on DrPage / DocIndia yourself so you know what doctors compare you to.
- Ask pilots for a small deposit or paid pilot — "yes I'd pay" is not proof.
- **Exit criteria:** ≥ 5 clinics say yes, 2–3 agree to be pilot users.
- Register: domain, business entity/GST (or start as sole proprietor), WhatsApp Business
  account + Meta business verification (takes days–weeks — **start now**), SMS DLT registration.

### Phase 1 — Foundation (Week 3–4)
- Repo, Spring Boot + Angular skeleton, Docker Compose, Flyway, CI pipeline.
- Auth (OTP + JWT + roles), clinic signup, clinic profile, tenant filter + isolation test.
- Deploy to a staging server on day 1 of this phase.

### Phase 2 — Scheduling & Booking (Week 5–7)
- Doctors, schedule rules, blocks, session generation job.
- Public booking page (slot + token modes), OTP verify, confirmation page.
- Public profile page on the same route (W1–W5): add Angular SSR, profile editor, photo upload,
  publish/unpublish, completeness checklist.
- Concurrency test: 50 parallel bookings on one slot → exactly one succeeds.

### Phase 3 — Reception & Queue (Week 8–9)
- Today's queue, walk-ins, status flow, call-next, payments, patient search.
- Live queue page for patients (polling).

### Phase 4 — Notifications (Week 10–11)
- Outbox + sender job, WhatsApp templates (submit for approval early), SMS fallback.
- Confirmation, reminders, turn-near, cancel/reschedule links.

### Phase 5 — Billing, Reports & Admin (Week 12–13)
- Razorpay subscriptions + webhooks, trial/lock logic, GST invoice PDF, message credits.
- Daily report, QR poster PDF, admin panel.
- Privacy policy, terms, consent text.

### Phase 6 — Pilot (Week 14–17)
- 2–3 real clinics, free. Sit in the clinic for a morning and watch the receptionist use it.
- Concierge setup: you fill in their profile, photos and schedule yourself; time how long it takes
  — that's your future onboarding cost.
- Fix the top 10 pain points. Measure no-show rate before vs after (your sales pitch).

### Phase 7 — Launch (Week 18+)
- Convert pilots to paid. Target 20 paying clinics in 3 months.
- Then P1 items: follow-ups, delay broadcast, TV mode, review requests, monthly reports, Hindi.

---

## 13. Testing Strategy

| Type | What | Tool |
|------|------|------|
| Unit | Slot/token calculation, wait estimate, reserve logic, OTP rules | JUnit 5 + Mockito |
| Integration | Booking concurrency, tenant isolation, webhook idempotency | Testcontainers (Postgres) |
| API | Every endpoint happy path + auth failure | Spring MockMvc / RestAssured |
| Frontend | Booking flow, queue screen | Jest/Karma + Playwright E2E |
| Manual | Real phones, slow 3G throttling, WhatsApp delivery | Pilot clinics |

Must-have automated tests before launch: double-booking, cross-clinic data access, payment
webhook replay, OTP brute-force limit.

---

## 14. Deployment & Operations

- Environments: `local`, `staging`, `prod`. Separate DBs and WhatsApp test numbers.
- Secrets in AWS Secrets Manager / SSM Parameter Store — never in git.
- Deploy: GitHub Actions → Docker image → ECR → pull & restart (zero-downtime later with ALB).
- Daily DB snapshot + weekly restore test.
- Alerts: API down, error spike (Sentry), outbox backlog > N, Razorpay webhook failures.
- Status page (free tier) so clinics know when something is down.
- Support: a WhatsApp number for clinics; reply within 1 hour in clinic hours.

**Monthly running cost (MVP, approx.)**
| Item | Cost |
|------|------|
| Server (Lightsail/EC2 small) | ₹1,000–2,500 |
| Managed Postgres (small) | ₹1,500–3,000 |
| Domain + email | ₹100–300 |
| WhatsApp + SMS | Pay-per-message (pass through via plans) |
| Sentry / UptimeRobot / PostHog | Free tiers |

---

## 15. Go-To-Market

- **Local first:** walk into clinics in 2–3 neighborhoods; doctors trust face-to-face.
- **Proof:** "Dr. X cut no-shows from 18% to 6%" — collect this from pilots.
- **Free QR poster** for the waiting room → patients scan → every patient sees your brand.
- **Referral:** 1 month free for each referred clinic.
- **Channels later:** pharma medical reps, local IMA/dental association events, Google Business
  Profile "Book" button link, Instagram reels showing the queue screen.
- **Niches that adopt fastest:** dentists, pediatricians, physiotherapists, dermatologists,
  eye clinics.

---

## 16. Success Metrics

| Metric | Target (6 months) |
|--------|------------------|
| Paying clinics | 50 |
| Monthly churn | < 4% |
| Online bookings / clinic / month | > 150 |
| No-show rate reduction | ≥ 40% vs baseline |
| Booking page conversion (open → booked) | > 60% |
| Message delivery success | > 97% |
| Activation (signup → profile published + first booking) | > 60% within 72 h |
| Time to value (signup → published page) | < 15 min self-serve |
| Patient funnel (page view → call/WA click or booking started → booked) | Tracked per clinic (W7) |

---

## 17. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| WhatsApp Business approval delayed | Start in Phase 0; SMS fallback ready |
| Doctors won't change habits | Reception-first design; walk-ins still work; free pilot |
| Message costs eat margin | Credits per plan; monitor cost per clinic |
| Competitors (Practo, Eka Care, DrPage, VaidyaX, CuraSuite, DocIndia) | Be simpler, local, WhatsApp-native, faster support; sell the queue + no-show reduction, not "a website" |
| Doctors expect the website to bring new patients | Don't promise it; give SEO basics and show real page-view/click numbers (W7) |
| Fake credentials or reviews | Manual registration check before any badge (W8); no on-site reviews, only Google review link |
| Data breach | Tenant isolation tests, encryption, minimal data, no PII in logs |
| Single developer bus factor | Infra as code, documented runbook, automated backups |
| Internet outage at clinic | PWA caches today's list; reception can still view queue |

---

## 18. Pre-Build Checklist

- [ ] 5+ clinics validated, 2–3 pilots committed
- [ ] Product name + domain bought
- [ ] Business registration / GST decision
- [ ] Meta Business verification + WhatsApp number applied
- [ ] SMS DLT entity + templates registered
- [ ] Razorpay account (KYC done)
- [ ] AWS account, billing alarm set
- [ ] Privacy policy + terms drafted
- [ ] Figma/HTML mockups reviewed by a real receptionist
- [ ] GitHub repo + CI created

---

## 19. Repo Structure (suggested)

```
clinic-appointment/
├── backend/                  Spring Boot
│   └── src/main/java/com/clinicq/
│       ├── auth/ clinic/ schedule/ booking/ queue/
│       ├── patient/ notification/ billing/ report/ admin/
│       └── common/ (security, tenant filter, errors)
│   └── src/main/resources/db/migration/   Flyway SQL
├── frontend/                 Angular + Tailwind
│   └── src/app/
│       ├── public/ (booking, queue status)
│       ├── dashboard/ (queue, patients, reports, settings)
│       └── admin/
├── infra/                    docker-compose, CDK/Terraform later
├── docs/                     this plan, runbook, API notes
└── .github/workflows/
```

---

## 20. Next Steps

1. Do Phase 0 this week: list 20 clinics nearby and visit them.
2. Apply for WhatsApp Business + SMS DLT in parallel (slowest external dependency).
3. Build the two mockups (booking page, reception queue) and show them.
4. Start Phase 1 only after ≥ 5 clinics say they'd pay.
