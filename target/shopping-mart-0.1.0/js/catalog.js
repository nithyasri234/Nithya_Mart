document.addEventListener("DOMContentLoaded", () => {

    const productList = document.getElementById("productList");
    const loadingMessage = document.getElementById("loadingMessage");
    const errorMessage = document.getElementById("errorMessage");

    const searchInput = document.getElementById("searchInput");
    const categoryFilter = document.getElementById("categoryFilter");
    const searchButton = document.getElementById("searchButton");


    async function loadProducts() {

        loadingMessage.style.display = "block";
        errorMessage.style.display = "none";
        productList.innerHTML = "";

        try {

            const keyword = searchInput.value.trim();
            const category = categoryFilter.value;

            const params = new URLSearchParams();

            if (keyword) {
                params.append("keyword", keyword);
            }

            if (category) {
                params.append("category", category);
            }


            let url = "api/v1/products";

            if (params.toString()) {
                url += "?" + params.toString();
            }


            const response = await fetch(url);

            if (!response.ok) {
                throw new Error(
                    "Unable to load products."
                );
            }


            const products = await response.json();

            loadingMessage.style.display = "none";

            displayProducts(products);

        } catch (error) {

            console.error("Product loading error:", error);

            loadingMessage.style.display = "none";

            errorMessage.textContent =
                "Unable to load products. Please try again.";

            errorMessage.style.display = "block";
        }
    }


    function displayProducts(products) {

        productList.innerHTML = "";


        if (!Array.isArray(products) || products.length === 0) {

            productList.innerHTML = `
                <div class="message">
                    No products found.
                </div>
            `;

            return;
        }


        products.forEach(product => {

            const card = document.createElement("div");

            card.className = "product-card";


            const imageUrl =
                product.imageUrl ||
                "https://via.placeholder.com/400x300?text=NithyaMart";


            card.innerHTML = `

                <img
                    src="${escapeHtml(imageUrl)}"
                    alt="${escapeHtml(product.name || "Product")}"
                    onerror="this.src='https://via.placeholder.com/400x300?text=NithyaMart'"
                >

                <div class="product-info">

                    <span class="product-category">
                        ${escapeHtml(product.category || "Other")}
                    </span>

                    <h3>
                        ${escapeHtml(product.name || "Unnamed Product")}
                    </h3>

                    <p>
                        ${escapeHtml(product.description || "No description available.")}
                    </p>

                    <div class="product-price">
                        ₹${formatPrice(product.price)}
                    </div>

                    <p>
                        Stock:
                        ${product.stockQuantity ?? 0}
                    </p>

                    <a
                        class="btn"
                        href="product-details.html?id=${encodeURIComponent(product.id)}"
                    >
                        View Product
                    </a>

                </div>
            `;


            productList.appendChild(card);
        });
    }


    function formatPrice(price) {

        const number = Number(price);

        if (Number.isNaN(number)) {
            return "0.00";
        }

        return number.toFixed(2);
    }


    function escapeHtml(value) {

        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }


    searchButton.addEventListener(
        "click",
        loadProducts
    );


    searchInput.addEventListener(
        "keydown",
        event => {

            if (event.key === "Enter") {
                loadProducts();
            }

        }
    );


    categoryFilter.addEventListener(
        "change",
        loadProducts
    );


    loadProducts();

});