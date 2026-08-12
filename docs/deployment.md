# Deployment (personal-viewing only, not production)

Current state (2026-08-11): UC-01 (browse/search/filter products) is functionally
complete end-to-end, backend and frontend. There is **no auth, no checkout/orders,
no payment** — customers can look but not buy. The steps below are for standing up
a live URL you can look at yourself, not for real customer traffic. See the
"Keep it low-risk" section before sharing the link with anyone.

Chosen stack: **DigitalOcean App Platform** (backend) + a **self-hosted Postgres
droplet** (reusing the existing `docker-compose.yml`) + **Vercel** (frontend).
Picked over Railway/Render for familiarity with DO, and over DO's managed
Postgres (~$15/mo) to keep cost down (~$9-11/mo total instead of ~$20/mo) — this
is still a pre-auth prototype, not production, so managed-DB backups/HA aren't
worth paying for yet.

## DigitalOcean resources created so far

- Project: `pharmos` (region NYC1)
- Droplet: `ubuntu-s-1vcpu-512mb-10gb-nyc1` — $4/mo, Ubuntu 24.04, VPC `default-nyc1`
  - Public IPv4: `134.209.209.138`
  - Private IP: `10.116.0.2`
- VPC CIDR for `default-nyc1`: **`10.116.0.0/20`** (confirmed via Networking →
  VPC on 2026-08-12) — contains the droplet's private IP `10.116.0.2`. Use this
  exact range for firewall rule sources; an earlier guess of `10.10.0.0/16` was
  wrong and caused a live mistake mid-setup.

## Backend (`pharmos-api`)

1. **Externalize the datasource config.** ✅ Done — `application.properties` now
   reads `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` /
   `SPRING_DATASOURCE_PASSWORD`, falling back to the old hardcoded
   `localhost:5432` values when unset. No code changes needed to switch back
   to local dev.
2. **Stand up Postgres on the droplet.** ✅ Done — Docker installed via
   `curl -fsSL https://get.docker.com | sh`, existing `docker-compose.yml`
   copied over with `scp`, `docker compose up -d postgres` run (skipped
   `pgadmin` to avoid exposing it). Confirm with `docker ps` that it's healthy.
3. **Lock down the droplet's Postgres port — use the VPC, not a public IP
   allowlist.** ✅ Done. App Platform does **not** have a static
   outbound IP without the paid Dedicated Egress IP add-on, so "allow only
   App Platform's IP" isn't achievable for free. Instead:
   - Cloud firewall `pharmos-db-firewall` restricts inbound TCP `5432` to
     `10.116.0.0/20` (the `default-nyc1` VPC CIDR) only — not
     `All IPv4`/`All IPv6` — and is attached to the
     `ubuntu-s-1vcpu-512mb-10gb-nyc1` droplet. SSH (22) stays open to
     `All IPv4`/`All IPv6` as before; only 5432 is VPC-restricted.
   - The App Platform app (step 4) must be attached to the same
     `default-nyc1` VPC so it gets a private IP and can reach the droplet at
     `10.116.0.2:5432` — Postgres is then never reachable from the public
     internet at all, on any port.
4. **Deploy the Spring Boot app to DO App Platform.** ⬜ Not started. Basic
   tier (~$5/mo, no idle sleep). Source directory must be set to `pharmos-api`
   (monorepo). Env vars to set:
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://10.116.0.2:5432/pharmos`
   - `SPRING_DATASOURCE_USERNAME=pharmos`
   - `SPRING_DATASOURCE_PASSWORD=pharmos`
   - `PHARMOS_CORS_ALLOWED_ORIGINS=http://localhost:3000` (placeholder until
     the Vercel URL exists, see step 5)
5. **Update CORS in `SecurityConfig.java`.** ✅ Done — origins are now driven by
   `pharmos.cors.allowed-origins` (env var `PHARMOS_CORS_ALLOWED_ORIGINS`,
   comma-separated), defaulting to `http://localhost:3000`. Once the Vercel
   URL exists, update the App Platform env var to
   `http://localhost:3000,https://<vercel-url>` and redeploy.
6. **Seed some data.** ⬜ Not started. No admin UI exists yet, so this means a
   manual SQL insert or a temporary seed script against the droplet's DB —
   otherwise there's nothing to browse once it's live.

## Frontend (`pharmos-web`)

7. **Point the API client at the deployed backend URL.** ✅ Already done in
   code — `src/lib/api.ts` reads `NEXT_PUBLIC_API_BASE_URL` from env. Just
   needs the real value set in Vercel's project settings (step 8).
8. **Deploy to Vercel.** ⬜ Not started. Trivial for Next.js; free tier is
   enough, and its always-on CDN means no cold-start risk when showing this
   to someone live. Set `NEXT_PUBLIC_API_BASE_URL` to the App Platform app's
   URL from step 4.

## Keep it low-risk since there's no real auth

- Don't share the URL widely, or turn on Vercel's built-in password protection
  (Pro tier), or put the whole thing behind a simple Basic Auth reverse-proxy rule
  in front of the App Platform component.
- Actual exposure is low even if the link leaks: nothing beyond GET browsing works
  today (no checkout, no accounts), so worst case someone browses the seed products.
- Unlike Neon/Supabase free tiers, neither the droplet nor App Platform
  autosuspend on inactivity — no risk of a stale/paused DB when a customer
  clicks the link cold.

## Before this becomes real production

Not covered by the steps above — needed before any real customer/transaction
traffic:

- Real user auth (replace Spring Security's dev-mode random-password Basic auth)
- Cart → order flow + payment integration
- Admin capability to manage products/categories (no admin UI exists yet)
- Security review of `SecurityConfig.java` beyond the current GET-only permit list
- Compliance review — restricted-medication flag already exists in the data model,
  implying real regulatory requirements once actual purchases are involved
- CI/CD, monitoring, backups for the production database
