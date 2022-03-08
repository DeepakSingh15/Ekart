package com.example.e_kart.activity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.e_kart.R
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.User
import com.example.e_kart.utils.Constants
import com.example.e_kart.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity(), View.OnClickListener {

    // A variable for user details which will be initialized later on.
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setupActionBar()

        tv_edit.setOnClickListener(this)
        btn_logout.setOnClickListener(this)
        ll_address.setOnClickListener(this)
    }

    // A function for actionBar Setup.
    private fun setupActionBar() {

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_settings_activity)
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

    /**
     * A function to get the user details from firestore.
     */
    private fun getUserDetails() {

        showProgressDialog()
        // Call the function of Firestore class to get the user details from firestore which is already created.
        FirestoreClass().getUserDetails(this@SettingActivity)
    }
    /**
     * A function to receive the user details and populate it in the UI.
     */
    fun userDetailsSuccess(user: User) {

        mUserDetails = user
        hideProgressDialog()

        // Load the image using the Glide Loader class.
        GlideLoader(this@SettingActivity).loadUserPicture(user.image, iv_user_photo)

        tv_name.text = "${user.firstName} ${user.lastName}"
        tv_gender.text = user.gender
        tv_email.text = user.email
        tv_mobile_number.text = "${user.mobile}"

    }

    override fun onResume() {
        super.onResume()
        getUserDetails()
    }

    override fun onClick(view: View?) {
         if (view != null){
             when (view.id){
                 R.id.tv_edit ->{
                     val intent = Intent(this@SettingActivity, UserProfileActivity::class.java)
                     intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                     startActivity(intent)
                 }
                 R.id.btn_logout ->{
                     FirebaseAuth.getInstance().signOut()

                     val intent = Intent(this@SettingActivity, LoginActivity::class.java)
                     intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                     startActivity(intent)
                     finish()
                 }
                 R.id.ll_address ->{
                     val intent = Intent(this@SettingActivity,
                         AddressListActivity::class.java)
                     startActivity(intent)
                 }
             }
         }
    }
}