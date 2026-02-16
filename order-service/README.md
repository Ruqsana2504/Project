An Order Service answers ONE core question:
“What did the customer want, and what is the current state of fulfilling it?”

It is NOT:
Payment logic ❌
Inventory logic ❌
Shipping logic ❌

It does NOT check stock
It does NOT process payment
It only records what should happen next
Saga decides how.

The Order Service stores the immutable intent of a user’s purchase and tracks the order state,while downstream services handle inventory and payment as part of a Saga.



1️⃣ Identity (Who / What Order Is This?)
Every order must be uniquely identifiable.
Required fields:
orderId (UUID)
userId (who placed the order)
👉 Without this, you can’t track or debug anything.


2️⃣ Business Intent (What Is Being Ordered?)
What did the user ask for?
Required fields:
productId (or list of items later)
quantity
price / amount
👉 Order service stores intent, not inventory availability.


3️⃣ Order State (Lifecycle Control)
Orders are long-lived and stateful.
Typical states:
CREATED
INVENTORY_RESERVED
PAYMENT_PENDING
PAYMENT_SUCCESS
COMPLETED
CANCELLED
👉 This is how Saga coordinates everything.


4️⃣ Financial Snapshot (IMPORTANT CONCEPT)
The order must capture a snapshot of pricing at creation time.
Why?
Product price may change tomorrow
Discounts may expire
Fields:
amount
currency
discountApplied
👉 Never recalculate order amount later.


5️⃣ Idempotency & Safety (PRODUCTION LEVEL)
Orders must be safe against retries.
Fields:
idempotencyKey
version (for optimistic locking)
👉 Prevents duplicate orders.


6️⃣ Audit & Debugging (VERY IMPORTANT)
Every production system needs traceability.
Fields:
createdAt
updatedAt
createdBy
failureReason
