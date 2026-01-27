Inventory Service does NOT:
❌ know about payments
❌ know about orders

It only:
✔ Tracks stock
✔ Reserves stock
✔ Releases stock

How will Order ↔ Inventory talk?

Two options:

1️⃣ REST (synchronous) ❌
Tight coupling
Hard to scale
Bad for payments

2️⃣ EVENTS + SAGA ✅ (WE CHOOSE THIS)
Loose coupling
Failure handling
Industry standard

👉 We’ll use Saga – Choreography pattern.

Order Service
|
|  OrderCreatedEvent
↓
Inventory Service
|
|  InventoryReservedEvent
↓
Order Service
|
|  OrderConfirmedEvent

If inventory fails:

InventoryRejectedEvent → OrderCancelled



What Happens WITHOUT @Transactional

Let’s walk through your exact code without @Transactional.

Scenario: Two users order the same product at the same time
Initial state:

Product P1 → availableQuantity = 5

🧪 Thread 1 (Order A)
inventory = findById(P1)  // gets 5

🧪 Thread 2 (Order B)
inventory = findById(P1)  // also gets 5


❌ Both see the same stock

Thread 1 continues
inventory.setAvailableQuantity(3);
repository.save(inventory);

Thread 2 continues
inventory.setAvailableQuantity(3);
repository.save(inventory);

💥 RESULT (DATA CORRUPTION)

You sold 4 items, but DB says:

availableQuantity = 3


❌ Overselling
❌ Financial loss
❌ Broken inventory

THIS is why inventory systems fail in production.

🚨 Another Critical Case: Exception After Update

Without @Transactional:

inventory.setAvailableQuantity(2);
repository.save(inventory);

// boom 💥
throw new RuntimeException("Kafka failed");

Result:

❌ Stock reduced

❌ Order not completed

❌ No rollback

Now inventory is wrong forever.

✅ What @Transactional Fixes

With @Transactional:

Case 1: Concurrent requests

DB applies row-level locking

Second transaction waits

Correct stock deduction

Case 2: Exception occurs

Entire transaction rolls back

Inventory remains unchanged

🧠 Very Important Detail (Advanced)

@Transactional works at DB level, not JVM level.

Uses ACID properties

Managed by Spring + Hibernate

Commits only at method end

⚠️ Subtle but CRITICAL Rule

@Transactional works ONLY when:

Method is public

Called from another Spring bean

❌ This will NOT work:

this.reserveStock();



“Without @Transactional, inventory updates are not atomic, leading to race conditions, overselling, and inconsistent state.
@Transactional ensures isolation and rollback, which is critical in payment and inventory systems.”


# Spring boot 4+ is not working with H2

Why Optimistic Locking (Quick Reminder)
No DB row lock
High throughput
Detects conflicts instead of blocking
Perfect when collisions are rare (e-commerce)

Hibernate adds version to WHERE clause during update
If version changed → update fails
Prevents lost updates

UPDATE inventory
SET available_quantity = ?, version = version + 1
WHERE product_id = ? AND version = ?

If another transaction updated it first:
rows affected = 0
Hibernate throws OptimisticLockException

We used optimistic locking with a version column to prevent lost updates during concurrent inventory reservations,
validated using multithreaded integration tests

Optimistic locking works only across transactions.
Annotating the test with @Transactional caused all threads to share one transaction, so locking didn’t trigger.