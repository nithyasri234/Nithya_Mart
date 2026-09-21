document.addEventListener("DOMContentLoaded", () => {

    const searchInput = document.getElementById("searchInput");
    const headerCategory = document.getElementById("headerCategory");
    const searchButton = document.getElementById("searchButton");
    const clearFilters = document.getElementById("clearFilters");
    const errorMessage = document.getElementById("errorMessage");

    const categories = [
        "Electronics",
        "Fashion",
        "Home",
        "Books",
        "Beauty",
        "Grocery"
    ];

    let allProducts = [];

    /*
     * Demo products for homepage display.
     * These are used if the database/API is unavailable.
     */
    const demoProducts = [

        // ELECTRONICS
        {
            id: "demo-electronics-1",
            name: "Wireless Headphones",
            description: "Comfortable wireless headphones",
            price: 1499,
            stockQuantity: 20,
            category: "Electronics",
            imageUrl: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-electronics-2",
            name: "Smart Watch",
            description: "Smart watch with fitness tracking",
            price: 2299,
            stockQuantity: 15,
            category: "Electronics",
            imageUrl: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-electronics-3",
            name: "Wireless Keyboard",
            description: "Slim wireless keyboard",
            price: 899,
            stockQuantity: 25,
            category: "Electronics",
            imageUrl: "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-electronics-4",
            name: "Bluetooth Speaker",
            description: "Portable Bluetooth speaker",
            price: 1299,
            stockQuantity: 18,
            category: "Electronics",
            imageUrl: "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-electronics-5",
            name: "Fast Charger",
            description: "USB-C fast charging adapter",
            price: 699,
            stockQuantity: 30,
            category: "Electronics",
            imageUrl: "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=600&q=80"
        },

        // FASHION
        {
            id: "demo-fashion-1",
            name: "Cotton T-Shirt",
            description: "Soft cotton casual T-shirt",
            price: 499,
            stockQuantity: 30,
            category: "Fashion",
            imageUrl: "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-fashion-2",
            name: "Denim Jacket",
            description: "Classic denim jacket",
            price: 1599,
            stockQuantity: 15,
            category: "Fashion",
            imageUrl: "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-fashion-3",
            name: "Casual Sneakers",
            description: "Comfortable everyday sneakers",
            price: 1899,
            stockQuantity: 20,
            category: "Fashion",
            imageUrl: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-fashion-4",
            name: "Ladies Handbag",
            description: "Elegant everyday handbag",
            price: 1299,
            stockQuantity: 15,
            category: "Fashion",
            imageUrl: "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-fashion-5",
            name: "Classic Sunglasses",
            description: "Stylish everyday sunglasses",
            price: 799,
            stockQuantity: 25,
            category: "Fashion",
            imageUrl: "https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=600&q=80"
        },

        // HOME
        {
            id: "demo-home-1",
            name: "Modern Table Lamp",
            description: "Modern decorative table lamp",
            price: 899,
            stockQuantity: 20,
            category: "Home",
            imageUrl: "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-home-2",
            name: "Plant Pot",
            description: "Decorative indoor plant pot",
            price: 399,
            stockQuantity: 30,
            category: "Home",
            imageUrl: "https://images.unsplash.com/photo-1485955900006-10f4d324d411?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-home-3",
            name: "Coffee Mug",
            description: "Beautiful ceramic coffee mug",
            price: 299,
            stockQuantity: 40,
            category: "Home",
            imageUrl: "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-home-4",
            name: "Soft Cushion",
            description: "Comfortable decorative cushion",
            price: 449,
            stockQuantity: 25,
            category: "Home",
            imageUrl: "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-home-5",
            name: "Wall Clock",
            description: "Modern wall clock",
            price: 599,
            stockQuantity: 18,
            category: "Home",
            imageUrl: "https://images.unsplash.com/photo-1563861826100-9cb868fdbe1c?auto=format&fit=crop&w=600&q=80"
        },

        // BOOKS
        {
            id: "demo-books-1",
            name: "The Alchemist",
            description: "Inspirational fiction novel",
            price: 399,
            stockQuantity: 20,
            category: "Books",
            imageUrl: "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-books-2",
            name: "Modern Java Programming",
            description: "Beginner Java programming guide",
            price: 699,
            stockQuantity: 15,
            category: "Books",
            imageUrl: "https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-books-3",
            name: "Web Development Basics",
            description: "HTML CSS and JavaScript guide",
            price: 599,
            stockQuantity: 18,
            category: "Books",
            imageUrl: "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-books-4",
            name: "Business Management",
            description: "Introduction to business management",
            price: 499,
            stockQuantity: 12,
            category: "Books",
            imageUrl: "https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-books-5",
            name: "Creative Thinking",
            description: "Improve creativity and problem solving",
            price: 449,
            stockQuantity: 16,
            category: "Books",
            imageUrl: "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80"
        },

        // BEAUTY
        {
            id: "demo-beauty-1",
            name: "Face Moisturizer",
            description: "Daily moisturizing face cream",
            price: 599,
            stockQuantity: 25,
            category: "Beauty",
            imageUrl: "https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-beauty-2",
            name: "Lipstick Set",
            description: "Collection of everyday lipstick shades",
            price: 799,
            stockQuantity: 20,
            category: "Beauty",
            imageUrl: "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-beauty-3",
            name: "Perfume",
            description: "Elegant everyday fragrance",
            price: 999,
            stockQuantity: 18,
            category: "Beauty",
            imageUrl: "https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-beauty-4",
            name: "Makeup Brush Set",
            description: "Soft makeup brush collection",
            price: 699,
            stockQuantity: 22,
            category: "Beauty",
            imageUrl: "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-beauty-5",
            name: "Face Care Set",
            description: "Daily skincare collection",
            price: 899,
            stockQuantity: 15,
            category: "Beauty",
            imageUrl: "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=600&q=80"
        },

        // GROCERY
        {
            id: "demo-grocery-1",
            name: "Fresh Apples",
            description: "Fresh and naturally sweet apples",
            price: 180,
            stockQuantity: 50,
            category: "Grocery",
            imageUrl: "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-grocery-2",
            name: "Organic Rice",
            description: "Quality rice for everyday meals",
            price: 450,
            stockQuantity: 35,
            category: "Grocery",
            imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-grocery-3",
            name: "Fresh Vegetables",
            description: "Fresh assorted vegetables",
            price: 250,
            stockQuantity: 30,
            category: "Grocery",
            imageUrl: "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-grocery-4",
            name: "Premium Coffee",
            description: "Rich roasted coffee",
            price: 399,
            stockQuantity: 25,
            category: "Grocery",
            imageUrl: "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=600&q=80"
        },
        {
            id: "demo-grocery-5",
            name: "Breakfast Cereal",
            description: "Crunchy breakfast cereal",
            price: 299,
            stockQuantity: 28,
            category: "Grocery",
            imageUrl: "https://images.unsplash.com/photo-1521483451569-e33803c0330c?auto=format&fit=crop&w=600&q=80"
        }
    ];

    async function loadProducts() {
        showError("");

        try {
            const response = await fetch("api/v1/products", {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error("API unavailable");
            }

            const result = await response.json();

            if (Array.isArray(result)) {
                allProducts = result;
            } else if (result && Array.isArray(result.data)) {
                allProducts = result.data;
            } else {
                allProducts = [];
            }

            if (allProducts.length === 0) {
                allProducts = demoProducts;
            }

            renderAllCategories(allProducts);

        } catch (error) {

            console.warn(
                "Product API unavailable. Showing homepage demo products."
            );

            /*
             * Important:
             * Even if the database/API fails,
             * the homepage will still show products and images.
             */
            allProducts = demoProducts;

            renderAllCategories(allProducts);

            showError("");
        }
    }

    function renderAllCategories(products) {

        categories.forEach(category => {

            const categoryProducts = products.filter(product =>
                normalizeCategory(product.category) ===
                normalizeCategory(category)
            );

            renderCategory(category, categoryProducts);
        });
    }

    function renderCategory(category, products) {

        const container =
            document.getElementById("products-" + category);

        if (!container) {
            return;
        }

        container.innerHTML = "";

        if (!products || products.length === 0) {

            container.innerHTML = `
                <div class="category-empty">
                    No products available.
                </div>
            `;

            return;
        }

        products.forEach(product => {

            const card = createProductCard(product);

            container.insertAdjacentHTML(
                "beforeend",
                card
            );
        });

        const buttons =
            container.querySelectorAll(".home-add-cart-button");

        buttons.forEach(button => {

            button.addEventListener("click", () => {

                const productId =
                    button.dataset.productId;

                addToCart(productId, button);
            });
        });
    }

    function createProductCard(product) {

        const id = product.id;

        const name =
            product.name || "Product";

        const description =
            product.description ||
            "Quality product from NithyaMart.";

        const category =
            product.category || "Other";

        const price =
            product.price != null
                ? product.price
                : 0;

        const stock =
            product.stockQuantity != null
                ? Number(product.stockQuantity)
                : Number(product.stock_quantity || 0);

        const imageUrl =
            product.imageUrl ||
            product.image_url ||
            "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=600&q=80";

        const outOfStock = stock <= 0;

        return `
            <article class="horizontal-product-card">

                <div class="home-product-image-wrap">

                    <img
                        class="home-product-image"
                        src="${escapeHtml(imageUrl)}"
                        alt="${escapeHtml(name)}"
                        loading="lazy"
                        onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=600&q=80';"
                    >

                </div>

                <div class="home-product-content">

                    <span class="home-product-category">
                        ${escapeHtml(category)}
                    </span>

                    <h3>
                        ${escapeHtml(name)}
                    </h3>

                    <p class="home-product-description">
                        ${escapeHtml(description)}
                    </p>

                    <div class="home-product-price">
                        ₹${formatPrice(price)}
                    </div>

                    <div class="home-product-stock">
                        ${outOfStock ? "Out of stock" : "In stock"}
                    </div>

                    <div class="home-product-actions">

                        <button
                            type="button"
                            class="home-add-cart-button"
                            data-product-id="${escapeHtml(id)}"
                            ${outOfStock ? "disabled" : ""}
                        >
                            ${outOfStock
                                ? "Out of Stock"
                                : "Add to Cart"}
                        </button>

                        <a
                            class="home-view-button"
                            href="product-details.html?id=${encodeURIComponent(id)}"
                        >
                            View
                        </a>

                    </div>

                </div>

            </article>
        `;
    }

    async function addToCart(productId, button) {

        /*
         * Demo products are only for homepage display.
         * Real database products continue to use the cart API.
         */
        if (!productId ||
            String(productId).startsWith("demo-")) {

            button.textContent = "Login to Shop";

            setTimeout(() => {
                button.textContent = "Add to Cart";
            }, 1500);

            return;
        }

        const originalText =
            button.textContent;

        button.disabled = true;
        button.textContent = "Adding...";

        try {

            const response = await fetch(
                "api/v1/cart?productId=" +
                encodeURIComponent(productId) +
                "&quantity=1",
                {
                    method: "POST",
                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

            if (response.status === 401) {

                window.location.href =
                    "login.html";

                return;
            }

            if (!response.ok) {
                throw new Error(
                    "Unable to add product to cart."
                );
            }

            button.textContent = "Added ✓";

            setTimeout(() => {

                button.textContent =
                    originalText;

                button.disabled = false;

            }, 1500);

        } catch (error) {

            console.error(
                "Add to cart error:",
                error
            );

            button.textContent =
                "Try Again";

            setTimeout(() => {

                button.textContent =
                    originalText;

                button.disabled = false;

            }, 1500);
        }
    }

    function searchProducts() {

        const keyword =
            searchInput
                ? searchInput.value.trim().toLowerCase()
                : "";

        const selectedCategory =
            headerCategory
                ? headerCategory.value
                : "";

        const filteredProducts =
            allProducts.filter(product => {

                const productName =
                    String(product.name || "")
                        .toLowerCase();

                const productDescription =
                    String(product.description || "")
                        .toLowerCase();

                const productCategory =
                    String(product.category || "")
                        .toLowerCase();

                const matchesKeyword =
                    keyword === "" ||
                    productName.includes(keyword) ||
                    productDescription.includes(keyword) ||
                    productCategory.includes(keyword);

                const matchesCategory =
                    selectedCategory === "" ||
                    normalizeCategory(product.category) ===
                    normalizeCategory(selectedCategory);

                return matchesKeyword &&
                    matchesCategory;
            });

        renderAllCategories(filteredProducts);

        const productsSection =
            document.getElementById("products");

        if (productsSection) {

            productsSection.scrollIntoView({
                behavior: "smooth"
            });
        }
    }

    function clearAllFilters() {

        if (searchInput) {
            searchInput.value = "";
        }

        if (headerCategory) {
            headerCategory.value = "";
        }

        showError("");

        renderAllCategories(allProducts);

        const productsSection =
            document.getElementById("products");

        if (productsSection) {

            productsSection.scrollIntoView({
                behavior: "smooth"
            });
        }
    }

    function showError(message) {

        if (!errorMessage) {
            return;
        }

        if (!message) {

            errorMessage.style.display = "none";
            errorMessage.textContent = "";

            return;
        }

        errorMessage.textContent = message;
        errorMessage.style.display = "block";
    }

    function normalizeCategory(value) {

        return String(value || "")
            .trim()
            .toLowerCase();
    }

    function formatPrice(value) {

        const number = Number(value);

        if (Number.isNaN(number)) {
            return "0.00";
        }

        return number.toLocaleString(
            "en-IN",
            {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }
        );
    }

    function escapeHtml(value) {

        if (
            value === null ||
            value === undefined
        ) {
            return "";
        }

        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    if (searchButton) {
        searchButton.addEventListener(
            "click",
            searchProducts
        );
    }

    if (searchInput) {
        searchInput.addEventListener(
            "keydown",
            event => {

                if (event.key === "Enter") {
                    searchProducts();
                }

            }
        );
    }

    if (headerCategory) {
        headerCategory.addEventListener(
            "change",
            searchProducts
        );
    }

    if (clearFilters) {
        clearFilters.addEventListener(
            "click",
            clearAllFilters
        );
    }

    loadProducts();
});