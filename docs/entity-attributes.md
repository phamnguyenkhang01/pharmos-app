# PharmOS — V1 Domain Entity Attribute Specification

Full attribute detail for each domain entity identified from the v1 use cases. This is a **conceptual/logical model** — types are shown generically (string, int, decimal, enum, timestamp, uuid, bool, text, ref). Final SQL types, indexes, and constraints belong to the physical DB-design stage.

**Conventions**
- `Primary Key` = primary key, `Foreign Key` = foreign key, `Unique` = unique, `Not Null` = not null (required), `Nullable` = nullable/optional.
- `ref → Entity` = foreign key to that entity.
- Money fields are `decimal(10,2)` and assumed USD in v1.
- All entities implicitly carry `created_at` / `updated_at` timestamps unless the lifecycle makes `updated_at` meaningless (noted where so).
- Bilingual fields (EN/VI) are shown as two columns (`_en` / `_vi`); a JSON/i18n table is an alternative but two columns is simpler for v1.

---

## 1. Customer  (UC-02, 03, 04, 05, 06, 07, 09)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| email | string | Unique, Not Null | Login identifier. |
| password_hash | string | Not Null | Never store plaintext. |
| full_name | string | Not Null | |
| phone | string | Nullable | For delivery/pickup contact. |
| preferred_language | enum(en, vi) | Not Null, default vi | Drives bilingual UI for target community. |
| status | enum(unverified, active, disabled) | Not Null, default unverified | Email-verification gate. |
| email_verified_at | timestamp | Nullable | Set when email confirmed. |
| created_at | timestamp | Not Null | |
| updated_at | timestamp | Not Null | |

---

## 2. Address  (UC-03, 07, 08)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| customer_id | ref → Customer | Foreign Key, Not Null | |
| recipient_name | string | Not Null | May differ from account holder. |
| street_line1 | string | Not Null | |
| street_line2 | string | Nullable | Apt/unit. |
| city | string | Not Null | |
| state | string | Not Null | |
| zip | string | Not Null | |
| phone | string | Nullable | Delivery contact for this address. |
| is_default | bool | Not Null, default false | |
| created_at | timestamp | Not Null | |

---

## 3. PaymentMethod  (UC-07, 03)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| customer_id | ref → Customer | Foreign Key, Not Null | |
| gateway_token | string | Not Null | Token from Stripe/Square. **No raw card data.** |
| type | enum(card, paypal, apple_pay) | Not Null | |
| display_label | string | Not Null | e.g. "Visa ····4242". |
| is_default | bool | Not Null, default false | |
| created_at | timestamp | Not Null | |

---

## 4. Product  (UC-01, 04, 10, 11)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| name_en | string | Not Null | |
| name_vi | string | Nullable | Bilingual display; fall back to EN if empty. |
| description_en | text | Nullable | |
| description_vi | text | Nullable | |
| category_id | ref → Category | Foreign Key, Not Null | |
| price | decimal(10,2) | Not Null | Current list price. |
| image_ref | string | Nullable | Path/URL to product image. |
| stock_quantity | int | Not Null, default 0, ≥0 | |
| status | enum(draft, published, unpublished) | Not Null, default draft | Only `published` shows on storefront. |
| is_medication | bool | Not Null, default false | Drives UC-06 opened-bottle non-refundable rule. |
| is_restricted | bool | Not Null, default false | Age-restricted (UC-19). v2 unless stocked. |
| created_at | timestamp | Not Null | |
| updated_at | timestamp | Not Null | |

*Derived, not stored:* `is_out_of_stock` = (stock_quantity == 0).

---

## 5. Category  (UC-01, 10)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| name_en | string | Not Null | |
| name_vi | string | Nullable | |
| parent_category_id | ref → Category | Foreign Key, Nullable | Self-reference for sub-categories. |
| sort_order | int | Nullable | Display ordering. |

---

## 6. Cart  (UC-04, 03)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| customer_id | ref → Customer | Foreign Key, Unique, Not Null | One persistent cart per customer (registration required). |
| updated_at | timestamp | Not Null | |

---

## 7. CartItem  (UC-04)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| cart_id | ref → Cart | Foreign Key, Not Null | |
| product_id | ref → Product | Foreign Key, Not Null | |
| quantity | int | Not Null, ≥1 | |
| added_at | timestamp | Not Null | |

