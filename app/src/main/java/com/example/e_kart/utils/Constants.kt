package com.example.e_kart.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    //Firebase Constants
    //This is used for the Collection name for USERS
    const val USERS: String = "users"
    const val PRODUCTS: String = "products"
    const val ADDRESSES: String = "Addresses"
    const val ORDERS: String = "orders"
    const val SOLD_PRODUCT = "sold_products"
    // Create a constant variables for sharedPreference and Username Key
    const val MY_PREFERENCE: String = "MyPrefs"
    const val LOGGED_IN_USERNAME: String = "logged_in_username"
    const val EXTRA_USER_DETAILS:String = "extra_user_details"
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val IMAGE_REQUEST_CODE = 2

    const val USER_ID: String = "user_id"
    const val MALE: String = "Male"
    const val FEMALE: String = "Female"
    const val FIRST_NAME: String = "firstName"
    const val LAST_NAME: String = "lastName"
    const val MOBILE: String = "mobile"
    const val GENDER: String = "gender"
    const val IMAGE: String = "image"

    const val PROFILE_COMPLETED: String = "profileCompleted"
    const val PROFILE_COMPLETED_CODE = 1
    const val USER_PROFILE_IMAGE:String = "User_Profile_Image"
    const val PRODUCT_IMAGE:String = "Product_Image"
    const val EXTRA_PRODUCT_ID:String = "extra_product_id"
    const val EXTRA_PRODUCT_OWNER_ID: String = "extra_product_owner_id"
    const val DEFAULT_CART_QUANTITY: String = "1"
    const val CART_ITEMS: String = "cart_items"
    const val PRODUCT_ID: String = "product_id"

    const val CART_QUANTITY: String = "cart_quantity"
    const val STOCK_QUANTITY: String = "stock_quantity"

    const val HOME: String = "Home"
    const val OFFICE: String = "Office"
    const val OTHER: String = "Other"

    const val EXTRA_ADDRESS_DETAILS: String = "AddressDetails"
    const val EXTRA_SELECT_ADDRESS: String = "extra_select_address"
    const val ADD_ADDRESS_REQUEST_CODE: Int = 1

    const val EXTRA_SELECTED_ADDRESS: String = "extra_selected_address"

    const val EXTRA_MY_ORDER_DETAILS: String = "extra_MY_ORDERS_DETAILS"
    const val EXTRA_SOLD_PRODUCT_DETAILS: String = "extra_sold_product_details"


    fun pickImageFromGallery(activity : Activity) {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity.startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}