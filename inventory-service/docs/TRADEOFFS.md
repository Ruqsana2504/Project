# Design Tradeoffs

This document outlines the major architectural and design decisions made in the Inventory Service, the alternatives
considered, and the tradeoffs involved. Tradeoff documentation helps future developers understand why certain
choices were made and what implications they carry.
It should explain the rationale and main quality attribute tradeoffs for the design decisions.

1. Architecture Pattern
   **Decision:** Adopt a microservices architecture for the Inventory Service.
   Rationale: Microservices allow independent deployment, scalability, and separation of concerns for inventory-specific
   logic.
   They fit well if the system is expected to grow with multiple interacting services.

Tradeoffs:

# Pros

Independent scaling of inventory operations
Clear service boundary for responsibilities
Technology flexibility per service

# Cons

Increased complexity in service communication
More overhead with inter-service networking and monitoring
Harder to maintain if the team is small and unfamiliar with microservices patterns

2. Data Storage Choice

Decision: Use a relational database (e.g., PostgreSQL) for inventory data.

Rationale: Relational databases provide strong consistency and support complex queries, which is useful for inventory
tracking, joining tables, and maintaining strict stock integrity.

Tradeoffs:

Strong consistency ensures accurate inventory counts, but relational databases can be harder to scale horizontally
compared to NoSQL options.

If the service grows extremely large with high write throughput, relational scaling becomes more costly and may require
partitioning or sharding.

3. API Design: Synchronous vs Asynchronous

Decision: Use primarily synchronous REST APIs for client interactions.

Rationale: REST APIs are simple, easy to understand and widely supported tools for CRUD operations on inventory records.

Tradeoffs:

Simple implementation and predictable request lifecycle.

But synchronous REST calls can introduce latency and become a bottleneck under heavy load.

For high-throughput use cases (e.g., massive order spikes), asynchronous messaging (queues) could improve resilience,
but at the cost of added infrastructure and complexity.

4. Caching Strategy

Decision: Add caching for frequently accessed inventory queries.

Rationale: Reduces database load and improves read performance for inventory lookups.

Tradeoffs:

Caches add speed but introduce overhead for cache invalidation logic.

Data may become stale if invalidation isn’t carefully synchronized with updates.

Requires careful balancing between speed and data accuracy.

5. Error Handling & Validation

Decision: Basic error handling and input validation implemented at service boundaries.

Rationale: Prevents bad data from entering the system and simplifies debugging.

Tradeoffs:

Improves reliability of operations.

More developer effort is required up front.

Additional validation may slightly slow response times on high-performance paths.

6. Testing Strategy

Decision: Prioritize unit and integration tests for critical inventory operations.

Rationale: Ensures correctness of key functionality like stock adjustment, lookups, and transaction rollbacks.

Tradeoffs:

High test coverage increases confidence before deployment.

More tests require time to write and maintain.

Extensive test suites can slow down CI/CD pipelines if not optimized.

Summary

Every design choice has tradeoffs. In this service, we prioritized correctness and maintainability (e.g., using
relational databases and clear API contracts) while still aiming for scalability and performance where feasible (e.g.,
caching and microservices). These decisions make sense based on current requirements, but may need revisiting if the
system evolves or load patterns change significantly.

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
| OrderCreatedEvent
↓
Inventory Service
|
| InventoryReservedEvent
↓
Order Service
|
| OrderConfirmedEvent

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

“Without @Transactional, inventory updates are not atomic, leading to race conditions, overselling, and inconsistent
state.
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

Why we are not using synchronized
“synchronized provides thread safety only within a JVM. synchronized fails in horizontal scaling.
In a horizontally scaled system with multiple instances, each JVM has its own lock, so it cannot prevent concurrent
updates to shared resources like a database.”

1️⃣ What should happen when optimistic locking fails?

There are only 3 correct strategies:
✅ Option A — Retry (most common)
Try again because the conflict was temporary.

✅ Option B — Fail fast
Tell the caller: “Stock changed, try again.”

✅ Option C — Compensate
Used in distributed workflows (Saga).

For inventory → retry is usually correct.

Why retry works here

Concurrency conflicts are rare and short-lived.

Timeline:

T1 updates inventory
T2 fails due to version mismatch
T2 retries → reads fresh state → succeeds or fails cleanly

Key rule (memorize this):

One persistence context = one transaction = one thread

@Transactional does 4 things:

