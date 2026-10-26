# Practo — public feature inventory

Captured 2026-10-26 from public pages only (no login, no forms, no booking/payment).
Sources: user-driven Chrome session observed read-only via REA + one same-URL reload of the search
page to record request URLs (no headers, bodies, cookies or storage). Bundles analysed statically
(never executed): `client.f3bef02e5898.js`, `listing-page.4504dcfed755.js`, `practonav.js` (9.5.21)
from `www.practostatic.com`.

Pages covered: home (`/doctors`), doctor search (`/search/doctors`, oncologist/Hyderabad),
online consult (`/consult`). **Not captured:** a doctor profile, a clinic page, lab tests,
medicines, Ray provider page — their features below come from bundle strings and footer links only.

Evidence: **UI** = visible on page (accessibility tree) · **API** = request observed on reload ·
**Bundle** = string in shipped JS (exists in code, not proven used).

## Patient side

| Feature | Evidence | URL / endpoint | Confidence |
|---|---|---|---|
| Doctor search by specialty + city/locality, "near me", current location | UI, API | `/search/doctors?results_type&q&city`; `/marketplace-api/dweb/search/provider/v2` | high |
| Search clinics / hospitals separately | Bundle, UI footer | `/marketplace-api/dweb/search/clinic`, `/search/hospital`, `/listing/clinic/v2`, `/listing/hospital/v2` | med |
| Filters: availability (4 h / today / tomorrow / 7 days), gender, experience, fee | UI | search page filter options | high |
| Sort: patient stories, experience, fee low↔high | UI | search page sort options | high |
| Locality chips (Gachibowli, Hitech City, …) | UI | search page | high |
| Doctor card: specialty, years exp., fee, % recommended, patient-stories count, "Available Today", clinic name | UI | search results | high |
| **"Book Clinic Visit — No Booking Fee"** | UI | search results button | high |
| "Contact Clinic / Contact Hospital" (lead/call instead of booking) | UI, API | `/marketplace-api/dweb/provider/cta-info` (called on load) | high |
| Slot selection with "slots left" count | Bundle | `slot_selection_component`, `slots_left_count`, `slot_available_count` | med |
| **Max wait-time guarantee** ("Max N mins wait", Prime doctors) | Bundle | `wait_time`, `is_practo_prime` → `"Max … mins wait"` | med |
| Doctor profile: media, surgeries, feedback, locations, practice doctors | Bundle | `/marketplace-api/dweb/profile/provider[/v2,/feedback]`, `/dweb/doctor-locations`, `/dweb/practice-doctors`, `/dweb/doctor-media` | med |
| Clinic/hospital profile: media, procedures, groups, doctors, feedback | Bundle | `/marketplace-api/dweb/profile/establishment[/feedback,/groups]`, `/dweb/profile/establishment/procedures` | med |
| Reviews ("patient stories"), recommendation %, recommended reviews, review moderation | UI, Bundle | `show_recommended_reviews`, `/wave/reviewcontexts/decencychecks`, `/wave/surveys`, `/wave/feedbacksurveys/` | high (UI) / med |
| Post-visit feedback survey with OTP verification | Bundle | `/wave/sendotp`, `/wave/verifyotp`, `/wave/surveys/feedbackexists` | med |
| Online consult: instant (≤2 min) or scheduled; audio/chat on web, **video app-only** | UI | `/consult` | high |
| Consult: digital prescription + **7-day free follow-up** | UI | `/consult` | high |
| 25+ specialties, symptom-based entry ("Common health concerns") | UI | `/consult` | high |
| Free public health Q&A ("Ask a …") | UI, Bundle | `/consult` links; `/marketplace-api/dweb/search/questions` | high |
| Surgeries (Piles, Hernia, 50+) with lead form + cost estimate | UI, Bundle | `/marketplace-api/lead/submit-lead`, `/lead-form-rule-engine`, `/mweb/procedure/procedure-cost` | med |
| Insurance: providers accepted / insurance landing | Bundle | `insurance_providers`, `/insurance/:id` | med |
| Cashback / discounts on listing cards | Bundle | `cashback_details`, `discount_value` | low |
| Lab tests, full-body checkups | UI (links) | `/tests`, `/health-checkup-packages/master` | med |
| Order medicines | UI (links) | footer / nav | high (exists) |
| Medical records in app ("records accessible only by you") | UI | home page | high (claim) |
| Health articles / Health Feed, doctor videos | UI, API | `/healthfeed`, `/content/doctor/paginated/posts`, HLS video from `video1.practo.com` | high |
| Practo Plus (membership), Corporate plans | UI, Bundle | `/plus`, `/plus/corporate` | med |
| Payment status page | Bundle | `/payment/status` | low |
| App download link by SMS | UI, Bundle | "Send app link", `/marketing/sendapplinksms` | high |

