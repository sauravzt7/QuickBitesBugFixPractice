# Delivery Fee Waiver — Technical Specification

**Status:** Approved · **Owner:** Checkout & Pricing · **Ticket:** QB-1427

## 1. Background

QuickBites lets a customer order from several restaurants in one cart. Each
restaurant needs its own courier pickup, so delivery is priced **per unique
restaurant**. To grow average order value, marketing launched a promotion:
**free delivery on any order whose subtotal is $50.00 or more.**

Customers report that they are still being charged delivery on large orders.

## 2. Definitions

| Term | Meaning |
|------|---------|
| Subtotal | Sum of `unitPrice × quantity` over all cart lines, rounded to 2 dp. Excludes delivery fee, tax and tips. |
| Unique restaurant | A distinct `restaurant.id` among cart lines. Two items from the same restaurant count once. |
| Base delivery fee | `deliveryFeePerRestaurant × uniqueRestaurantCount` (default `$5.00` per restaurant). |
| Threshold | `quickbites.pricing.free-delivery-threshold` (default `$50.00`). |

## 3. Rules

1. **R1 — Base fee.** If the subtotal is **below** the threshold, the delivery fee is
   `$5.00 × uniqueRestaurantCount`.
2. **R2 — Waiver.** If the subtotal is **greater than or equal to** the threshold, the
   delivery fee is **`$0.00`**, regardless of how many restaurants are in the cart.
   (Exactly `$50.00` qualifies.)
3. **R3 — Empty cart.** An empty cart has a delivery fee of `$0.00`.
4. **R4 — Total.** `total = subtotal + deliveryFee`, rounded to 2 dp (HALF_UP).
5. **R5 — Single source of truth.** The fee shown in the cart (`GET /api/cart`),
   in the checkout summary, and persisted on the order (`POST /api/orders`) **must
   be computed by the same logic** and must always agree for the same cart.
6. **R6 — Configurable.** Neither the fee nor the threshold may be hard-coded;
   both come from `PricingProperties`.
7. **R7 — Re-evaluation.** The fee is recalculated on every cart change. Removing
   items so the subtotal drops below the threshold reinstates the fee.

## 4. Examples

| Cart | Subtotal | Restaurants | Fee | Total |
|------|---------:|:-----------:|----:|------:|
| 4× Nachos Supreme (Taco Fiesta) | $51.96 | 1 | **$0.00** | $51.96 |
| 2× Margherita Pizza + 1× Tiramisu (Italian Kitchen) | $40.97 | 1 | $5.00 | $45.97 |
| 1× Margherita Pizza + 1× Nachos Supreme | $28.98 | 2 | $10.00 | $38.98 |
| 2× Lasagna + 1× Dragon Roll + 1× Edamame | $57.96 | 2 | **$0.00** | $57.96 |
| 2× Fiesta Party Platter (Taco Fiesta) | $50.00 | 1 | **$0.00** | $50.00 |
| Empty cart | $0.00 | 0 | $0.00 | $0.00 |

## 5. API contract (unchanged)

`GET /api/cart` and every cart mutation return:

```json
{
  "items": [ ... ],
  "restaurantCount": 1,
  "subtotal": 51.96,
  "deliveryFee": 0.00,
  "total": 51.96,
  "freeDeliveryThreshold": 50.00
}
```

`POST /api/orders` returns the order with `subtotal`, `deliveryFee`, `total`.

## 6. Out of scope

- Frontend changes — the UI already renders whatever the backend returns
  (it shows **FREE** when `deliveryFee` is `0`).
- Backfilling historical orders.

## 7. Acceptance

`mvn test` passes, including `DeliveryFeeWaiverTest` and `CheckoutFlowIntegrationTest`.
