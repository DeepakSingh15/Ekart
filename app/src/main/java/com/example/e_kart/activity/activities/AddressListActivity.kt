package com.example.e_kart.activity.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.e_kart.R
import com.example.e_kart.activity.adapters.AddressListAdapter
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.Address
import com.example.e_kart.utils.Constants
import com.example.e_kart.utils.SwipeToDeleteCallback
import com.example.e_kart.utils.SwipeToEditCallback
import com.google.api.ResourceProto.resource
import kotlinx.android.synthetic.main.activity_address_list.*

class AddressListActivity : BaseActivity() {

    private var mSelectedAddress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_list)
        setupActionBar()

        tv_add_address.setOnClickListener {
            startActivityForResult(Intent(this@AddressListActivity,AddEditAddressActivity::class.java),Constants.ADD_ADDRESS_REQUEST_CODE)
        }
        getAddressList()
        if (intent.hasExtra(Constants.EXTRA_SELECT_ADDRESS)){
            mSelectedAddress = intent.getBooleanExtra(Constants.EXTRA_SELECT_ADDRESS,false)
        }

        if (mSelectedAddress){
            tv_title.text = resources.getString(R.string.title_select_address)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            getAddressList()
        }
    }
    private fun setupActionBar() {

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_address_list_activity)
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

    private fun getAddressList(){
        showProgressDialog()
        FirestoreClass().getAddressesList(this@AddressListActivity)
    }

    fun successAddressListFromFirestore(addressList: ArrayList<Address>) {

        hideProgressDialog()

        if (addressList.size > 0) {

            rv_address_list.visibility = View.VISIBLE
            tv_no_address_found.visibility = View.GONE

            rv_address_list.layoutManager = LinearLayoutManager(this@AddressListActivity)
            rv_address_list.setHasFixedSize(true)

            val addressAdapter = AddressListAdapter(this@AddressListActivity, addressList,mSelectedAddress)
            rv_address_list.adapter = addressAdapter

            if (!mSelectedAddress){
                val editSwipeHandler = object: SwipeToEditCallback(this){
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val adapter = rv_address_list.adapter as AddressListAdapter
                        adapter.notifyEditItem(this@AddressListActivity, viewHolder.adapterPosition)
                    }
                }

                val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(rv_address_list)

                val deleteSwipeHandler = object: SwipeToDeleteCallback(this){
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        showProgressDialog()
                        FirestoreClass().deleteAddress(this@AddressListActivity, addressList[viewHolder.adapterPosition].id)
                    }
                }
                val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(rv_address_list)
            }

        } else {
            rv_address_list.visibility = View.GONE
            tv_no_address_found.visibility = View.VISIBLE
        }

    }

    fun deleteAddressSuccess(){
        hideProgressDialog()

        Toast.makeText(this@AddressListActivity,resources.getString(R.string.err_address_deleted_successfully),Toast.LENGTH_SHORT).show()

        getAddressList()
    }

}