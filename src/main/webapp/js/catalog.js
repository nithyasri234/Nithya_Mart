document.addEventListener("DOMContentLoaded", () => {

    // =========================================================
    // ELEMENTS
    // =========================================================

    const searchInput = document.getElementById("searchInput");
    const headerCategory = document.getElementById("headerCategory");
    const searchButton = document.getElementById("searchButton");
    const clearFilters = document.getElementById("clearFilters");
    const errorMessage = document.getElementById("errorMessage");


    // =========================================================
    // CATEGORIES
    // =========================================================

    const categories = [
        "Electronics",
        "Fashion",
        "Home",
        "Books",
        "Beauty",
        "Grocery"
    ];


    // =========================================================
    // PRODUCT DATA
    // =========================================================

    let allProducts = [];


    // =========================================================
    // LOAD PRODUCTS
    // =========================================================

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

                throw new Error(
                    "Unable to load products. HTTP status: "
                    + response.status
                );

            }


            const result = await response.json();


            /*
             * Your API may return either:
             *
             * 1. Direct array
             *    [
             *      {...},
             *      {...}
             *    ]
             *
             * OR
             *
             * 2. Response envelope
             *    {
             *      success: true,
             *      data: [...]
             *    }
             *
             * This code supports both.
             */

            if (Array.isArray(result)) {

                allProducts = result;

            } else if (
                result &&
                Array.isArray(result.data)
            ) {

                allProducts = result.data;

            } else {

                allProducts = [];

            }


            renderAllCategories(allProducts);


        } catch (error) {

            console.error(
                "Product loading error:",
                error
            );


            showError(
                "Unable to load products. Please try again."
            );


            renderEmptyCategories(
                "Products could not be loaded."
            );

        }

    }


    // =========================================================
    // RENDER ALL CATEGORIES
    // =========================================================

    function renderAllCategories(products) {

        categories.forEach(category => {

            const categoryProducts = products.filter(
                product => {

                    return normalizeCategory(
                        product.category
                    ) === normalizeCategory(category);

                }
            );


            renderCategory(
                category,
                categoryProducts
            );

        });

    }


    // =========================================================
    // RENDER ONE CATEGORY
    // =========================================================

    function renderCategory(
        category,
        products
    ) {

        const container = document.getElementById(
            "products-" + category
        );


        if (!container) {

            console.warn(
                "Product container not found:",
                "products-" + category
            );

            return;

        }


        container.innerHTML = "";


        /*
         * No products in this category
         */

        if (
            !products ||
            products.length === 0
        ) {

            container.innerHTML = `
                <div class="category-empty">
                    No products available in ${escapeHtml(category)}.
                </div>
            `;

            return;

        }


        /*
         * Create product cards
         */

        products.forEach(product => {

            const card = createProductCard(product);

            container.insertAdjacentHTML(
                "beforeend",
                card
            );

        });


        /*
         * Add-to-cart buttons
         */

        const buttons = container.querySelectorAll(
            ".home-add-cart-button"
        );


        buttons.forEach(button => {

            button.addEventListener(
                "click",
                () => {

                    const productId =
                        button.dataset.productId;

                    addToCart(
                        productId,
                        button
                    );

                }
            );

        });

    }


    // =========================================================
    // CREATE PRODUCT CARD
    // =========================================================

    function createProductCard(product) {

        const id = product.id;


        const name =
            product.name ||
            "Product";


        const description =
            product.description ||
            "No description available.";


        const category =
            product.category ||
            "Other";


        const price =
            product.price != null
                ? product.price
                : 0;


        const stock =
            product.stockQuantity != null
                ? Number(product.stockQuantity)
                : Number(product.stock_quantity || 0);


        /*
         * Support both:
         *
         * imageUrl
         *
         * and
         *
         * image_url
         */

        const imageUrl =
            product.imageUrl ||
            product.image_url ||
            "https://via.placeholder.com/500x400?text=NithyaMart";


        const outOfStock =
            stock <= 0;


        return `
            <article class="horizontal-product-card">


                <!-- Product Image -->

                <div class="home-product-image-wrap">

                    <img
                        class="home-product-image"
                        src="${escapeHtml(imageUrl)}"
                        alt="${escapeHtml(name)}"
                        loading="lazy"
                        onerror="this.onerror=null; this.src='https://via.placeholder.com/500x400?text=No+Image';"
                    >

                </div>


                <!-- Product Information -->

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

                        ${
                            outOfStock
                                ? "Out of stock"
                                : "In stock"
                        }

                    </div>


                    <!-- Buttons -->

                    <div class="home-product-actions">


                        <button
                            type="button"
                            class="home-add-cart-button"
                            data-product-id="${escapeHtml(id)}"
                            ${
                                outOfStock
                                    ? "disabled"
                                    : ""
                            }
                        >

                            ${
                                outOfStock
                                    ? "Out of Stock"
                                    : "Add to Cart"
                            }

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


    // =========================================================
    // ADD PRODUCT TO CART
    // =========================================================

    async function addToCart(
        productId,
        button
    ) {

        if (!productId) {

            return;

        }


        const originalText =
            button.textContent;


        button.disabled = true;

        button.textContent =
            "Adding...";


        try {

            const response = await fetch(
                "api/v1/cart?productId="
                + encodeURIComponent(productId)
                + "&quantity=1",
                {
                    method: "POST",
                    headers: {
                        "Accept": "application/json"
                    }
                }
            );


            /*
             * User is not logged in
             */

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


            button.textContent =
                "Added ✓";


            /*
             * Return button to normal state
             * after a short delay.
             */

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


    // =========================================================
    // SEARCH PRODUCTS
    // =========================================================

    function searchProducts() {

        const keyword =
            searchInput
                ? searchInput.value
                    .trim()
                    .toLowerCase()
                : "";


        const selectedCategory =
            headerCategory
                ? headerCategory.value
                : "";


        /*
         * If there is no search and no category,
         * show everything.
         */

        const filteredProducts =
            allProducts.filter(product => {

                const productName =
                    String(
                        product.name || ""
                    ).toLowerCase();


                const productDescription =
                    String(
                        product.description || ""
                    ).toLowerCase();


                const productCategory =
                    String(
                        product.category || ""
                    ).toLowerCase();


                const matchesKeyword =
                    keyword === ""
                    ||
                    productName.includes(keyword)
                    ||
                    productDescription.includes(keyword)
                    ||
                    productCategory.includes(keyword);


                const matchesCategory =
                    selectedCategory === ""
                    ||
                    normalizeCategory(
                        product.category
                    ) === normalizeCategory(
                        selectedCategory
                    );


                return (
                    matchesKeyword &&
                    matchesCategory
                );

            });


        renderAllCategories(
            filteredProducts
        );


        /*
         * Scroll to products after searching.
         */

        const productsSection =
            document.getElementById("products");


        if (productsSection) {

            productsSection.scrollIntoView({
                behavior: "smooth"
            });

        }

    }


    // =========================================================
    // CLEAR FILTERS
    // =========================================================

    function clearAllFilters() {

        if (searchInput) {

            searchInput.value = "";

        }


        if (headerCategory) {

            headerCategory.value = "";

        }


        showError("");


        renderAllCategories(
            allProducts
        );


        const productsSection =
            document.getElementById("products");


        if (productsSection) {

            productsSection.scrollIntoView({
                behavior: "smooth"
            });

        }

    }


    // =========================================================
    // EMPTY CATEGORIES
    // =========================================================

    function renderEmptyCategories(
        message
    ) {

        categories.forEach(category => {

            const container =
                document.getElementById(
                    "products-" + category
                );


            if (!container) {

                return;

            }


            container.innerHTML = `
                <div class="category-empty">
                    ${escapeHtml(message)}
                </div>
            `;

        });

    }


    // =========================================================
    // SHOW ERROR
    // =========================================================

    function showError(message) {

        if (!errorMessage) {

            return;

        }


        if (!message) {

            errorMessage.style.display =
                "none";

            errorMessage.textContent =
                "";

            return;

        }


        errorMessage.textContent =
            message;


        errorMessage.style.display =
            "block";

    }


    // =========================================================
    // CATEGORY NORMALIZATION
    // =========================================================

    function normalizeCategory(
        value
    ) {

        return String(
            value || ""
        )
            .trim()
            .toLowerCase();

    }


    // =========================================================
    // PRICE FORMAT
    // =========================================================

    function formatPrice(
        value
    ) {

        const number =
            Number(value);


        if (
            Number.isNaN(number)
        ) {

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


    // =========================================================
    // HTML ESCAPE
    // =========================================================

    function escapeHtml(
        value
    ) {

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


    // =========================================================
    // EVENT LISTENERS
    // =========================================================


    /*
     * Search button
     */

    if (searchButton) {

        searchButton.addEventListener(
            "click",
            searchProducts
        );

    }


    /*
     * Press Enter inside search box
     */

    if (searchInput) {

        searchInput.addEventListener(
            "keydown",
            event => {

                if (
                    event.key === "Enter"
                ) {

                    searchProducts();

                }

            }
        );

    }


    /*
     * Category selection
     */

    if (headerCategory) {

        headerCategory.addEventListener(
            "change",
            searchProducts
        );

    }


    /*
     * View All button
     */

    if (clearFilters) {

        clearFilters.addEventListener(
            "click",
            clearAllFilters
        );

    }


    // =========================================================
    // START
    // =========================================================

    loadProducts();

});