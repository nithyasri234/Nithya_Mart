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
    'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1542272604-780c96856592?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1570222094114-d054a817e56b?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80'
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
    'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=600&q=80'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Sunflower Cooking Oil'
);