*Note:* price is **not** stored here — read live from Product until checkout. Unique(cart_id, product_id) to prevent duplicate lines.

---

## 8. WishlistItem  (UC-04)  *(P2 — defer with wishlist)*

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| customer_id | ref → Customer | Foreign Key, Not Null | |
| product_id | ref → Product | Foreign Key, Not Null | |
| saved_at | timestamp | Not Null | |

Unique(customer_id, product_id).

---

## 9. Order  (UC-03, 05, 09, 12, 13)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| order_number | string | Unique, Not Null | Human-friendly reference. |
| customer_id | ref → Customer | Foreign Key, Not Null | |
| status | enum(placed, packed, ready_for_pickup, out_for_delivery, delivered, completed, canceled, refunded) | Not Null, default placed | |
| fulfillment_type | enum(pickup, delivery) | Not Null | |
| address_id | ref → Address | Foreign Key, Nullable | Required if delivery, null if pickup. |
| subtotal | decimal(10,2) | Not Null | Sum of line totals before discount. |
| discount_total | decimal(10,2) | Not Null, default 0 | |
| total | decimal(10,2) | Not Null | Amount charged. |
| placed_at | timestamp | Not Null | |
| updated_at | timestamp | Not Null | |

*Constraint:* if fulfillment_type = delivery then address_id Not Null.

---

## 10. OrderItem  (UC-03, 06, 12)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| order_id | ref → Order | Foreign Key, Not Null | |
| product_id | ref → Product | Foreign Key, Not Null | |
| product_name_snapshot | string | Not Null | Name at purchase time (product may be renamed/deleted later). |
| quantity | int | Not Null, ≥1 | |
| unit_price_at_purchase | decimal(10,2) | Not Null | **Frozen** price snapshot. |
| line_total | decimal(10,2) | Not Null | quantity × unit_price. |
| is_medication_snapshot | bool | Not Null | Snapshot for refund-eligibility (UC-06). |

*Note:* the price/name/flag snapshots are deliberate — historical orders and refunds must stay accurate even if the Product changes.

---

## 11. Discount  (UC-11)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| scope | enum(product, category) | Not Null | |
| target_id | uuid | Not Null | Product.id or Category.id per scope. |
| type | enum(percent, amount) | Not Null | |
| value | decimal(10,2) | Not Null | % or fixed amount per type. |
| start_date | timestamp | Not Null | |
| end_date | timestamp | Not Null | |
| is_active | bool | Not Null, default true | Admin can stop early (UC-11). |
| created_at | timestamp | Not Null | |

*Constraint:* end_date > start_date.

---

## 12. Payment  (UC-03, 17, 18)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| order_id | ref → Order | Foreign Key, Not Null | |
| gateway_transaction_id | string | Unique, Not Null | Reference into Stripe/Square. |
| amount | decimal(10,2) | Not Null | |
| method_type | enum(card, paypal, apple_pay) | Not Null | |
| status | enum(authorized, captured, declined, voided, refunded, partially_refunded) | Not Null | |
| created_at | timestamp | Not Null | |

*Note:* authoritative record lives in the gateway; this is the local mirror/reference.

---

## 13. Refund  (UC-05, 06, 14, 18)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| payment_id | ref → Payment | Foreign Key, Not Null | |
| return_request_id | ref → ReturnRequest | Foreign Key, Nullable | Null for cancellation refunds (UC-05). |
| amount | decimal(10,2) | Not Null | Supports partial refunds. |
| gateway_refund_id | string | Unique, Not Null | Reference into gateway. |
| reason | string | Nullable | |
| status | enum(pending, completed, failed) | Not Null, default pending | |
| created_at | timestamp | Not Null | |

---

## 14. ReturnRequest  (UC-06, 14)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| order_id | ref → Order | Foreign Key, Not Null | |
| customer_id | ref → Customer | Foreign Key, Not Null | |
| reason | string | Not Null | Customer-supplied. |
| status | enum(pending, approved, denied) | Not Null, default pending | |
| requested_at | timestamp | Not Null | |
| decided_at | timestamp | Nullable | |
| decided_by | ref → PharmacyAdmin | Foreign Key, Nullable | Set when approved/denied (UC-14). |

