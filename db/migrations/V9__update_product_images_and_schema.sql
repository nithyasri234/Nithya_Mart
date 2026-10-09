-- ========================================================
-- V9: PRODUCT IMAGES, ORDER NUMBER, AND ITEM SNAPSHOTS
-- ========================================================

-- 1. ADD ORDER NUMBER TO ORDERS
ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_number VARCHAR(50);

-- 2. ADD PRODUCT SNAPSHOT COLUMNS TO ORDER_ITEMS
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS product_name VARCHAR(150);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- 3. UPDATE DEMO AND SEEDED PRODUCTS WITH REAL HIGH-QUALITY PRODUCT IMAGES

-- Electronics
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Samsung Galaxy M15';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=600&q=80'
WHERE name = 'HP Laptop 15';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('Boat Wireless Headphones', 'Wireless Headphones');

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('Noise Smart Watch', 'Smart Watch');

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Sony LED Smart TV';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Laptop Backpack';

-- Fashion
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('Men Cotton Shirt', 'Cotton T-Shirt');

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Women Casual Kurti';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1542272604-780c96856592?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('Denim Jeans', 'Classic Jeans');

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Casual Sneakers';

-- Home
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Study Table';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('LED Table Lamp', 'Table Lamp');

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1570222094114-d054a817e56b?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Kitchen Mixer Grinder';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Cushion Set';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1591129841117-3adfd313e34f?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Storage Box';

-- Books
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('Atomic Habits', 'The Mystery Novel');

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('Java Programming Guide', 'Study Guide');

-- Beauty
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Face Wash';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('Body Lotion', 'Moisturizer');

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Daily Shampoo';

-- Grocery
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80'
WHERE name IN ('Premium Rice 5kg', 'Premium Rice');

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Sunflower Cooking Oil';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Instant Coffee';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=600&q=80'
WHERE name = 'Butter Cookies';
