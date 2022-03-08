package com.example.e_kart.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.e_kart.activity.activities.*
import com.example.e_kart.activity.ui.fragments.DashboardFragment
import com.example.e_kart.activity.ui.fragments.OrdersFragment
import com.example.e_kart.activity.ui.fragments.ProductsFragment
import com.example.e_kart.activity.ui.fragments.SoldProductsFragment
import com.example.e_kart.models.*
import com.example.e_kart.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

    // Access a Cloud Firestore instance
    private val mFireStore = FirebaseFirestore.getInstance()

     // A function to make an entry of the registered user in the FireStore database.
    fun registerUser(activity: RegisterActivity, userInfo: User) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while registering the user.", e)
            }
    }

     // A function to get the user id of current logged user.
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

     // A function to get the logged user details from from FireStore Database.
    fun getUserDetails(activity: Activity) {
        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
                // The document id to get the Fields of user.
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    Log.i(activity.javaClass.simpleName, document.toString())

                    // Here we have received the document snapshot which is converted into the User Data model object.
                    val user = document.toObject(User::class.java)!!

                    val sharedPreferences = activity.getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE)

                    // Create an instance of the editor which is help us to edit the SharedPreference.
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString(Constants.LOGGED_IN_USERNAME, "${user.firstName} ${user.lastName}")
                    editor.apply()

                    when (activity) {
                        is LoginActivity -> {
                            // Call a function of base activity for transferring the result to it.
                            activity.userLoggedInSuccess(user)
                        }
                        is SettingActivity ->{

                            // Call a function of base activity for transferring the result to it.
                            activity.userDetailsSuccess(user)
                            // END
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Hide the progress dialog if there is any error. And print the error in log.
                    when (activity) {
                        is LoginActivity -> {
                            activity.hideProgressDialog()
                        }
                        is SettingActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(activity.javaClass.simpleName, "Error while getting user details.", e)
                }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>){
        // Collection Name
        mFireStore.collection(Constants.USERS)
                // Document ID against which the data to be updated. Here the document id is the current logged in user id.
                .document(getCurrentUserID())
                // A HashMap of fields which are to be updated.
                .update(userHashMap)
                .addOnSuccessListener {

                    when (activity) {
                        is UserProfileActivity -> {
                            activity.userProfileUpdateSuccess()
                        }
                    }

                }
                .addOnFailureListener { e ->

                    when (activity) {
                        is UserProfileActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(activity.javaClass.simpleName, "Error while updating the user details.", e)
                }
    }

    // A function to upload the image to the cloud storage.
    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String) {

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                imageType + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(activity, imageFileURI))

        //adding the file to reference
        sRef.putFile(imageFileURI!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri ->
                                Log.e("Downloadable Image URL", uri.toString())

                                // Here call a function of base activity for transferring the result to it.
                                when (activity) {
                                    is UserProfileActivity -> {
                                        activity.imageUploadSuccess(uri.toString())
                                    }
                                    is AddProductActivity -> {
                                        activity.imageUploadSuccess(uri.toString())
                                    }
                                }
                            }
                }
                .addOnFailureListener { exception ->
                    // Hide the progress dialog if there is any error. And print the error in log.
                    when (activity) {
                        is UserProfileActivity -> {
                            activity.hideProgressDialog()
                        }
                        is AddProductActivity -> {
                            activity.hideProgressDialog()
                        }
                    }

                    Log.e(activity.javaClass.simpleName, exception.message, exception)
                }
          }

    fun uploadProductDetails(activity: AddProductActivity, productInfo: Product){

        // The "products" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.PRODUCTS)

                .document()
                .set(productInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.productUploadSuccess()
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while uploading the product details.", e)
                }
    }

    fun getProductsList(fragment: Fragment){

        mFireStore.collection(Constants.PRODUCTS)
                .whereEqualTo(Constants.USER_ID, getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    Log.e("Products List", document.documents.toString())
                    val productsList: ArrayList<Product> = ArrayList()
                    for (i in document.documents) {
                        val product = i.toObject(Product::class.java)
                        product!!.product_id = i.id

                        productsList.add(product)
                    }

                    when (fragment) {
                        is ProductsFragment -> {
                            fragment.successProductsListFromFireStore(productsList)
                        }
                    }
                }
         }

    fun getDashboardItemsList(fragment: DashboardFragment){
        mFireStore.collection(Constants.PRODUCTS)
                .get()
                .addOnSuccessListener { document ->
                    Log.e(fragment.javaClass.simpleName, document.documents.toString())

                    val productsList: ArrayList<Product> = ArrayList()

                    for (i in document.documents) {
                        val product = i.toObject(Product::class.java)
                        product!!.product_id = i.id

                        productsList.add(product)
                    }

                    fragment.successDashboardItemsList(productsList)

                }
                .addOnFailureListener { e ->
                    fragment.hideProgressDialog()
                    Log.e(fragment.javaClass.simpleName,"Error while getting dashboard items list")
                }
        }

    fun deleteProduct(fragment: ProductsFragment, productID: String){
        mFireStore.collection(Constants.PRODUCTS)
                .document(productID)
                .delete()
                .addOnSuccessListener {
                    fragment.productDeleteSuccess()
                }
                .addOnFailureListener {e ->
                    fragment.hideProgressDialog()
                    Log.e(fragment.requireActivity().javaClass.simpleName,"Error while deleting the product",e)
                }
         }

    fun getProductDetails(activity: ProductDetailsActivity, productId: String) {

        mFireStore.collection(Constants.PRODUCTS)
                .document(productId)
                .get()
                .addOnSuccessListener { document ->

                    Log.e(activity.javaClass.simpleName, document.toString())

                    val product = document.toObject(Product::class.java)!!

                    activity.productDetailsSuccess(product)

                }
                .addOnFailureListener { e ->

                    activity.hideProgressDialog()

                    Log.e(activity.javaClass.simpleName, "Error while getting the product details.", e)
                }
        }

    fun checkIfItemExitsInCart(activity: ProductDetailsActivity, productId: String){
        mFireStore.collection(Constants.CART_ITEMS)
                .whereEqualTo(Constants.USER_ID,getCurrentUserID())
                .whereEqualTo(Constants.PRODUCT_ID,productId)
                .get()
                .addOnSuccessListener { document ->
                    Log.e(activity.javaClass.simpleName, document.documents.toString())
                    if (document.documents.size > 0){
                        activity.productExistsInCart()
                    }
                    else{
                        activity.hideProgressDialog()
                    }
                }
                .addOnFailureListener { e ->

                    activity.hideProgressDialog()

                    Log.e(activity.javaClass.simpleName,"Error while checking the existing cart list.",e)
                }
    }

    fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem){
        mFireStore.collection(Constants.CART_ITEMS)
                .document()
                .set(addToCart, SetOptions.merge())
                .addOnSuccessListener {
                    activity.addToCartSuccess()
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while creating the document for cart item.",e)
                }
         }

        fun getCartList(activity: Activity) {

            mFireStore.collection(Constants.CART_ITEMS)
                    .whereEqualTo(Constants.USER_ID, getCurrentUserID())
                    .get() // Will get the documents snapshots.
                    .addOnSuccessListener { document ->

                        Log.e(activity.javaClass.simpleName, document.documents.toString())

                        val list: ArrayList<CartItem> = ArrayList()

                        // A for loop as per the list of documents to convert them into Cart Items ArrayList.
                        for (i in document.documents) {

                            val cartItem = i.toObject(CartItem::class.java)!!
                            cartItem.id = i.id

                            list.add(cartItem)
                        }

                        when (activity) {
                            is CartListActivity -> {
                                activity.successCartItemsList(list)
                            }
                            is CheckoutActivity -> {
                                activity.successCartItemsList(list)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Hide the progress dialog if there is an error based on the activity instance.
                        when (activity) {
                            is CartListActivity -> {
                                activity.hideProgressDialog()
                            }
                            is CheckoutActivity -> {
                                activity.hideProgressDialog()
                            }
                        }

                        Log.e(activity.javaClass.simpleName, "Error while getting the cart list items.", e)
                    }
        }

    fun getAllProductsList(activity: Activity){
        mFireStore.collection(Constants.PRODUCTS)
                .get()
                .addOnSuccessListener {document ->

                    Log.e("Products List", document.documents.toString())

                    val productsList: ArrayList<Product> = ArrayList()

                    for (i in document.documents) {

                        val product = i.toObject(Product::class.java)
                        product!!.product_id = i.id

                        productsList.add(product)
                    }
                    when (activity) {
                        is CartListActivity -> {
                            activity.hideProgressDialog()
                            activity.successProductsListFromFireStore(productsList)
                        }
                        is CheckoutActivity -> {
                            activity.successProductsListFromFireStore(productsList)
                        }
                    }
                }
                .addOnFailureListener {e ->
                    when (activity) {
                        is CartListActivity -> {
                            activity.hideProgressDialog()
                        }
                        is CheckoutActivity -> {
                            activity.hideProgressDialog()
                        }
                    }

                    Log.e("Get Product List", "Error while getting all product list.", e)
                }

        }

    fun removeItemFromCart(context: Context, cart_id: String) {
        mFireStore.collection(Constants.CART_ITEMS)
                .document(cart_id)
                .delete()
                .addOnSuccessListener {
                    when (context) {
                        is CartListActivity -> {
                            context.itemRemovedSuccess()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    when (context) {
                        is CartListActivity -> {
                            context.hideProgressDialog()
                        }
                    }
                    Log.e(context.javaClass.simpleName, "Error while removing the item from the cart list.", e)
                }
    }

    fun updateMyCart(context: Context, card_id: String, itemHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.CART_ITEMS)
                .document(card_id)
                .update(itemHashMap)
                .addOnSuccessListener {
                    when (context) {
                        is CartListActivity -> {
                            context.itemUpdateSuccess()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    when (context) {
                        is CartListActivity -> {
                            context.hideProgressDialog()
                        }
                    }

                    Log.e(context.javaClass.simpleName, "Error while updating the cart item.", e)
                }
        }

    fun addAddress(activity: AddEditAddressActivity, addressInfo: Address){

        mFireStore.collection(Constants.ADDRESSES)
                .document()
                // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
                .set(addressInfo, SetOptions.merge())
                .addOnSuccessListener {

                    activity.addUpdateAddressSuccess()
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while adding the address.", e)
                }
        }

    fun getAddressesList(activity: AddressListActivity) {

        mFireStore.collection(Constants.ADDRESSES)
                .whereEqualTo(Constants.USER_ID, getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->

                    Log.e(activity.javaClass.simpleName, document.documents.toString())
                    // Here we have created a new instance for address ArrayList.
                    val addressList: ArrayList<Address> = ArrayList()

                    // A for loop as per the list of documents to convert them into Boards ArrayList.
                    for (i in document.documents) {

                        val address = i.toObject(Address::class.java)!!
                        address.id = i.id

                        addressList.add(address)
                    }

                    activity.successAddressListFromFirestore(addressList)
                }
                .addOnFailureListener { e ->
                    // Here call a function of base activity for transferring the result to it.

                    activity.hideProgressDialog()

                    Log.e(activity.javaClass.simpleName, "Error while getting the address list.", e)
                }
    }

    fun updateAddress(activity: AddEditAddressActivity, addressInfo: Address, addressId: String){

        mFireStore.collection(Constants.ADDRESSES)
                .document(addressId)
                .set(addressInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.addUpdateAddressSuccess()
                }
                .addOnFailureListener {e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while updating the address",e)
                }
    }

    fun deleteAddress(activity: AddressListActivity, addressId: String) {

        mFireStore.collection(Constants.ADDRESSES)
                .document(addressId)
                .delete()
                .addOnSuccessListener {

                    // Here call a function of base activity for transferring the result to it.
                    activity.deleteAddressSuccess()
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while deleting the address.", e)
                }
    }

    fun placeOrder(activity: CheckoutActivity, order: Order) {

        mFireStore.collection(Constants.ORDERS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(order, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.orderPlacedSuccess()

            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is any error.
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while placing an order.", e)
            }
    }

    fun updateAllDetails(activity: CheckoutActivity, cartList: ArrayList<CartItem>,order: Order){
        val writeBatch = mFireStore.batch()

        for(cart in cartList){
            val soldProduct = SoldProduct(
                cart.product_owner_id,
                cart.title,
                cart.price,
                cart.cart_quantity,
                cart.image,
                order.title,
                order.order_datetime,
                order.sub_total_amount,
                order.shipping_charge,
                order.total_amount,
                order.address
            )
            val documentReference = mFireStore.collection(Constants.SOLD_PRODUCT)
                .document()

            writeBatch.set(documentReference, soldProduct)

        }
        for (cart in cartList) {

            val productHashMap = HashMap<String, Any>()

            productHashMap[Constants.STOCK_QUANTITY] =
                (cart.stock_quantity.toInt() - cart.cart_quantity.toInt()).toString()

            val documentReference = mFireStore.collection(Constants.PRODUCTS)
                .document(cart.product_id)

            writeBatch.update(documentReference, productHashMap)
        }

        for (cart in cartList) {

            val documentReference = mFireStore.collection(Constants.CART_ITEMS)
                .document(cart.id)
            writeBatch.delete(documentReference)
        }

        writeBatch.commit().addOnSuccessListener {

            activity.allDetailsUpdatedSuccessfully()

        }.addOnFailureListener { e ->
            // Here call a function of base activity for transferring the result to it.
            activity.hideProgressDialog()

            Log.e(activity.javaClass.simpleName, "Error while updating all the details after order placed.", e)
        }
    }



    fun getMyOrderList(fragment: OrdersFragment){
        mFireStore.collection(Constants.ORDERS)
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            .get()
            .addOnSuccessListener{ document ->
                Log.e(fragment.javaClass.simpleName,document.documents.toString())
                val list: ArrayList<Order> = ArrayList()

                for (i in document.documents) {
                    val orderItem = i.toObject(Order::class.java)!!
                    orderItem.id = i.id

                    list.add(orderItem)
                }
                fragment.populateOrdersListInUI(list)
            }
            .addOnFailureListener { e->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "Error while getting the orders list.", e)
            }
    }

    fun getSoldProductsList(fragment: SoldProductsFragment){
        mFireStore.collection(Constants.SOLD_PRODUCT)
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(fragment.javaClass.simpleName, document.documents.toString())
                val list: ArrayList<SoldProduct> = ArrayList()
                for (i in document.documents) {

                    val soldProduct = i.toObject(SoldProduct::class.java)!!
                    soldProduct.id = i.id

                    list.add(soldProduct)
                }
                fragment.successSoldProductsList(list)
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error.
                fragment.hideProgressDialog()

                Log.e(fragment.javaClass.simpleName, "Error while getting the list of sold products.", e)
            }
    }

}
