# PharmOS — V1 Use Case Specifications

**Product:** PharmOS — e-commerce web platform for a single pharmacy to sell OTC (over-the-counter, non-prescription) products and medical equipment directly to consumers.
**Initial target market:** Vietnamese community in America.
**Out of scope (v1):** prescription processing, insurance claims.

**Legend**
Complexity — S (small) / M (medium) / L (large)
Priority — P0 (must-have to launch) / P1 (launch-important) / P2 (can slip)

**Assumptions carried through these specs (confirm before build):**
1. Delivery uses a **third-party courier API** (not in-house drivers). If in-house, UC-13/15/16 grow significantly.
2. Payment is handled by a **third-party gateway** (Stripe or Square). PharmOS never stores raw card data.
3. Registration is **required** — no guest checkout in v1.
4. Age-restricted products (e.g. pseudoephedrine) are **only** in scope if UC-19 is built; recommended to defer to v2.

---

## Actors

| Actor | Type | Description |
|---|---|---|
| Customer | Primary (human) | End shopper; initial target is the Vietnamese community in America. Requires a registered account. |
| Pharmacy Admin | Primary (human) | Pharmacy owner/staff managing catalog, orders, discounts, and refunds. |
| Delivery Driver | Secondary (human) | Fulfills delivery orders. Assumed third-party courier in v1. |
| Payment Gateway | External system | Stripe/Square. Processes charges and refunds. |

---

# CUSTOMER USE CASES

## UC-01 — Browse / Search / Filter Products
**Complexity:** M **Priority:** P0
**Primary actor:** Customer
**Goal:** Find OTC products by browsing categories, searching, or filtering.
**Preconditions:** Storefront is live; catalog has published, in-stock products.
**Trigger:** Customer opens the storefront or enters a search term.

**Main flow:**
1. Customer lands on storefront home or category page.
2. Customer browses by category, enters a keyword search, or applies filters (category, price, availability).
3. System returns matching products with name, price, image, and stock status.
4. Customer selects a product to view its detail page.
5. System displays full product detail (description, dosage/usage info, price, stock).

**Alternate / exception flows:**
- 3a. No results → system shows an empty-state message and suggested categories.
- 3b. Product is out of stock → shown but marked "Out of stock," add-to-cart disabled.

**Postconditions:** Customer has viewed products and may proceed to add items to cart (UC-04).
**Notes:** Bilingual (English/Vietnamese) product names and search matching should be considered here.

---

## UC-02 — Register / Login
**Complexity:** M **Priority:** P0
**Primary actor:** Customer
**Goal:** Create an account or authenticate to place orders.
**Preconditions:** None (registration is open to public).
**Trigger:** Customer attempts to check out, or clicks Register/Login.

**Main flow (register):**
1. Customer submits registration (email, password, name, contact info).
2. System validates input and checks email uniqueness.
3. System creates the account and (recommended) sends a verification email.
4. Customer is logged in / prompted to verify.

**Main flow (login):**
1. Customer submits credentials.
2. System authenticates and starts a session.

**Alternate / exception flows:**
- Register 2a. Email already exists → prompt to log in or reset password.
- Login 2a. Invalid credentials → error, allow retry / password reset.
- Password reset → email-based reset link flow.

**Postconditions:** Customer has an authenticated session.
**Notes:** Registration is required in v1 (no guest checkout). Consider bilingual UI for this flow, as it is the first friction point for the target community.

---

## UC-03 — Place Order (Pickup or Delivery)
**Complexity:** L **Priority:** P0
**Primary actor:** Customer
**Goal:** Convert cart contents into a paid order for pickup or delivery.
**Preconditions:** Customer is logged in (UC-02); cart has ≥1 in-stock item (UC-04).
**Trigger:** Customer clicks Checkout.

**Main flow:**
1. Customer reviews cart and proceeds to checkout.
2. Customer selects fulfillment: **pickup** or **delivery**.
3. If delivery, customer provides/confirms address and delivery method (UC-08).
4. **«extend» UC-19:** if cart contains age-restricted items, age/ID verification runs here.
5. Customer selects/confirms a payment method (UC-07).
6. **«include» UC-17:** system requests payment authorization from the gateway.
7. On approval, system creates the order, decrements stock, and shows confirmation.
8. System sends order confirmation (email).

**Alternate / exception flows:**
- 4a. Age/ID verification fails → order blocked for restricted items; customer may remove them and continue.
- 6a. Payment declined → order not created; customer prompted to retry or change method.
- 7a. An item went out of stock during checkout → notify customer, adjust cart.