1️⃣ Opens a DB transaction
2️⃣ Creates a persistence context
3️⃣ Binds it to current thread
4️⃣ Commits or rolls back at the end

❗ @Transactional is thread-bound

Thread T1
├── Transaction
│ └── Persistence Context (PC1)
│
└── commit → flush → close PC

Thread T1 → Transaction → PC1
Thread T2 → Transaction → PC2

Pod A (JVM 1)          Pod B (JVM 2)
───────────── ─────────────
Thread A1 Thread B1
Transaction A Transaction B
PC_A PC_B

Critical truth:

❌ Persistence context is NOT shared
❌ synchronized does NOT work
❌ in-memory locks are useless

1️⃣ Pod A reads Inventory (version = 1)
2️⃣ Pod B reads Inventory (version = 1)
3️⃣ Pod A updates → version becomes 2
4️⃣ Pod B updates → ❌ version mismatch

➡ DB rejects update
➡ Exception thrown

Thread T1 starts
│
│ @Transactional
│ ┌──────────────────────────────────┐
│ │ Transaction (TX1)               │
│ │ │
│ │ Persistence Context (PC1)       │
│ │ ┌──────────────┐ │
│ │ │ Inventory P1 │◄── managed │
│ │ │ qty = 5 │ │
│ │ │ version = 1 │ │
│ │ └──────────────┘ │
│ │ │
│ │ Dirty checking happens here │
│ │ │
│ └──────────────────────────────────┘
│
│ Commit
│ ├── flush changes to DB
│ └── close PC
│
Thread ends

Important

Entity is managed
Hibernate tracks changes
Update happens automatically

ASCII Diagram — NO @Transactional

Thread T1
│
│ repo.findById()
│ ┌────────────────────┐
│ │ Temp TX │
│ │ Temp PC │
│ │ Inventory P1 │
│ └────────────────────┘
│
│ PC closed ❌
│
│ Inventory object now DETACHED
│
│ inv.setAvailableQuantity(...)
│
│ ❌ NO dirty checking
│ ❌ NO auto update
│
Thread ends

Change exists only in memory
DB is unchanged
No locking
No consistency guarantee

Persistence Context (PC)
┌──────────────────────────┐
│ Inventory (CURRENT)      │ ← you modify this
│ qty = 5 │
│ version = 1 │
│ │
│ Inventory (SNAPSHOT)     │ ← original DB state
│ qty = 5 │
│ version = 1 │
└──────────────────────────┘
Dirty Checking + Version

BEGIN TX
│
│ Load Inventory (v=1)
│ ┌───────────────┐
│ │ SNAPSHOT │
│ │ qty=5 v=1 │
│ └───────────────┘
│
│ Change qty → 3
│
│ COMMIT
│ ├─ Compare SNAPSHOT vs CURRENT
│ ├─ Generate UPDATE with version check
│ └─ If rows=0 → OptimisticLockException
│
END TX

Why This Works Across Threads & Pods

Because:

Version check happens in the database

DB is shared

JVM memory is not

This is why optimistic locking is cloud-safe.

Happy Path Saga (ASCII Diagram)
Client
│
▼
Order Service
│ TX1: CREATE ORDER (PENDING)
│
▼
Inventory Service
│ TX2: RESERVE STOCK
│
▼
Payment Service
│ TX3: DEBIT MONEY
│
▼
Order Service
│ TX4: MARK ORDER CONFIRMED
│
▼
SUCCESS 🎉

Key Insight:

✔ Each step commits independently
✔ No shared transaction

Persistence Contexts in Saga
Order TX1 → PC1 → commit → close
Inventory TX2 → PC2 → commit → close
Payment TX3 → PC3 → commit → close
Order TX4 → PC4 → commit → close

❌ PCs never overlap
❌ No shared memory

Failure Case — Payment Fails

Let’s say payment fails.

Client
│
▼
Order Service
│ TX1: CREATE ORDER (PENDING) ✔
│
▼
Inventory Service
│ TX2: RESERVE STOCK ✔
│
▼
Payment Service
│ TX3: DEBIT MONEY ❌ FAIL

Now what?

7️⃣ Compensation Flow (CRITICAL)
Payment Service
│ TX3 failed
│
▼
Inventory Service
│ TX4: RELEASE STOCK (COMPENSATION)
│
▼
Order Service
│ TX5: MARK ORDER CANCELLED
│
▼
CONSISTENCY RESTORED ✅

This is the heart of Saga.