## Booking flow (observed 2026-10-26, stopped before OTP — nothing booked)

Booking is a separate app (`practostatic.com/web-assets/appointment/en-*.js`) on
`/appointment/{doctor-slug}/{practice_doctor_id}/book`, backed by GraphQL `POST /client-api/v3/gql`.

| Step / feature | Evidence | URL / endpoint | Confidence |
|---|---|---|---|
| 1. Pick slot on search/profile card → opens booking page in new tab | UI | `/appointment/{slug}/{id}/book` | high |
| 2. Summary: "In-clinic Appointment · On {date} · At {time}", "Change Date & Time", "Go back to my results" | UI | booking page | high |
| 3. **Mobile number only** → OTP ("We will send appointment-related communications on this number") | UI, Bundle | `otp_requested`, `otp_verified` | high |
| 4. Patient details form driven by server config (name, email, relation) | Bundle | `/marketplace-api/dweb/book/form/config/v2`, `form_config`, `/health/api/form/` | med |
| **Book for family member** (relation picker) | Bundle | `relation_id`, `relations` | med |
| WhatsApp consent checkbox on booking (edited vs initial state tracked) | Bundle | `consent_given_initial/edited`, `/wave/api/whatsapp/consent` | med |
| CTA: **"Confirm FREE Clinic Visit"** vs **"Pay & Confirm Clinic Visit"** (prepaid optional) | Bundle | `cta_texts`, `prepaid_type`, `payable_amount`, `payment_summary` | med |
| Booking errors handled: `slot_unavailable`, `appointment_exist`, `limit_exceeded`, `time_exceeded`, **`pns`** | Bundle | booking error switch | med |
| **No-show policy ("PNS")** — repeat no-shows blocked from booking, shown a call-the-clinic path; policy page | Bundle | `booking_status → "pns"`, `show_pns_call`, `/company/pns-policy` | med |
| Cancellation / no-show fee on prepaid ("a fee of Rs X … for cancellation or no-show"), % or fixed | Bundle | `cancellation_fee`, `cancellation_fee_type: PERCENTAGE` | med |
| **Wait-time guarantee** ("{N} mins or less wait time") + guarantee claim page; post-visit wait-time feedback | Bundle | `wait_time_guaranteed`, `/company/guarantee-claim`, `wait_time_feedback_state` | med |
| Reschedule / cancel from confirmation | Bundle | `reschedule_url`, `cancel_appointment_sucess` | med |
| Upcoming appointments for logged-in user | Bundle | `/health/api/appointments_upcoming/active`, `/health/api/appointments` | med |
| Upsell membership (Practo Plus) after booking; lead capture | Bundle | `/subscriptions/api/v1/planupsell`, `/subscriptions/api/v1/leads`, `/marketplace-api/leadsquared/create-lead-activity` | med |

## Provider / clinic side (from public pages)

| Feature | Evidence | URL / endpoint | Confidence |
|---|---|---|---|
| **Ray by Practo** — clinic management software (+ Ray Tab) | UI, Bundle | `/providers/clinics/ray` | high (exists) |
| **Qikwell by Practo** — hospital queue / appointment management | UI, Bundle | `/providers/hospitals/qikwell` | high (exists) |
| Insta by Practo — hospital management system | UI, Bundle | `/providers/hospitals/insta` | high (exists) |
| Practo Profile — doctor/clinic/hospital listing management | UI, Bundle | `/providers/doctors/profile`, `/providers/clinics/profile` | high |
| **Practo Reach** — paid/sponsored listings | UI, Bundle | `/providers/clinics/reach`, `/marketplace-api/dweb/reach`, `/reach/cards` | high |
| Practo Prime — verified clinic program (wait-time promise) | UI, Bundle | `/providers/prime`, `prime_program` | med |
| Practo Consult / Health Feed for doctors | UI, Bundle | `/providers/doctors/consult`, `/providers/doctors/health-feed` | high |
| ABDM (Ayushman Bharat) integration page | Bundle | `/providers/abdm` | med |
| Practo Pro (doctor app), Practo Drive | UI (links) | footer | med |
| Doctor registration master list | Bundle | `/health/core-api/master/doctorregistrations.json` | low |