**Postconditions:** A paid order exists in "Placed" status; stock updated.
**Notes:** This is the system hub — it includes payment and conditionally extends into age verification. Highest integration risk in v1.

---

## UC-04 — Manage Cart / Wishlist
**Complexity:** M **Priority:** P0 (cart) / P2 (wishlist)
**Primary actor:** Customer
**Goal:** Collect products to purchase now (cart) or save for later (wishlist).
**Preconditions:** Storefront live. Cart usable by logged-in customers; wishlist requires login.
**Trigger:** Customer clicks Add to Cart or Save for Later.

**Main flow (cart):**
1. Customer adds a product; system adds it and shows updated cart count.
2. Customer adjusts quantity or removes items.
3. System recalculates totals and validates stock.

**Main flow (wishlist):**
1. Customer saves a product to wishlist.
2. Customer later moves a wishlist item into the cart.

**Alternate / exception flows:**
- Cart 3a. Requested quantity exceeds stock → cap quantity, notify customer.

**Postconditions:** Cart reflects intended purchase; wishlist persists across sessions.
**Notes:** Split for scope — ship cart in v1, defer wishlist to v2 if needed.

---

## UC-05 — Update / Cancel Order
**Complexity:** M **Priority:** P1
**Primary actor:** Customer
**Goal:** Modify or cancel an order before it ships / is picked up.
**Preconditions:** Customer has a placed order not yet fulfilled.
**Trigger:** Customer opens an order and selects Update or Cancel.

**Main flow:**
1. Customer views an eligible order.
2. Customer requests change (e.g. cancel, change fulfillment) within the allowed window.
3. System validates the order is still in a cancelable/editable state.
4. If cancellation involves a completed charge → triggers refund (UC-18).
5. System updates order status and notifies customer.

**Alternate / exception flows:**
- 3a. Order already packed/shipped → cancellation not allowed; direct customer to returns (UC-06).

**Postconditions:** Order is updated or canceled; refund initiated if applicable.
**Notes:** Define the exact editable window (e.g. before status = "Packed").

---

## UC-06 — Request Return / Refund
**Complexity:** L **Priority:** P1
**Primary actor:** Customer
**Goal:** Return a delivered/picked-up product and request a refund.
**Preconditions:** Customer has a fulfilled order within the return window.
**Trigger:** Customer selects Request Return on an order.

**Main flow:**
1. Customer selects item(s) and a return reason.
2. System checks eligibility: within **14 days**; item is **not an opened medication bottle** (non-refundable).
3. If eligible, system creates a return request in "Pending approval."
4. **«include» UC-14:** admin reviews and approves/denies.
5. On approval, **«include» UC-18:** gateway refund is issued.
6. System notifies customer of outcome.

**Alternate / exception flows:**
- 2a. Outside 14 days or opened medication → request rejected with policy explanation.
- 4a. Admin denies → customer notified with reason.

**Postconditions:** Refund issued (approved) or request closed (denied).
**Notes:** Policy (14-day, opened-bottle) must be published at launch even if automation is minimal. Verify against Texas Pharmacy Board OTC return rules. At launch, admin may process refunds manually via the gateway dashboard.

---

## UC-07 — Update Profile / Saved Payment Method
**Complexity:** S **Priority:** P1
**Primary actor:** Customer
**Goal:** Maintain account details and saved payment methods.
**Preconditions:** Customer is logged in.
**Trigger:** Customer opens Profile/Account settings.

**Main flow:**
1. Customer edits profile fields (name, contact, addresses).
2. Customer adds/removes a saved payment method.
3. System validates and saves; payment methods are tokenized via the gateway (no raw card data stored).

**Alternate / exception flows:**
- 2a. Gateway rejects the card → error shown, not saved.

**Postconditions:** Updated profile / payment methods persisted.
**Notes:** Card data lives with the gateway; PharmOS stores only tokens/references.

---

## UC-08 — Decide Delivery Method
**Complexity:** S **Priority:** P1
**Primary actor:** Customer
**Goal:** Specify how a delivery should be handed off.
**Preconditions:** Customer selected delivery in checkout (UC-03).
**Trigger:** Delivery fulfillment selected.

**Main flow:**
1. System presents drop-off options: leave at door / leave at mailbox / hand over in person.
2. Customer selects one; optionally adds instructions.
3. System attaches the preference to the order for the driver.

**Postconditions:** Delivery preference recorded on the order.
**Notes:** Feeds driver instructions (UC-15/16). Some options may be constrained by the courier's capabilities.

---

## UC-09 — Track Delivery Status
**Complexity:** M **Priority:** P1
**Primary actor:** Customer
**Goal:** See the current status of a delivery order.
**Preconditions:** Customer has a delivery order.
**Trigger:** Customer opens the order's tracking view.

