package com.kiladarbar.data.remote

object ApiEndpoints {

    // Auth
    const val SEND_OTP    = "auth/otp/send"
    const val VERIFY_OTP  = "auth/otp/verify"
    const val GOOGLE_AUTH = "auth/google"
    const val REFRESH     = "auth/refresh"
    const val LOGOUT      = "auth/logout"
    const val GUEST_LOGIN = "auth/guest"

    // Menu
    const val CATEGORIES      = "menu/categories"
    const val MENU_ITEMS      = "menu/items"
    const val BEST_SELLERS    = "menu/items/best-sellers"
    const val RECOMMENDED     = "menu/items/recommended"
    const val MENU_ITEM_BY_ID = "menu/items/{id}"

    // Orders
    const val ORDERS          = "orders"
    const val ORDER_BY_ID     = "orders/{id}"
    const val CANCEL_ORDER    = "orders/{id}/cancel"
    const val RATE_ORDER      = "orders/{id}/rate"

    // Profile
    const val PROFILE         = "profile"
    const val ADDRESSES       = "profile/addresses"
    const val ADDRESS_BY_ID   = "profile/addresses/{id}"

    // Loyalty
    const val LOYALTY         = "loyalty"
    const val LOYALTY_HISTORY = "loyalty/history"

    // Coupons
    const val VALIDATE_COUPON = "coupons/validate"

    // Payments
    const val INITIATE_PAYMENT = "payments/initiate"
    const val VERIFY_PAYMENT   = "payments/verify"

    // Branches
    const val BRANCHES        = "branches"
    const val BRANCH_BY_ID    = "branches/{id}"
}
