-- Block duplicate names under the same parent.
-- NULLS NOT DISTINCT (Postgres 15+) also blocks two top-level groups with the same name.
ALTER TABLE category ADD CONSTRAINT uq_category_parent_name UNIQUE NULLS NOT DISTINCT (parent_id, name);

-- Seed categories: top-level groups (depth 0) and their items (depth 1).

INSERT INTO category (name, depth, parent_id) VALUES
    ('Living',        0, NULL),
    ('Food',          0, NULL),
    ('Shopping',      0, NULL),
    ('Car',           0, NULL),
    ('Lifestyle',     0, NULL),
    ('Health',        0, NULL),
    ('Not spending',  0, NULL),
    ('Uncategorised', 0, NULL);

INSERT INTO category (name, depth, parent_id) VALUES
    ('Rent',             1, (SELECT id FROM category WHERE name = 'Living'       AND depth = 0)),
    ('Power',            1, (SELECT id FROM category WHERE name = 'Living'       AND depth = 0)),
    ('Wifi',             1, (SELECT id FROM category WHERE name = 'Living'       AND depth = 0)),
    ('Mobile',           1, (SELECT id FROM category WHERE name = 'Living'       AND depth = 0)),
    ('Grocery',          1, (SELECT id FROM category WHERE name = 'Food'         AND depth = 0)),
    ('Outside Food',     1, (SELECT id FROM category WHERE name = 'Food'         AND depth = 0)),
    ('Alcohol',          1, (SELECT id FROM category WHERE name = 'Food'         AND depth = 0)),
    ('Household',        1, (SELECT id FROM category WHERE name = 'Shopping'     AND depth = 0)),
    ('Personal',         1, (SELECT id FROM category WHERE name = 'Shopping'     AND depth = 0)),
    ('Petrol',           1, (SELECT id FROM category WHERE name = 'Car'          AND depth = 0)),
    ('Car Insurance',    1, (SELECT id FROM category WHERE name = 'Car'          AND depth = 0)),
    ('Rego',             1, (SELECT id FROM category WHERE name = 'Car'          AND depth = 0)),
    ('WOF',              1, (SELECT id FROM category WHERE name = 'Car'          AND depth = 0)),
    ('Travel',           1, (SELECT id FROM category WHERE name = 'Lifestyle'    AND depth = 0)),
    ('Activity',         1, (SELECT id FROM category WHERE name = 'Lifestyle'    AND depth = 0)),
    ('Social',           1, (SELECT id FROM category WHERE name = 'Lifestyle'    AND depth = 0)),
    ('Education',        1, (SELECT id FROM category WHERE name = 'Lifestyle'    AND depth = 0)),
    ('Health Insurance', 1, (SELECT id FROM category WHERE name = 'Health'       AND depth = 0)),
    ('Treatment',        1, (SELECT id FROM category WHERE name = 'Health'       AND depth = 0)),
    ('Beauty',           1, (SELECT id FROM category WHERE name = 'Health'       AND depth = 0)),
    ('Income',           1, (SELECT id FROM category WHERE name = 'Not spending' AND depth = 0)),
    ('Transfer',         1, (SELECT id FROM category WHERE name = 'Not spending' AND depth = 0)),
    ('Refund',           1, (SELECT id FROM category WHERE name = 'Not spending' AND depth = 0));
