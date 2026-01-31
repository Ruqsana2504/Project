ALTER TABLE inventory
    ALTER COLUMN version SET DEFAULT 0;

INSERT INTO INVENTORY (product_id, available_quantity)
VALUES ('P1', 5);
INSERT INTO INVENTORY (product_id, available_quantity)
VALUES ('P2', 10);
INSERT INTO INVENTORY (product_id, available_quantity)
VALUES ('P3', 15);
INSERT INTO INVENTORY (product_id, available_quantity)
VALUES ('P4', 20);
INSERT INTO INVENTORY (product_id, available_quantity)
VALUES ('P5', 25);

select *
from inventory;


INSERT INTO idempotency_keys (idempotency_key, response)
VALUES ('123', 'SUCCESS:OrderCreated');
INSERT INTO idempotency_keys (idempotency_key, response)
VALUES ('124', 'SUCCESS:ReservationDone');