*Eligibility (14-day window, opened-medication non-refundable) is evaluated at creation against Order + OrderItem snapshots — not stored as fields.*

---

## 15. ReturnItem  (UC-06)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| return_request_id | ref → ReturnRequest | Foreign Key, Not Null | |
| order_item_id | ref → OrderItem | Foreign Key, Not Null | |
| quantity | int | Not Null, ≥1 | Allows partial-quantity returns. |

---

## 16. Delivery  (UC-08, 09, 13, 15, 16)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| order_id | ref → Order | Foreign Key, Unique, Not Null | One delivery per order. |
| drop_off_method | enum(door, mailbox, hand_over) | Not Null | From UC-08. |
| instructions | text | Nullable | Customer notes. |
| courier_reference | string | Nullable | Third-party courier job id. |
| tracking_number | string | Nullable | |
| status | enum(pending, picked_up, en_route, delivered, failed) | Not Null, default pending | Feeds UC-09 tracking. |
| created_at | timestamp | Not Null | |
| updated_at | timestamp | Not Null | |

*Note:* pickup orders have **no** Delivery row.

---

## 17. ProofPhoto  (UC-15, 09)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| delivery_id | ref → Delivery | Foreign Key, Not Null | |
| type | enum(pickup, drop_off) | Not Null | |
| image_ref | string | Not Null | Path/URL to stored image. |
| captured_at | timestamp | Not Null | |

*Note:* drop_off photo surfaces to the customer (UC-09).

---

## 18. PharmacyAdmin  (UC-10, 11, 12, 13, 14)

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| email | string | Unique, Not Null | |
| password_hash | string | Not Null | |
| full_name | string | Not Null | |
| role | enum(admin) | Not Null, default admin | Single role in v1; staff/owner split is v2. |
| status | enum(active, disabled) | Not Null, default active | |
| created_at | timestamp | Not Null | |

---

## 19. AgeVerification  (UC-19)  *(v2 unless age-restricted items stocked)*

| Attribute | Type | Constraints | Notes |
|---|---|---|---|
| id | uuid | Primary Key | |
| order_id | ref → Order | Foreign Key, Not Null | |
| customer_id | ref → Customer | Foreign Key, Not Null | |
| method | string | Not Null | How verification was performed. |
| result | enum(pass, fail) | Not Null | |
| regulatory_log_ref | string | Nullable | Combat Meth Epidemic Act logging reference (pseudoephedrine). |
| verified_at | timestamp | Not Null | |

---

## Cross-Cutting Notes

**Snapshotting pattern (important):** OrderItem freezes `product_name_snapshot`, `unit_price_at_purchase`, and `is_medication_snapshot`. This is the single most important data-integrity decision in the model — it ensures orders, refunds, and return-eligibility stay correct even after products are edited, repriced, or removed.

**No raw card data anywhere:** PaymentMethod and Payment hold only gateway tokens/IDs. This keeps PharmOS out of the heaviest PCI-compliance scope.

**Bilingual fields:** shown as `_en`/`_vi` column pairs on Product and Category. If the catalog grows large or more languages appear, migrate to a translations table — but two columns is right for v1.

**Soft-delete consideration (open decision):** Products referenced by historical OrderItems should not be hard-deleted. Either use `status = unpublished` or add a `deleted_at` soft-delete column. The name/price snapshots on OrderItem already protect order history regardless.

---

## Open Attribute-Level Decisions

1. **Guest vs required account:** model assumes required registration (Cart has Unique customer_id). If guest checkout is ever added, Cart and Order need a nullable customer + session key.
2. **Tax/shipping fields on Order:** not modeled yet — add `tax_total` and `delivery_fee` to Order if either applies at launch.
3. **Address snapshot on Order:** currently Order references a live Address (Foreign Key). If a customer edits/deletes that address later, the order's delivery location changes. Consider snapshotting the address onto the Order (like OrderItem does with price) for historical accuracy.
4. **Discount stacking:** model allows multiple active discounts to match one product. Define whether they stack or a single best-discount wins.
5. **Inventory reservation:** stock_quantity is decremented at order creation (UC-03 step 7). Decide whether carts temporarily reserve stock or oversell is possible between add-to-cart and checkout.