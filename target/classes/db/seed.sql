-- =========================================================
-- NITHYAMART DEMO SEED DATA
-- Demo password for seeded accounts: password
-- BCrypt hash
-- =========================================================

MERGE INTO users
    (name, email, password_hash, role)
KEY (email)
VALUES
(
    'NithyaMart Demo Seller',
    'demo-seller@nithyamart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'SELLER'
);

MERGE INTO users
    (name, email, password_hash, role)
KEY (email)
VALUES
(
    'NithyaMart Demo Buyer',
    'demo-buyer@nithyamart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'BUYER'
);

MERGE INTO users
    (name, email, password_hash, role)
KEY (email)
VALUES
(
    'NithyaMart Admin',
    'admin@nithyamart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN'
);


-- =========================================================
-- ELECTRONICS
-- =========================================================

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Samsung Galaxy M15',
    '5G smartphone with large display and long battery life.',
    14999.00,
    20,
    'Electronics',
    'https://placehold.co/500x400?text=Samsung+Galaxy'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Samsung Galaxy M15'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'HP Laptop 15',
    '15-inch laptop suitable for study, office and everyday work.',
    54999.00,
    12,
    'Electronics',
    'https://placehold.co/500x400?text=HP+Laptop'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'HP Laptop 15'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Boat Wireless Headphones',
    'Wireless headphones with comfortable ear cushions.',
    1999.00,
    35,
    'Electronics',
    'https://placehold.co/500x400?text=Headphones'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Boat Wireless Headphones'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Noise Smart Watch',
    'Smart watch with fitness tracking and notifications.',
    2999.00,
    25,
    'Electronics',
    'https://placehold.co/500x400?text=Smart+Watch'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Noise Smart Watch'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Sony LED Smart TV',
    'Full HD smart television for entertainment and streaming.',
    32999.00,
    8,
    'Electronics',
    'https://placehold.co/500x400?text=Sony+TV'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Sony LED Smart TV'
);


-- =========================================================
-- FASHION
-- =========================================================

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Men Cotton Shirt',
    'Comfortable casual cotton shirt for everyday wear.',
    899.00,
    40,
    'Fashion',
    'https://placehold.co/500x400?text=Cotton+Shirt'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Men Cotton Shirt'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Women Casual Kurti',
    'Comfortable printed kurti for casual occasions.',
    799.00,
    30,
    'Fashion',
    'https://placehold.co/500x400?text=Kurti'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Women Casual Kurti'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Denim Jeans',
    'Classic slim-fit denim jeans.',
    1299.00,
    25,
    'Fashion',
    'https://placehold.co/500x400?text=Jeans'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Denim Jeans'
);


-- =========================================================
-- HOME
-- =========================================================

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Study Table',
    'Compact wooden study table for home and office.',
    3499.00,
    15,
    'Home',
    'https://placehold.co/500x400?text=Study+Table'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Study Table'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'LED Table Lamp',
    'Modern LED lamp for study and work spaces.',
    699.00,
    30,
    'Home',
    'https://placehold.co/500x400?text=Table+Lamp'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'LED Table Lamp'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Kitchen Mixer Grinder',
    'Powerful mixer grinder for everyday kitchen use.',
    2499.00,
    18,
    'Home',
    'https://placehold.co/500x400?text=Mixer+Grinder'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Kitchen Mixer Grinder'
);


-- =========================================================
-- BOOKS
-- =========================================================

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Atomic Habits',
    'Popular book about building good habits and breaking bad ones.',
    499.00,
    30,
    'Books',
    'https://placehold.co/500x400?text=Atomic+Habits'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Atomic Habits'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Java Programming Guide',
    'Beginner-friendly Java programming reference.',
    699.00,
    20,
    'Books',
    'https://placehold.co/500x400?text=Java+Book'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Java Programming Guide'
);


-- =========================================================
-- BEAUTY
-- =========================================================

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Face Wash',
    'Gentle daily face cleanser.',
    299.00,
    50,
    'Beauty',
    'https://placehold.co/500x400?text=Face+Wash'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Face Wash'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Body Lotion',
    'Moisturizing body lotion for daily use.',
    399.00,
    45,
    'Beauty',
    'https://placehold.co/500x400?text=Body+Lotion'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Body Lotion'
);


-- =========================================================
-- GROCERY
-- =========================================================

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Premium Rice 5kg',
    'Premium quality rice for everyday cooking.',
    499.00,
    50,
    'Grocery',
    'https://placehold.co/500x400?text=Rice+5kg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Premium Rice 5kg'
);

INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    (SELECT id FROM users WHERE email = 'demo-seller@nithyamart.com'),
    'Sunflower Cooking Oil',
    'Refined sunflower cooking oil.',
    179.00,
    60,
    'Grocery',
    'https://placehold.co/500x400?text=Cooking+Oil'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Sunflower Cooking Oil'
);