8️⃣ Saga with Compensation — Full Diagram
CREATE ORDER
│
▼
RESERVE STOCK
│
▼
DEBIT PAYMENT ──────┐
│ │ FAIL
▼ │
CONFIRM ORDER │
▼
RELEASE STOCK
│                   
▼
CANCEL ORDER

) Orchestrated Saga (Controller Service)
Saga Orchestrator
│
├── Order Service
├── Inventory Service
└── Payment Service

✔ Easy to reason
✔ Central control
❌ Single point of failure

B) Choreographed Saga (Event-driven)
OrderCreated → InventoryReserved → PaymentCompleted
↘ failure ↙
Compensation events

✔ Scalable
✔ Loosely coupled
❌ Harder to debug

10️⃣ ASCII — Choreographed Saga (Events)
Order Service
│
├─ publish OrderCreated
│
▼
Inventory Service
│
├─ reserve stock
├─ publish InventoryReserved
│
▼
Payment Service
│
├─ debit money
├─ publish PaymentFailed ❌
│
▼
Inventory Service
│
├─ release stock
│
▼
Order Service
│
├─ cancel order

ASCII Diagram — Retry Flow
READ (v=1)
│
├─ SUCCESS → COMMIT
│
└─ FAIL (version mismatch)
│
▼
RETRY
│
▼
READ (v=2)
│
▼
COMMIT

ASCII Diagram — Why This Fails
Controller
│
▼
InventoryService (Proxy)
│
├── reserveStockWithRetry()
│ │
│ └── reserveStock() ❌ bypasses proxy
│
└── @Transactional NOT applied

Maven 4+ does not support h2 db console, so used Maven 3+ for testing with H2 database console.

Imagine:
DB is down
Inventory service is overloaded
Lock keeps failing

Retry will:
Hammer DB
Increase latency
Cause cascading failure
That’s where Circuit Breaker comes in.

Interpretation:

Retry 3 times for optimistic locking
Open circuit if 50% failures
Stay open for 10 seconds

Circuit Breaker Logic:
Look at last 10 calls
If ≥5 fail, breaker OPENS
For 10 seconds → reject all calls
Then HALF-OPEN, allow 2 test calls

Client → Order Service → Inventory Service
Now this happens 👇

Client sends Reserve request
InventoryService processes it
Response gets LOST (network issue)
Client retries SAME request

❌ Without idempotency:
Stock deducted twice
Money charged twice
Production incident 🚨
💡 Retry + optimistic lock ≠ idempotency

Same request → same result → applied only once

Key rule:

Business effect must happen ONCE
Even if API is called N times

Client
|
|--(Idempotency-Key)
v
Controller
|
|-- check key exists?
|-- YES → return stored response
|-- NO → call service
|
|-- reserve stock
|
store response + key

❌ Don’t store huge responses
Store status + reference ID
Example: SUCCESS:ORDER_123

❌ Don’t store forever
Add TTL cleanup (cron/job)
Usually 24–72 hours

“Idempotency is implemented using a dedicated persistence entity keyed by an idempotency header.
This guarantees exactly-once execution across retries, network failures, and horizontally scaled services.”

“Idempotency records must be persisted in the same transaction as the business effect.
Any rollback must rollback both, otherwise exactly-once semantics are broken.”

“In Resilience4j, retry-exceptions apply only to retry logic.
Circuit breakers treat all exceptions as failures unless explicitly restricted using record-exceptions or
ignore-exceptions.”


Method throws exception
↓
Spring AOP proxy intercepts
↓
Resilience4j decides → fallback()
↓
Fallback return value is sent back
↓
Exception NEVER reaches your catch


server.port = 8081
🔍 Circuit Breaker state
GET http://localhost:8081/actuator/circuitbreakers
Example output:

{
"inventoryCB": {
"state": "CLOSED",
"failureRate": 20.0,
"bufferedCalls": 5,
"failedCalls": 1
}
}
🔁 Retry metrics
GET http://localhost:8081/actuator/retries
Example:

{
"inventoryRetry": {
"maxAttempts": 3,
"successfulCallsWithoutRetry": 1,
"successfulCallsWithRetry": 2,
"failedCalls": 1
}
}
📈 Live events (BEST for learning)
GET http://localhost:8081/actuator/circuitbreakerevents
You’ll literally see:

ERROR → OPEN → HALF_OPEN → CLOSED

Observe actuator
GET /actuator/circuitbreakers
GET /actuator/metrics/resilience4j.circuitbreaker.state
