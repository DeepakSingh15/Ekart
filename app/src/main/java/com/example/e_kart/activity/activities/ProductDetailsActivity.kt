package com.example.e_kart.activity.activities


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.e_kart.R
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.CartItem
import com.example.e_kart.models.Product
import com.example.e_kart.utils.Constants
import com.example.e_kart.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

    private var mProductId: String = ""
    private lateinit var mProductDetails: Product
    private var mProductOwnerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        setUpActionBar()
        btn_add_to_cart.setOnClickListener(this)
        btn_go_to_cart.setOnClickListener(this)

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
            mProductId = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
        }

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)){
            mProductOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }

        if (FirestoreClass().getCurrentUserID() == mProductOwnerId){
            btn_add_to_cart.visibility = View.GONE
            btn_go_to_cart.visibility = View.GONE
        }
        else{
            btn_add_to_cart.visibility = View.VISIBLE
            btn_go_to_cart.visibility = View.VISIBLE
        }

        getProductDetails()
    }

    fun setUpActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_product_details_activity)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar.setNavigationOnClickListener {
           onBackPressed()
        }
    }

    private fun getProductDetails() {

        showProgressDialog()

        FirestoreClass().getProductDetails(this@ProductDetailsActivity, mProductId)
    }

    fun productDetailsSuccess(product: Product) {
        mProductDetails = product

        GlideLoader(this@ProductDetailsActivity).loadProductPicture(product.image, iv_product_detail_image)

        tv_product_details_title.text = product.title
        tv_product_details_price.text = "Price: ₹${product.price}"
        tv_product_details_description.text = product.description
        tv_product_details_available_quantity.text = product.stock_quantity

        if (product.stock_quantity.toInt() == 0){
            hideProgressDialog()
            btn_add_to_cart.visibility = View.GONE
            tv_product_details_available_quantity.text = resources.getString(R.string.lbl_out_of_stock)
            tv_product_details_available_quantity.setTextColor(ContextCompat.getColor(this@ProductDetailsActivity,R.color.colorSnackBarError))
        }
        else {
            if (FirestoreClass().getCurrentUserID() == product.user_id){
                hideProgressDialog()
            }
            else{
                FirestoreClass().checkIfItemExitsInCart(this@ProductDetailsActivity,mProductId)
            }
        }
    }

    private fun addToCart(){
        val cartItem = CartItem(
                FirestoreClass().getCurrentUserID(),
                mProductOwnerId,
                mProductId,
                mProductDetails.title,
                mProductDetails.price,
                mProductDetails.image,
                Constants.DEFAULT_CART_QUANTITY
        )
        showProgressDialog()
        FirestoreClass().addCartItems(this@ProductDetailsActivity, cartItem)
    }

    fun addToCartSuccess(){
        hideProgressDialog()

        Toast.makeText(this@ProductDetailsActivity,resources.getString(R.string.success_message_item_add_to_cart),Toast.LENGTH_SHORT).show()

        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }

    fun productExistsInCart(){
        hideProgressDialog()

        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE

    }


    override fun onClick(view: View?) {
        if (view !=null){
            when (view.id){
                R.id.btn_add_to_cart ->{
                    addToCart()
                }
                R.id.btn_go_to_cart ->{
                    startActivity(Intent(this@ProductDetailsActivity, CartListActivity::class.java))
                }
            }

            }
        }

}