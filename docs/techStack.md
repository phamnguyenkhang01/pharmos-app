# Pharmos v1 — Technology Stack

Adaptive web app (mobile phone + computer) for a pharmacy e-commerce domain.

## Frontend
- **React** (Next.js recommended for SSR/SSG, routing, and light BFF endpoints)
- **Styling**: Tailwind CSS + component library (shadcn/ui or MUI)
- **Adaptive strategy**: single responsive codebase + PWA (manifest + service worker) so it installs like an app on mobile without a separate native build
- **State/data**: TanStack Query (server state) + Zustand or Redux Toolkit (client state — cart, UI)

*Alternatives considered*: Vue 3 + Nuxt, SvelteKit

## Backend
- **Java + Spring Boot 3.x**
  - Spring Web / WebFlux
  - Spring Security
  - Spring Data JPA

*Alternatives considered*: Node.js + NestJS, Kotlin + Spring Boot, Python (Django/FastAPI), Go (Gin/Fiber)

## Databases
- **PostgreSQL** — primary system of record
  - Customers, orders, payments, refunds, returns, delivery, age verification
  - Chosen over MySQL for native UUID/JSON support and stronger relational/transactional guarantees
- **MongoDB** — secondary store
  - Product catalog variable attributes (dosage, form, strength, active ingredients, warnings — varies by category)
  - High-volume append-only event/audit logs (order status changes, delivery tracking, age-verification attempts)

*Rationale*: core order → payment → refund → return chain stays relational (multi-table ACID transactions, foreign keys). MongoDB is additive, not a replacement, for the parts of the domain that are naturally document-shaped or write-heavy/append-only.

## Supporting Services
| Concern | Technology |
|---|---|
| Caching / sessions | Redis |
| Product search & filtering | OpenSearch or MeiliSearch |
| File storage (proof photos, product images) | S3 or MinIO (self-hosted) |
| Auth | Spring Security + JWT, or Auth0 / Cognito / Keycloak |
| Payments | Stripe or Braintree (PCI scope stays with provider; app stores only `gateway_token` / `gateway_transaction_id`) |
| API style | REST (GraphQL only if client-specific query shapes become a real pain point) |
| Async / events | Spring Events or a lightweight queue (SQS / RabbitMQ) |
| Containerization | Docker + Docker Compose (local); ECS Fargate or EKS (prod) |
| CI/CD | GitHub Actions |

## Compliance Note
Schema includes `is_medication`, `is_restricted`, and `AGE_VERIFICATION` — plan for audit logging (who verified age, when, result) beyond standard e-commerce requirements.
