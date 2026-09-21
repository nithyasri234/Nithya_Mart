document.addEventListener("DOMContentLoaded", () => {

    const loadingMessage =
        document.getElementById("loadingMessage");

    const errorMessage =
        document.getElementById("errorMessage");

    const searchInput =
        document.getElementById("searchInput");

    const headerCategory =
        document.getElementById("headerCategory");

    const searchButton =
        document.getElementById("searchButton");

    const clearFilters =
        document.getElementById("clearFilters");


    const categories = [
        "Electronics",
        "Fashion",
        "Home",
        "Books",
        "Beauty",
        "Grocery"
    ];


    let allProducts = [];


    async function loadProducts() {

        if (loadingMessage) {
            loadingMessage.style.display = "block";
        }

        if (errorMessage) {
            errorMessage.style.display = "none";
        }

        try {

            const response =
                await fetch("api/v1/products");

            if (!response.ok) {
                throw new Error(
                    "Unable to load products."
                );
            }

            allProducts =
                await response.json();

            renderAllCategories(
                allProducts
            );

        } catch (error) {

            console.error(
                "Product loading error:",
                error
            );

            if (errorMessage) {
                errorMessage.textContent =
                    "Unable to load products. Please try again.";

                errorMessage.style.display =
                    "block";
            }

        } finally {

            if (loadingMessage) {
                loadingMessage.style.display =
                    "none";
            }

        }
    }


    function renderAllCategories(products) {

        categories.forEach(category => {

            const categoryProducts =
                products.filter(product =>
                    String(product.category || "")
                        .toLowerCase()
                        === category.toLowerCase()
                );

            renderCategory(
                category,
                categoryProducts
            );

        });
    }


    function renderCategory(
        category,
        products
    ) {

        const container =
            document.getElementById(
                "products-" + category
            );

        if (!container) {
            return;
        }

        container.innerHTML = "";


        if (
            !Array.isArray(products)
            || products.length === 0
        ) {

            container.innerHTML = `
                <div class="category-empty">
                    No products available in ${escapeHtml(category)} yet.
                </div>
            `;

            return;
        }


        products.forEach(product => {

            const card =
                document.createElement("article");

            card.className =
                "horizontal-product-card";


            const imageUrl =
                product.imageUrl
                || "https://placehold.co/500x400?text=NithyaMart";


            card.innerHTML = `

                <div class="home-product-image-wrap">

                    <img
                        class="home-product-image"
                        src="${escapeHtml(imageUrl)}"
                        alt="${escapeHtml(
                            product.name || "Product"
                        )}"
                    >

                </div>


                <div class="home-product-content">

                    <span class="home-product-category">
                        ${escapeHtml(
                            product.category || "Other"
                        )}
                    </span>


                    <h3>
                        ${escapeHtml(
                            product.name || "Product"
                        )}
                    </h3>


                    <p class="home-product-description">
                        ${escapeHtml(
                            product.description
                            || "No description available."
                        )}
                    </p>


                    <div class="home-product-price">
                        ₹${formatPrice(product.price)}
                    </div>


                    <div class="home-product-stock">
                        ${Number(product.stockQuantity) > 0
                            ? "In stock"
                            : "Out of stock"}
                    </div>


                    <div class="home-product-actions">

                        <button
                            type="button"
                            class="home-add-cart-button"
                            data-product-id="${product.id}"
                            ${Number(product.stockQuantity) <= 0
                                ? "disabled"
                                : ""}>
                            Add to Cart
                        </button>


                        <a
                            class="home-view-button"
                            href="product-details.html?id=${encodeURIComponent(
                                product.id
                            )}">
                            View
                        </a>

                    </div>

                </div>
            `;


            container.appendChild(card);

        });


        attachCartButtons(container);
    }


    function attachCartButtons(container) {

        container
            .querySelectorAll(".home-add-cart-button")
            .forEach(button => {

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


    async function addToCart(
        productId,
        button
    ) {

        button.disabled = true;
        button.textContent = "Adding...";


        try {

            /*
             * CartServlet currently expects
             * productId and quantity as request
             * parameters.
             */
            const url =
                "api/v1/cart?productId="
                + encodeURIComponent(productId)
                + "&quantity=1";


            const response =
                await fetch(
                    url,
                    {
                        method: "POST"
                    }
                );


            if (response.status === 401) {

                alert(
                    "Please login as a buyer to add products to your cart."
                );

                window.location.href =
                    "login.html";

                return;
            }


            if (!response.ok) {

                let message =
                    "Unable to add product to cart.";

                try {

                    const data =
                        await response.json();

                    if (data.message) {
                        message =
                            data.message;
                    }

                } catch (error) {
                    // Keep default message.
                }

                throw new Error(message);
            }


            button.textContent =
                "Added ✓";


            setTimeout(() => {

                button.textContent =
                    "Add to Cart";

                button.disabled =
                    false;

            }, 1200);


        } catch (error) {

            console.error(
                "Add to cart error:",
                error
            );

            alert(
                error.message
                || "Unable to add product to cart."
            );

            button.disabled =
                false;

            button.textContent =
                "Add to Cart";
        }
    }


    function formatPrice(price) {

        const number =
            Number(price);

        if (Number.isNaN(number)) {
            return "0.00";
        }

        return number.toFixed(2);
    }


    function escapeHtml(value) {

        return String(value)
            .replaceAll(
                "&",
                "&amp;"
            )
            .replaceAll(
                "<",
                "&lt;"
            )
            .replaceAll(
                ">",
                "&gt;"
            )
            .replaceAll(
                '"',
                "&quot;"
            )
            .replaceAll(
                "'",
                "&#039;"
            );
    }


    /*
     * Search
     */
    if (searchButton) {

        searchButton.addEventListener(
            "click",
            () => {

                const keyword =
                    searchInput
                        ? searchInput.value
                            .trim()
                            .toLowerCase()
                        : "";

                const category =
                    headerCategory
                        ? headerCategory.value
                        : "";


                const filtered =
                    allProducts.filter(product => {

                        const matchesKeyword =
                            !keyword
                            || String(
                                product.name || ""
                            )
                                .toLowerCase()
                                .includes(keyword)
                            || String(
                                product.description || ""
                            )
                                .toLowerCase()
                                .includes(keyword);


                        const matchesCategory =
                            !category
                            || String(
                                product.category || ""
                            )
                                .toLowerCase()
                            === category.toLowerCase();


                        return (
                            matchesKeyword
                            && matchesCategory
                        );

                    });


                renderAllCategories(
                    filtered
                );


                document
                    .getElementById("products")
                    ?.scrollIntoView({
                        behavior: "smooth"
                    });

            }
        );

    }


    /*
     * Search with Enter
     */
    if (searchInput) {

        searchInput.addEventListener(
            "keydown",
            event => {

                if (event.key === "Enter") {

                    searchButton?.click();

                }

            }
        );

    }


    /*
     * View All
     */
    if (clearFilters) {

        clearFilters.addEventListener(
            "click",
            () => {

                if (searchInput) {
                    searchInput.value = "";
                }

                if (headerCategory) {
                    headerCategory.value = "";
                }

                renderAllCategories(
                    allProducts
                );

            }
        );

    }


    /*
     * Category View All buttons
     */
    document
        .querySelectorAll(
            ".view-all-category"
        )
        .forEach(button => {

            button.addEventListener(
                "click",
                () => {

                    const category =
                        button.dataset.category;

                    const products =
                        allProducts.filter(
                            product =>
                                String(
                                    product.category || ""
                                ).toLowerCase()
                                === category.toLowerCase()
                        );

                    renderCategory(
                        category,
                        products
                    );

                    document
                        .getElementById(
                            "products-" + category
                        )
                        ?.scrollIntoView({
                            behavior: "smooth",
                            block: "center"
                        });

                }
            );

        });


    /*
     * Initial load
     */
    loadProducts();

});