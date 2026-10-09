-- V6: Demo seller and demo products

-- Create a demo seller only if it does not already exist.
MERGE INTO users
    (name, email, password_hash, role)
KEY (email)
VALUES
    (
        'Demo Seller',
        'demo.seller@nithyamart.com',
        '$2a$12$abcdefghijklmnopqrstuuV5m8QJ9Q8YQ8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8',
        'SELLER'
    );


-- Electronics
INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Wireless Headphones',
    'Comfortable wireless headphones for music and calls.',
    1499.00,
    25,
    'Electronics',
    'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Wireless Headphones'
        AND category = 'Electronics'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Smart Watch',
    'Smart watch with fitness and notification features.',
    2499.00,
    20,
    'Electronics',
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Smart Watch'
        AND category = 'Electronics'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Laptop Backpack',
    'Durable backpack suitable for laptops and daily travel.',
    999.00,
    30,
    'Electronics',
    'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Laptop Backpack'
        AND category = 'Electronics'
  );


-- Fashion
INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Cotton T-Shirt',
    'Comfortable everyday cotton t-shirt.',
    599.00,
    40,
    'Fashion',
    'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Cotton T-Shirt'
        AND category = 'Fashion'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Classic Jeans',
    'Classic regular-fit jeans for everyday wear.',
    1299.00,
    25,
    'Fashion',
    'https://images.unsplash.com/photo-1542272604-780c96856592?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Classic Jeans'
        AND category = 'Fashion'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Casual Sneakers',
    'Lightweight sneakers for everyday use.',
    1799.00,
    18,
    'Fashion',
    'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Casual Sneakers'
        AND category = 'Fashion'
  );


-- Home
INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Table Lamp',
    'Modern table lamp for bedroom or study table.',
    799.00,
    22,
    'Home',
    'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Table Lamp'
        AND category = 'Home'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Cushion Set',
    'Soft decorative cushion set for your home.',
    699.00,
    35,
    'Home',
    'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Cushion Set'
        AND category = 'Home'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Storage Box',
    'Compact storage box for organizing household items.',
    449.00,
    30,
    'Home',
    'https://images.unsplash.com/photo-1591129841117-3adfd313e34f?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Storage Box'
        AND category = 'Home'
  );


-- Books
INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'The Mystery Novel',
    'An entertaining mystery novel for weekend reading.',
    399.00,
    20,
    'Books',
    'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'The Mystery Novel'
        AND category = 'Books'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Java Programming Guide',
    'Beginner-friendly guide to Java programming.',
    699.00,
    15,
    'Books',
    'https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Java Programming Guide'
        AND category = 'Books'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Study Guide',
    'Useful study guide for students and learners.',
    349.00,
    25,
    'Books',
    'https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Study Guide'
        AND category = 'Books'
  );


-- Beauty
INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Face Wash',
    'Gentle daily face wash for clean and fresh skin.',
    299.00,
    35,
    'Beauty',
    'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Face Wash'
        AND category = 'Beauty'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Moisturizer',
    'Lightweight moisturizer for daily skincare.',
    499.00,
    30,
    'Beauty',
    'https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Moisturizer'
        AND category = 'Beauty'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Daily Shampoo',
    'Everyday shampoo for clean and fresh hair.',
    399.00,
    25,
    'Beauty',
    'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Daily Shampoo'
        AND category = 'Beauty'
  );


-- Grocery
INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Premium Rice',
    'Quality rice suitable for everyday meals.',
    699.00,
    50,
    'Grocery',
    'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Premium Rice'
        AND category = 'Grocery'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Instant Coffee',
    'Rich instant coffee for a quick cup at home.',
    299.00,
    40,
    'Grocery',
    'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Instant Coffee'
        AND category = 'Grocery'
  );


INSERT INTO products
    (seller_id, name, description, price, stock_quantity, category, image_url)
SELECT
    id,
    'Butter Cookies',
    'Crispy and tasty cookies for snacks.',
    199.00,
    45,
    'Grocery',
    'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=600&q=80'
FROM users
WHERE email = 'demo.seller@nithyamart.com'
  AND NOT EXISTS (
      SELECT 1 FROM products
      WHERE name = 'Butter Cookies'
        AND category = 'Grocery'
  );