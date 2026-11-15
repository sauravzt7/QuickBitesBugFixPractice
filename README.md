# QuickBites — Bug-Fix Practice (AI-assisted coding round style)

A small, realistic Spring Boot + vanilla JS food-delivery app with a **planted
backend bug**, modelled on "fix this issue in an existing codebase" interview
rounds. Timebox yourself to **60–75 minutes**.

---

## Question

QuickBites is a food delivery app that lets users order from multiple
restaurants in a single cart. A delivery fee of **$5 is charged per unique
restaurant**. To encourage larger orders, the platform promises **free delivery
on orders of $50 or more**, but this waiver is currently broken.

### Issue summary

When a user's cart subtotal reaches $50 or more, the delivery fee should be
waived entirely. Instead, the full delivery fee is always charged regardless of
the subtotal. **Your task is to fix this issue on the backend.**

### Steps to reproduce

1. Run the app (see below) and open <http://localhost:8080>.
2. Log in with the test credentials:
   ```
   Email:    alex.morgan@quickbites.com
   Password: password123
   ```
3. Add items from a single restaurant until the cart subtotal is $50 or more
   (e.g. 4× **Nachos Supreme** from Taco Fiesta = $51.96).
4. Open the cart — the delivery fee is still **$5.00** even though the subtotal
   exceeds the free-delivery threshold.
5. Proceed to checkout — the delivery fee is still shown in the order summary.
6. Place the order with this test card:
   ```
   Cardholder Name: Sample User
   Card Number:     4242424242424242
   Expiry Month:    12
   Expiry Year:     2050
   CVV:             123
   ```
7. Open **Orders** — the order was created with a delivery fee ($56.96 total).

### Expected behaviour

- When the cart subtotal is **below $50.00**, a **$5.00** delivery fee is
  charged **per unique restaurant**.
- When the cart subtotal is **$50.00 or more**, the delivery fee is **$0**.
- The waiver is reflected **both in the cart view and in the final order total**.

> **Note:** Read `technical-specs/DeliveryFeeWaiver.md` carefully — it has the
> full rules, edge cases and expected behaviour.

---

## Running

Requirements: Java 17+, Maven 3.9+.

```bash
cd backend
mvn spring-boot:run          # app on http://localhost:8080
mvn test                     # run the test suite
```

The frontend in `frontend/` is plain HTML/JS and is served by the backend at `/`.
H2 console: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:quickbites`, user `sa`).

### Useful API calls

```bash
TOKEN=$(curl -s localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"alex.morgan@quickbites.com","password":"password123"}' | jq -r .token)

curl -s localhost:8080/api/restaurants | jq '.[] | {name, menu: [.menu[] | {id, name, price}]}'
curl -s localhost:8080/api/cart/items -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"menuItemId": 2, "quantity": 4}' | jq
```

## Project layout

```
backend/
  src/main/java/com/quickbites/
    config/       PricingProperties, AuthProperties, WebConfig
    controller/   Auth, Restaurant, Cart, Order REST controllers
    dto/          request / response records
    exception/    domain exceptions + GlobalExceptionHandler
    filter/       AuthTokenFilter (Bearer token -> userId)
    model/        JPA entities
    repository/   Spring Data repositories
    seed/         DataSeeder (users, restaurants, menus)
    service/      business logic  <-- start here
    util/         money, card validation, hashing
  src/test/java/  unit + integration tests (some currently FAIL)
frontend/         index.html, app.js, styles.css
technical-specs/  DeliveryFeeWaiver.md
```

## How to practise

1. Reproduce the bug in the UI **and** with `mvn test` before touching code.
2. Read the spec; list every rule (there are edge cases).
3. Find **all** the places the fee is computed — it's not just one line.
4. Fix it so cart and order share one source of truth; don't hard-code numbers.
5. Make every test pass, then add one test of your own for an edge case.
6. Narrate your reasoning as you go, as you would to an interviewer.

Spoiler-free hint level 1: `grep -rn "deliveryFee" backend/src/main`.
A reference fix lives on the **`solution`** branch — don't peek until you're done.

---

## Solution notes (`solution` branch)

There were **three** defects, which is why fixing one spot isn't enough:

1. **`CartService`** called the overload `calculate(restaurantCount)`, which ignores the
   subtotal, so the cart never applied the waiver.
2. **`OrderService`** didn't use the calculator at all. It recomputed
   `feePerRestaurant × count` inline, so the saved order still charged delivery even
   after the cart was fixed (breaks spec R5).
3. **`DeliveryFeeCalculator.qualifiesForFreeDelivery`** used `> 0` instead of `>= 0`, so a
   subtotal of exactly $50.00 didn't qualify (spec R2).

Fix: make `calculate(subtotal, count)` the only public entry point (the base-fee
helper is now private), route both cart and checkout through it, and use `>=`.
