Client
|
|-- POST /orders
|
|-- OrderService
|
|-- create order (PENDING)
|
|-- Inventory.reserve()
|       |
|       |-- fail? → order FAILED ❌
|
|-- Payment.pay()
|       |
|       |-- fail? → Inventory.release()
|                    order FAILED ❌
|
|-- order CONFIRMED ✅

OrderService TX START
|
|-- save Order (PENDING)
|
|-- Inventory.reserve()  ✔
|
|-- Payment.pay()        ❌
|
|-- Inventory.release()  ✔
|
|-- Order FAILED
TX COMMIT
