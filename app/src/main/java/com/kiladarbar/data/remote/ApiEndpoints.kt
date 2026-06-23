package com.kiladarbar.data.remote

object ApiEndpoints {
    private const val V1 = "v1/"

    // ── Auth ──────────────────────────────────────────────────────────────────
    const val SEND_OTP    = "${V1}auth/send-otp"
    const val VERIFY_OTP  = "${V1}auth/verify-otp"
    const val GOOGLE_AUTH = "${V1}auth/google"
    const val REFRESH     = "${V1}auth/refresh-token"
    const val LOGOUT      = "${V1}auth/logout"
    const val GUEST_LOGIN    = "${V1}auth/guest"
    const val FIREBASE_PHONE = "${V1}auth/firebase-phone"

    // ── User / Profile ────────────────────────────────────────────────────────
    const val PROFILE           = "${V1}users/me"
    const val DELETE_ACCOUNT    = "${V1}users/me"
    const val ADDRESSES         = "${V1}users/me/addresses"
    const val ADDRESS_BY_ID     = "${V1}users/me/addresses/{id}"
    const val ADDRESS_DEFAULT   = "${V1}users/me/addresses/{id}/default"
    const val FCM_TOKEN         = "${V1}users/me/fcm-token"

    // ── Menu ──────────────────────────────────────────────────────────────────
    const val CATEGORIES       = "${V1}categories"
    const val MENU_ITEMS       = "${V1}products"
    const val BEST_SELLERS     = "${V1}products/best-sellers"
    const val RECOMMENDED      = "${V1}products/recommended"
    const val MENU_ITEM_BY_ID  = "${V1}products/{id}"

    // ── Cart ──────────────────────────────────────────────────────────────────
    const val CART             = "${V1}cart"
    const val CART_ITEMS       = "${V1}cart/items"
    const val CART_ITEM_BY_ID  = "${V1}cart/items/{id}"
    const val CART_COUPON      = "${V1}cart/coupon"

    // ── Orders ────────────────────────────────────────────────────────────────
    const val ORDERS           = "${V1}orders"
    const val MY_ORDERS        = "${V1}orders/me"
    const val ORDER_BY_ID      = "${V1}orders/{id}"
    const val CANCEL_ORDER     = "${V1}orders/{id}/cancel"
    const val RATE_ORDER       = "${V1}orders/{id}/rate"

    // ── Payments ──────────────────────────────────────────────────────────────
    const val PAYMENT_CREATE   = "${V1}payments/create-order"
    const val PAYMENT_VERIFY   = "${V1}payments/verify"

    // ── Reservations ──────────────────────────────────────────────────────────
    const val RESERVATIONS           = "${V1}reservations"
    const val RESERVATION_BY_ID      = "${V1}reservations/{id}"
    const val RESERVATION_AVAILABILITY = "${V1}reservations/availability"

    // ── Banners & Offers ──────────────────────────────────────────────────────
    const val BANNERS          = "${V1}banners"
    const val OFFERS           = "${V1}offers"

    // ── Loyalty ───────────────────────────────────────────────────────────────
    const val LOYALTY          = "${V1}loyalty"
    const val LOYALTY_HISTORY  = "${V1}loyalty/history"

    // ── Branches ──────────────────────────────────────────────────────────────
    const val BRANCHES         = "${V1}branches"
    const val BRANCH_BY_ID     = "${V1}branches/{id}"
}
