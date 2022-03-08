package com.example.e_kart.activity.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.e_kart.R
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.User
import com.example.e_kart.utils.Constants
import com.example.e_kart.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var userDetails: User
    private var mSelectedImageFileUri: Uri? = null
    private var mUserProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        iv_user_photo.setOnClickListener(this@UserProfileActivity)
        btn_submit.setOnClickListener(this@UserProfileActivity)

        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            // Get the user details from intent as a ParcelableExtra.
            userDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }

        if (userDetails.profileCompleted == 0){
            tv_title.text = resources.getString(R.string.title_complete_profile)
            et_first_name.isEnabled = false
            et_last_name.isEnabled = false
            et_email.isEnabled = false
            et_first_name.setText(userDetails.firstName)
            et_last_name.setText(userDetails.lastName)
            et_email.setText(userDetails.email)

        }
        else{
            setUpActionBar()

            tv_title.text = resources.getString(R.string.title_edit_profile)
            GlideLoader(this@UserProfileActivity).loadUserPicture(userDetails.image,iv_user_photo)
            et_first_name.isEnabled = true
            et_last_name.isEnabled = true
            et_email.isEnabled = false
            et_first_name.setText(userDetails.firstName)
            et_last_name.setText(userDetails.lastName)
            et_email.setText(userDetails.email)
            if (userDetails.mobile != 0L){

                et_mobile_number.setText(userDetails.mobile.toString())

            }
            if (userDetails.gender == Constants.MALE){
                rb_male.isChecked = true
            }
            else{
                rb_female.isChecked = true
            }
           btn_submit.text = resources.getString(R.string.lvl_update_profile)

        }
    }

    private fun setUpActionBar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_user_profile_activity)
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

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.iv_user_photo -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            //permission denied
                            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                            //show popup to request runtime permission
                            requestPermissions(permissions, Constants.READ_STORAGE_PERMISSION_CODE)
                        } else {

                            Constants.pickImageFromGallery(this@UserProfileActivity)
                        }
                    } else {
                        //system OS is < Marshmallow
                        Constants.pickImageFromGallery(this@UserProfileActivity)
                    }
                }
                R.id.btn_submit -> {

                    if (validateUserProfileDetails()) {
                        // Show the progress dialog.
                        showProgressDialog()

                        if (mSelectedImageFileUri != null) {
                            FirestoreClass().uploadImageToCloudStorage(this@UserProfileActivity, mSelectedImageFileUri,Constants.USER_PROFILE_IMAGE)
                        } else {
                            updateProfileUserDetails()
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.pickImageFromGallery(this@UserProfileActivity)
            } else {
                Toast.makeText(this, resources.getString(R.string.read_storage_permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageView = findViewById<ImageView>(R.id.iv_user_photo)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.IMAGE_REQUEST_CODE) {
            mSelectedImageFileUri = data?.data!!

            // imageView.setImageURI(Uri.parse(selectedImageFileUri.toString()))

            GlideLoader(this@UserProfileActivity).loadUserPicture(mSelectedImageFileUri!!, imageView)

        }
    }

    //A function to validate the input entries for profile details
    private fun validateUserProfileDetails(): Boolean {
        return when {
            // Check if the mobile number is not empty as it is mandatory to enter.
            TextUtils.isEmpty(et_mobile_number.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            !rb_male.isChecked && !rb_female.isChecked -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_gender_selection), true)
                false
            }

            else -> {
                true
            }
        }
    }

    private fun updateProfileUserDetails() {
        val userHashMap = HashMap<String, Any>()

        val firstName = et_first_name.text.toString().trim { it <= ' ' }
        if (firstName != userDetails.firstName) {
            userHashMap[Constants.FIRST_NAME] = firstName
        }

        // Get the LastName from editText and trim the space
        val lastName = et_last_name.text.toString().trim { it <= ' ' }
        if (lastName != userDetails.lastName) {
            userHashMap[Constants.LAST_NAME] = lastName
        }

        val mobileNumber = et_mobile_number.text.toString().trim { it <= ' ' }
        val gender = if (rb_male.isChecked) {
            Constants.MALE
        } else {
            Constants.FEMALE
        }
        if (mobileNumber.isNotEmpty()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }
        if (mUserProfileImageURL.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
        }

        userHashMap[Constants.GENDER] = gender
        userHashMap[Constants.PROFILE_COMPLETED] = Constants.PROFILE_COMPLETED_CODE

        FirestoreClass().updateUserProfileData(this@UserProfileActivity, userHashMap)
    }


    //  function to notify the success result and proceed further accordingly after updating the user details
    fun userProfileUpdateSuccess() {
        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(this@UserProfileActivity, resources.getString(R.string.msg_profile_update_success), Toast.LENGTH_SHORT).show()

        // Redirect to the Main Screen after profile completion.
        startActivity(Intent(this@UserProfileActivity, DashboardActivity::class.java))
        finish()
    }

    /**
     * A function to notify the success result of image upload to the Cloud Storage.
     *
     * @param imageURL After successful upload the Firebase Cloud returns the URL.
     */
    fun imageUploadSuccess(imageURL: String) {
        // Hide the progress dialog
        hideProgressDialog()

        mUserProfileImageURL = imageURL

        updateProfileUserDetails()
    }
}