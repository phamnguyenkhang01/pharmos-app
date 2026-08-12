https://claude.ai/share/cfc4e1f6-3239-4a9d-a3d5-632342ea3533
New chat:
https://claude.ai/share/46ed6aea-b5d5-4880-a77e-61f649eb2c04

I/ Definition:
    PharmOS is an e-commerce website that allows a pharmacy to sell OTC 
    (over-the-counter, non-prescription) products, including medications 
    and medical equipment, to customers who need them. The initial target 
    audience is the Vietnamese community in America, and the initial 
    scope covers a single pharmacy operating its own online storefront.

    Out of scope: prescription processing and insurance claims are not 
    handled by this system.

II/ Use cases/ Actors:
    ACTORS:
- Customer (initial target: Vietnamese community in America)
- Delivery (in-house driver OR third-party courier — TBD)
- Pharmacy Admin (manages inventory, orders, pricing)
- Payment gateway

    USE CASES:

=== V1 / MVP — Numbered Use Cases ===

CUSTOMER
UC-01  Browse / search / filter products by category
UC-02  Register (required) and login
UC-03  Place order (choose pick-up or delivery)
UC-04  Manage cart / wishlist
UC-05  Update / cancel order
UC-06  Request return / refund (14-day limit; opened bottles non-refundable)
UC-07  Update profile / saved payment method
UC-08  Decide delivery method (leave at door / mailbox / hand over)
UC-09  Track delivery status (Placed / Packed / Out for delivery / Delivered)

PHARMACY ADMIN
UC-10  Manage catalog / stock (list medications, mark out-of-stock)
UC-11  Manage discounts (add / stop)
UC-12  Process orders (pack, mark shipped / ready-for-pickup)
UC-13  Trigger delivery request
UC-14  Approve return / refund

DELIVERY DRIVER
UC-15  Upload proof photo (pickup + drop-off)
UC-16  Update delivery status (picked up / en route / delivered)

PAYMENT GATEWAY (external system)
UC-17  Process payment (verify / decline / cancel)
UC-18  Process refund

SHARED / CONDITIONAL
UC-19  Verify age / ID  (extends UC-03, only for age-restricted OTC)

=== Relationships ===
UC-03  «include» → UC-17   (placing an order always runs payment)
UC-06  «include» → UC-14   (a refund request always needs admin approval)
UC-06  «include» → UC-18   (approved refund always runs gateway refund)
UC-13  «include» → UC-15   (delivery always produces proof photo)
UC-19  «extend»  → UC-03   (age check only fires for restricted items)


=== V2 / Later ===

Admin:
- Admin can put recommendations/promoted medications on storefront (manual curation first; algorithmic later).
- Admin can send reminders to customers for auto-refill (requires subscription/recurring billing logic).
- Admin assigns delivery manually to in-house driver pool (only needed if you add in-house delivery alongside/instead of third-party).

Customer:
- Customer can save products for later ("wishlist").
- Customer can leave reviews/ratings.
- Auto-refill subscription for recurring OTC products.
- Guest checkout (if you ever relax "required registration").

Delivery:
- In-house driver app/portal (only if you move away from third-party-only delivery).




=== V1 / MVP - USE CASES WITH COMPLEXITY AND PRIORITY === 

Legend:
  Complexity — S (small) / M (medium) / L (large)
  Priority   — P0 (must-have to launch) / P1 (launch-important) / P2 (can slip)

┌────────┬─────────────────────────────────┬────────────┬──────────┐
│  ID    │  Use Case                       │ Complexity │ Priority │
├────────┼─────────────────────────────────┼────────────┼──────────┤
│ CUSTOMER                                                          │
│ UC-01  │ Browse / search / filter        │     M      │   P0     │
│ UC-02  │ Register (required) / login     │     M      │   P0     │
│ UC-03  │ Place order (pickup/delivery)   │     L      │   P0     │
│ UC-04  │ Manage cart / wishlist          │     M      │   P0*    │
│ UC-05  │ Update / cancel order           │     M      │   P1     │
│ UC-06  │ Request return / refund         │     L      │   P1     │
│ UC-07  │ Update profile / saved payment  │     S      │   P1     │
│ UC-08  │ Decide delivery method          │     S      │   P1     │
│ UC-09  │ Track delivery status           │     M      │   P1     │
├────────┼─────────────────────────────────┼────────────┼──────────┤
│ PHARMACY ADMIN                                                    │
│ UC-10  │ Manage catalog / stock          │     M      │   P0     │
│ UC-11  │ Manage discounts                │     S      │   P2     │
│ UC-12  │ Process orders                  │     M      │   P0     │
│ UC-13  │ Trigger delivery request        │     M      │   P1     │
│ UC-14  │ Approve return / refund         │     S      │   P1     │
├────────┼─────────────────────────────────┼────────────┼──────────┤
│ DELIVERY DRIVER                                                   │
│ UC-15  │ Upload proof photo              │     M      │   P1     │
│ UC-16  │ Update delivery status          │     S      │   P1     │
├────────┼─────────────────────────────────┼────────────┼──────────┤
│ PAYMENT GATEWAY (external)                                        │
│ UC-17  │ Process payment                 │     M      │   P0     │
│ UC-18  │ Process refund                  │     S      │   P1     │
├────────┼─────────────────────────────────┼────────────┼──────────┤
│ CONDITIONAL                                                       │
│ UC-19  │ Verify age / ID (extends UC-03) │     L      │   P0**   │
└────────┴─────────────────────────────────┴────────────┴──────────┘

*  UC-04: cart is P0, wishlist is P2 — split if you want to defer wishlist.
** UC-19: P0 ONLY if catalog contains age-restricted items (e.g. pseudoephedrine).
   If v1 stocks none, drop UC-19 entirely → becomes v2.