## Platform

| Feature | Evidence | URL / endpoint | Confidence |
|---|---|---|---|
| Login: phone **OTP**, **Truecaller**, **Google One Tap** | Bundle, UI | `otp_modal`, `truecallerLogin`, `_initGoogleOneTapLogin`; "Login / Signup" | med |
| WhatsApp consent at login; WhatsApp chat number | Bundle | `is_login_whatsapp_consent_given`, `/wave/api/whatsapps/consents` | med |
| Logged-in check on every page | API | `GET /logged_in_user` | high |
| GraphQL + REST APIs | Bundle | `/client-api/v3/gql`, `/client-api/v1`, `/marketplace-api/*`, `/consult/api/v1` | med |
| A/B experiments framework | Bundle | `UPDATE_EXPERIMENTS`, `doctor_listing_experiment`, `pla_availability_experiment`, `book_experiment` | med |
| SEO landing pages per specialty/locality, clinic & hospital SEO, healthcare directory | Bundle, UI | `/search/provider-seo/v2`, `/provider-seo-landing`, `/listing/clinic-seo/v2`, "Healthcare Directory", `/bluebook/india` | med |
| Multi-country: India, Singapore, Philippines, Indonesia, Brazil | UI, Bundle | footer; `/en-sg/`, `/en-ph/`, `/en-id/` | high |
| Own event pipeline | API | `POST events.practo.com/v1/events` | high |
| Engagement/push: CleverTap | API | `in.clevertap-prod.com`, `practostatic.com/pel/clevertap` | high |
| Analytics/ads: GA4 + UA, GTM, DoubleClick, Facebook Pixel, Bing UET, MS Clarity, OpenAI pixel | API | requests observed on reload | high |
| Monitoring: Sentry (self-hosted), New Relic, Akamai mPulse, SpeedCurve | API | `sentry.practo.com`, `bam.nr-data.net`, `go-mpulse.net`, `cdn.speedcurve.com` | high |
| Cookie consent banner: CookieYes | API | `cdn-cookieyes.com` | high |
| Bot protection: Akamai Bot Manager (blocks headless browsers) | API | `/_sec/verify`, obfuscated sensor scripts | high |
| Security claims: 256-bit encryption, "verified doctors" | UI | home, consult | high (claim only) |

## Features relevant to a clinic appointment MVP (ranked)

1. **Fast search → "Book Clinic Visit" with visible availability** ("Available Today", slots-left). Samaysar equivalent: availability on the public page (B1, B7).
2. **No booking fee for patients.** Patients compare on this; keep Samaysar booking free for patients.
3. **Wait-time promise** ("Max N mins wait"). Samaysar's live queue + wait estimate (§10.4) is a stronger version — make it a headline.
4. **Contact clinic / call / WhatsApp fallback** when online booking is off. Matches W2 + B9 request mode.
5. **Phone OTP login + WhatsApp consent at sign-in.** Matches B2; record consent explicitly (DPDP).
6. **Post-visit feedback with % recommended.** Samaysar: keep the Google-review link (P1) and a private rating for the clinic; skip a public review system.
7. **Doctor/clinic profile with fee, experience, photos, timings.** = W1–W2.
8. **SEO landing pages per specialty + locality.** = W3/W6; later, `/{city}/{specialty}` pages once many clinics exist.
9. **Instant/scheduled online consult with free follow-up.** Out of MVP scope; note as P2.
10. Skip for MVP: sponsored listings (Reach), insurance, cashback, surgeries lead-gen, medicines, lab tests, multi-country.

Booking-flow lessons for Samaysar: slot first, then phone OTP (matches §10A "pick slot first,
verify last"); handle `slot_unavailable` / already-booked / daily-limit errors explicitly; a
**repeat no-show block** is cheap and directly attacks no-shows; let clinics show a wait-time promise.

**Gaps:** slot picker UI itself, profile and clinic pages not seen live; endpoint list is from strings, so
some may be unused or retired. Request purposes are inferred from names.