**Main flow:**
1. Customer opens order tracking.
2. System displays current status: Placed → Packed → Out for delivery → Delivered.
3. On delivery, system shows drop-off proof photo (from UC-15).

**Alternate / exception flows:**
- 2a. Delivery delayed/failed → status reflects exception; customer sees updated state.

**Postconditions:** Customer is informed of delivery progress.
**Notes:** Status is driven by driver updates (UC-16); consider modeling UC-09 as «include» UC-16. Driver name/photo/live GPS is deferred to v2.

---

# PHARMACY ADMIN USE CASES

## UC-10 — Manage Catalog / Stock
**Complexity:** M **Priority:** P0
**Primary actor:** Pharmacy Admin
**Goal:** Maintain the product catalog and stock levels.
**Preconditions:** Admin is authenticated.
**Trigger:** Admin opens catalog management.

**Main flow:**
1. Admin creates/edits a product (name, description, price, category, image, stock).
2. Admin marks items in/out of stock.
3. System publishes changes to the storefront.

**Alternate / exception flows:**
- 1a. Required fields missing → validation error.

**Postconditions:** Catalog and stock reflect admin changes.
**Notes:** Bilingual product fields (EN/VI) should be supported here if the storefront is bilingual. Flag which fields are age-restricted if UC-19 is in scope.

---

## UC-11 — Manage Discounts
**Complexity:** S **Priority:** P2
**Primary actor:** Pharmacy Admin
**Goal:** Create and stop discounts/promotions.
**Preconditions:** Admin is authenticated; products exist.
**Trigger:** Admin opens discount management.

**Main flow:**
1. Admin creates a discount (product/category, amount or %, date range).
2. Admin activates it; storefront reflects discounted pricing.
3. Admin stops/expires a discount.

**Postconditions:** Active discounts applied at checkout; stopped discounts no longer apply.
**Notes:** Deferrable — store can launch at full price.

---

## UC-12 — Process Orders
**Complexity:** M **Priority:** P0
**Primary actor:** Pharmacy Admin
**Goal:** Fulfill orders (pack and mark ready).
**Preconditions:** A placed, paid order exists.
**Trigger:** Admin opens the order queue.

**Main flow:**
1. Admin views incoming orders.
2. Admin packs the order and marks status: Packed → Ready for pickup **or** ready for delivery.
3. For delivery orders, admin proceeds to UC-13.
4. System updates order status (visible to customer via UC-09).

**Alternate / exception flows:**
- 2a. Item unavailable after order → admin contacts customer / initiates partial refund (UC-18).

**Postconditions:** Order advanced to the next fulfillment stage.
**Notes:** Core fulfillment loop; required for launch.

---

## UC-13 — Trigger Delivery Request
**Complexity:** M (third-party) / L (in-house) **Priority:** P1
**Primary actor:** Pharmacy Admin
**Goal:** Dispatch a packed delivery order to a courier.
**Preconditions:** A delivery order is Packed.
**Trigger:** Admin clicks Request Delivery.

**Main flow:**
1. Admin selects a packed delivery order.
2. System sends a delivery request to the third-party courier API.
3. Courier accepts; system stores tracking reference.
4. **«include» UC-15:** delivery will produce proof photo(s).
5. Order status → Out for delivery.

**Alternate / exception flows:**
- 3a. No courier available → admin retries or arranges alternative.

**Postconditions:** Delivery is dispatched and tracked.
**Notes:** Complexity/priority depends heavily on the in-house-vs-third-party decision. In-house would require driver assignment and a driver app.

---

## UC-14 — Approve Return / Refund
**Complexity:** S **Priority:** P1
**Primary actor:** Pharmacy Admin
**Goal:** Review and decide on customer return/refund requests.
**Preconditions:** A pending return request exists (from UC-06).
**Trigger:** Admin opens the returns queue.

**Main flow:**
1. Admin reviews the request (item, reason, eligibility).
2. Admin approves or denies.
3. On approval, **«include» UC-18:** refund is issued via gateway.
4. System notifies the customer.

**Postconditions:** Return request resolved; refund issued if approved.
**Notes:** Included by UC-06.

---

# DELIVERY DRIVER USE CASES

## UC-15 — Upload Proof Photo
**Complexity:** M **Priority:** P1
**Primary actor:** Delivery Driver
**Goal:** Capture photo proof of pickup and drop-off (DoorDash-style).
**Preconditions:** Driver is assigned to a delivery.
**Trigger:** Driver picks up or completes a drop-off.

