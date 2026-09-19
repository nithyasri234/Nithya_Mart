document.addEventListener("DOMContentLoaded", () => {

    const cartLoading =
        document.getElementById("cartLoading");

    const cartError =
        document.getElementById("cartError");

    const emptyCart =
        document.getElementById("emptyCart");

    const cartItems =
        document.getElementById("cartItems");

    const cartSummary =
        document.getElementById("cartSummary");

    const cartTotal =
        document.getElementById("cartTotal");


    loadCart();


    async function loadCart() {

        showLoading();


        try {

            const response =
                await fetch("api/v1/cart");


            /*
             * Buyer is not logged in
             */

            if (response.status === 401) {

                showError(
                    "Please login as a buyer to view your cart."
                );

                setTimeout(() => {

                    window.location.href =
                        "login.html";

                }, 1500);

                return;
            }


            if (!response.ok) {

                throw new Error(
                    "Unable to load your cart."
                );

            }


            const data =
                await response.json();


            displayCart(data);


        } catch (error) {

            console.error(
                "Cart loading error:",
                error
            );

            showError(
                error.message ||
                "Unable to load your cart."
            );

        }

    }


    function displayCart(data) {

        hideAllMessages();


        /*
         * Our CartServlet returns a list
         * of cart items.
         */

        const items =
            Array.isArray(data)
                ? data
                : data.items;


        if (
            !Array.isArray(items)
            || items.length === 0
        ) {

            emptyCart.style.display =
                "block";

            return;
        }


        cartItems.style.display =
            "block";


        cartSummary.style.display =
            "block";


        cartItems.innerHTML =
            "";


        let total = 0;


        items.forEach(item => {

            const product =
                item.product;


            const quantity =
                Number(item.quantity) || 0;


            const price =
                Number(product.price) || 0;


            const subtotal =
                price * quantity;


            total += subtotal;


            const itemElement =
                document.createElement("div");


            itemElement.className =
                "cart-item";


            const imageUrl =
                product.imageUrl ||
                "https://via.placeholder.com/100x100?text=NithyaMart";


            itemElement.innerHTML = `

                <img
                    class="cart-item-image"
                    src="${escapeHtml(imageUrl)}"
                    alt="${escapeHtml(product.name || "Product")}"
                >

                <div class="cart-item-name">
                    ${escapeHtml(product.name || "Product")}
                </div>

                <div class="cart-item-price">
                    ₹${price.toFixed(2)}
                </div>

                <div class="cart-quantity">

                    <input
                        type="number"
                        min="1"
                        value="${quantity}"
                        data-product-id="${product.id}"
                        class="quantity-input"
                    >

                    <button
                        type="button"
                        class="update-button"
                        data-product-id="${product.id}">

                        Update

                    </button>

                </div>

                <div class="cart-item-total">

                    ₹${subtotal.toFixed(2)}

                </div>

                <div class="cart-item-remove">

                    <button
                        type="button"
                        class="remove-button"
                        data-product-id="${product.id}">

                        Remove

                    </button>

                </div>

            `;


            cartItems.appendChild(
                itemElement
            );

        });


        cartTotal.textContent =
            "Total: ₹" +
            total.toFixed(2);


        /*
         * Update buttons
         */

        document
            .querySelectorAll(".update-button")
            .forEach(button => {

                button.addEventListener(
                    "click",
                    () => {

                        const productId =
                            button.dataset.productId;


                        const input =
                            document.querySelector(
                                `.quantity-input[data-product-id="${productId}"]`
                            );


                        const quantity =
                            Number(input.value);


                        updateQuantity(
                            productId,
                            quantity
                        );

                    }
                );

            });


        /*
         * Remove buttons
         */

        document
            .querySelectorAll(".remove-button")
            .forEach(button => {

                button.addEventListener(
                    "click",
                    () => {

                        const productId =
                            button.dataset.productId;


                        removeItem(
                            productId
                        );

                    }
                );

            });

    }


    async function updateQuantity(
        productId,
        quantity
    ) {

        if (
            !Number.isInteger(quantity)
            || quantity < 1
        ) {

            alert(
                "Quantity must be at least 1."
            );

            return;
        }


        try {

            const response =
                await fetch(
                    "api/v1/cart/" +
                    encodeURIComponent(productId),
                    {
                        method: "PUT",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({
                            quantity: quantity
                        })
                    }
                );


            if (response.status === 401) {

                window.location.href =
                    "login.html";

                return;
            }


            if (!response.ok) {

                throw new Error(
                    "Unable to update quantity."
                );

            }


            await loadCart();


        } catch (error) {

            console.error(
                "Update cart error:",
                error
            );

            showError(
                error.message
            );

        }

    }


    async function removeItem(productId) {

        const confirmed =
            confirm(
                "Remove this product from your cart?"
            );


        if (!confirmed) {
            return;
        }


        try {

            const response = await fetch(
    "api/v1/cart?productId=" +
    encodeURIComponent(productId) +
    "&quantity=" +
    encodeURIComponent(quantity),
    {
        method: "PUT"
    }
);


            if (response.status === 401) {

                window.location.href =
                    "login.html";

                return;
            }


            if (!response.ok) {

                throw new Error(
                    "Unable to remove product."
                );

            }


            await loadCart();


        } catch (error) {

            console.error(
                "Remove cart item error:",
                error
            );

            showError(
                error.message
            );

        }

    }


    function showLoading() {

        cartLoading.style.display =
            "block";

        cartError.style.display =
            "none";

        emptyCart.style.display =
            "none";

        cartItems.style.display =
            "none";

        cartSummary.style.display =
            "none";

    }


    function hideAllMessages() {

        cartLoading.style.display =
            "none";

        cartError.style.display =
            "none";

        emptyCart.style.display =
            "none";

    }


    function showError(message) {

        cartLoading.style.display =
            "none";

        emptyCart.style.display =
            "none";

        cartItems.style.display =
            "none";

        cartSummary.style.display =
            "none";


        cartError.textContent =
            message;

        cartError.style.display =
            "block";

    }


    function escapeHtml(value) {

        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");

    }

});