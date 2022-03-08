package com.example.e_kart.activity.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_kart.R
import com.example.e_kart.activity.adapters.CartItemsListAdapter
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.CartItem
import com.example.e_kart.models.Product
import com.example.e_kart.utils.Constants
import kotlinx.android.synthetic.main.activity_cart_list.*

class CartListActivity : BaseActivity() {

    private lateinit var mProductsList: ArrayList<Product>

    private lateinit var mCartListItems: ArrayList<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_list)

        setUpActionBar()

        btn_checkout.setOnClickListener{
            val intent = Intent(this@CartListActivity,AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECT_ADDRESS,true)
            startActivity(intent)
        }
    }

    private fun setUpActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_cart_list_activity)

        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun successCartItemsList(cartList: ArrayList<CartItem>) {
        hideProgressDialog()
        for (product in mProductsList) {
            for (cartItem in cartList) {
                if (product.product_id == cartItem.product_id) {

                    cartItem.stock_quantity = product.stock_quantity

                    if (product.stock_quantity.toInt() == 0) {
                        cartItem.cart_quantity = product.stock_quantity
                    }
                }
            }
        }
        mCartListItems = cartList

        if (mCartListItems.size > 0) {

            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.VISIBLE
            tv_no_cart_item_found.visibility = View.GONE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this@CartListActivity)
            rv_cart_items_list.setHasFixedSize(true)

            val cartListAdapter = CartItemsListAdapter(this@CartListActivity, mCartListItems,true)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal: Double = 0.0

                     for (item in mCartListItems) {

                         val price = item.price.toDouble()
                         val quantity = item.cart_quantity.toInt()

                         subTotal += (price * quantity)
                     }

                    tv_sub_total.text = "₹$subTotal"
                     // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
                     tv_shipping_charge.text = "₹10.0"

                     if (subTotal > 0) {
                         ll_checkout.visibility = View.VISIBLE

                         val total = subTotal + 10
                         tv_total_amount.text = "₹$total"
                     } else {
                         ll_checkout.visibility = View.GONE
                     }


        } else {
            rv_cart_items_list.visibility = View.GONE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.VISIBLE
        }

    }

    private fun getCartItemsList() {

        FirestoreClass().getCartList(this@CartListActivity)
    }

    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
        mProductsList = productsList
        getCartItemsList()
    }

    private fun getProductList() {

        showProgressDialog()

        FirestoreClass().getAllProductsList(this@CartListActivity)
    }

    fun itemRemovedSuccess(){
        hideProgressDialog()

        Toast.makeText(this@CartListActivity, resources.getString(R.string.msg_item_removed_successfully), Toast.LENGTH_SHORT).show()

        getCartItemsList()
    }

    fun itemUpdateSuccess(){
        hideProgressDialog()
        getCartItemsList()
    }

   override fun onResume() {
        super.onResume()
        getProductList()
   }
    

}