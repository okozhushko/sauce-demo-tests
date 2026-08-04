package com.example.pages;

import org.openqa.selenium.By;

/**
 * Page Object for a product detail page on Swag Labs (see e.g.
 * {@code https://www.saucedemo.com/inventory-item.html?id=4}), reached by clicking a
 * product's name/image link from {@link InventoryPage}.
 *
 * <p>Unlike the inventory grid (where each "Add to cart" button is disambiguated by a
 * per-product {@code data-test} slug, e.g. {@code add-to-cart-sauce-labs-backpack}), this
 * page only ever shows one product, so its single button uses the generic
 * {@code data-test="add-to-cart"} (confirmed live).
 */
public class ProductPage extends BasePage {

    private static final By PRODUCT_NAME = By.cssSelector(".inventory_details_name");
    private static final By PRODUCT_PRICE = By.cssSelector(".inventory_details_price");
    private static final By CART_BUTTON = By.cssSelector(".inventory_details_desc_container button");
    private static final By BACK_TO_PRODUCTS_BUTTON = By.cssSelector("[data-test='back-to-products']");

    private final HeaderComponent header = new HeaderComponent();

    public String getProductName() {
        return getText(PRODUCT_NAME);
    }

    public String getProductPrice() {
        return getText(PRODUCT_PRICE);
    }

    public ProductPage addToCart() {
        click(CART_BUTTON);
        return this;
    }

    public boolean isInCart() {
        return getText(CART_BUTTON).equalsIgnoreCase("Remove");
    }

    public InventoryPage backToProducts() {
        click(BACK_TO_PRODUCTS_BUTTON);
        return new InventoryPage();
    }

    public int getCartItemCount() {
        return header.getCartItemCount();
    }

    public ProductPage waitForCartItemCount(int expected) {
        header.waitForCartItemCount(expected);
        return this;
    }

    public CartPage goToCart() {
        return header.goToCart();
    }
}