**Main flow:**
1. Driver takes a pickup photo when collecting the order.
2. Driver takes a drop-off photo at delivery.
3. Photos are attached to the order; drop-off proof surfaces to the customer (UC-09).

**Postconditions:** Proof photos stored on the order.
**Notes:** If using a third-party courier, this may be handled by the courier's own app and returned via webhook, rather than built in PharmOS.

---

## UC-16 — Update Delivery Status
**Complexity:** S **Priority:** P1
**Primary actor:** Delivery Driver
**Goal:** Advance the delivery state.
**Preconditions:** Driver is assigned to a delivery.
**Trigger:** Delivery progresses.

**Main flow:**
1. Driver updates status: Picked up → En route → Delivered.
2. System records each transition; customer tracking (UC-09) reflects it.

**Postconditions:** Order delivery status current.
**Notes:** With a third-party courier, statuses may arrive via API/webhook rather than manual driver input.

---

# PAYMENT GATEWAY USE CASES (External System)

## UC-17 — Process Payment
**Complexity:** M **Priority:** P0
**Primary actor:** Payment Gateway (external)
**Goal:** Authorize and capture payment for an order.
**Preconditions:** Checkout reached payment step (UC-03).
**Trigger:** System submits a payment request.

**Main flow:**
1. System sends payment details/token to the gateway.
2. Gateway verifies and authorizes.
3. Gateway returns success; system captures and confirms.

**Alternate / exception flows:**
- 2a. Declined → gateway returns failure; order not created (UC-03 handles retry).
- 2b. Cancel/void before capture → transaction voided.

**Postconditions:** Payment captured; order can be created.
**Notes:** Accepts credit card, debit card, PayPal, Apple Pay. Most verify/decline/void logic is provided by the gateway — treat as integration, not custom build.

---

## UC-18 — Process Refund
**Complexity:** S **Priority:** P1
**Primary actor:** Payment Gateway (external)
**Goal:** Return funds for a canceled order or approved return.
**Preconditions:** A captured payment exists; refund approved (UC-05 or UC-14).
**Trigger:** System submits a refund request.

**Main flow:**
1. System sends a refund request referencing the original transaction.
2. Gateway processes the refund.
3. System records the refund and notifies the customer.

**Postconditions:** Funds returned; order/return marked refunded.
**Notes:** At launch, refunds may be issued manually via the gateway dashboard.

---

# CONDITIONAL USE CASE

## UC-19 — Verify Age / ID  «extend» UC-03
**Complexity:** L **Priority:** P0 if age-restricted items are stocked; else defer to v2
**Primary actor:** Customer
**Goal:** Confirm the customer meets age requirements for restricted OTC products.
**Preconditions:** Cart contains an age-restricted product (e.g. pseudoephedrine).
**Trigger:** Checkout detects a restricted item (extends UC-03 step 4).

**Main flow:**
1. System detects a restricted item at checkout.
2. System prompts for age/ID verification.
3. Customer provides required information/verification.
4. On pass, checkout continues; system logs as required by regulation.

**Alternate / exception flows:**
- 4a. Verification fails → restricted item blocked; customer may remove it and continue with the rest.

**Postconditions:** Restricted purchase is compliant and logged, or blocked.
**Notes:** Selling pseudoephedrine-containing products triggers federal logging obligations (Combat Methamphetamine Epidemic Act) plus state rules. **Recommendation:** do not stock age-restricted OTC items in v1; this removes the largest compliance risk and defers UC-19 to v2.

---

## Relationship Summary

| Relationship | Type | Meaning |
|---|---|---|
| UC-03 → UC-17 | «include» | Placing an order always runs payment. |
| UC-06 → UC-14 | «include» | A refund request always needs admin approval. |
| UC-06 → UC-18 | «include» | An approved refund always runs a gateway refund. |
| UC-13 → UC-15 | «include» | A delivery always produces proof photo(s). |
| UC-14 → UC-18 | «include» | Approval issues the gateway refund. |
| UC-19 → UC-03 | «extend» | Age check fires only for restricted items. |
| UC-09 → UC-16 | «include» (proposed) | Customer tracking depends on driver status updates. |

---

## Open Decisions (resolve before build)

1. **Delivery model:** third-party courier API vs. in-house drivers. Drives complexity of UC-13/15/16.
2. **Age-restricted products:** stock in v1 (build UC-19) or defer (drop UC-19 to v2). Recommended: defer.
3. **Wishlist:** ship in v1 or defer (UC-04 split).
4. **Editable/cancelable order window:** define the exact status cutoff for UC-05.
5. **Leanest-launch option:** consider pickup-only v1 (removes UC-08, 09, 13, 15, 16 from critical path) to validate the store faster.
