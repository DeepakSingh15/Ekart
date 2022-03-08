package com.example.e_kart.activity.activities

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.example.e_kart.R
import kotlinx.android.synthetic.main.activity_add_product.*
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.Product
import com.example.e_kart.utils.Constants
import com.example.e_kart.utils.GlideLoader

class AddProductActivity : BaseActivity(), View.OnClickListener {

    private var mSelectedProductImageUri: Uri? = null
    private var mProductImageURL: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        iv_add_icon.setOnClickListener(this@AddProductActivity)
        btn_submit_product.setOnClickListener(this@AddProductActivity)

        setUpActionBar()
    }

    private fun setUpActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_add_product_activity)
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
        if (view != null){
            when (view.id) {
                (R.id.iv_add_icon) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                            //permission denied
                            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                            requestPermissions(permissions, Constants.READ_STORAGE_PERMISSION_CODE)
                        }
                        else{
                            Constants.pickImageFromGallery(this@AddProductActivity)
                        }

                    }
                    else{
                        Constants.pickImageFromGallery(this@AddProductActivity)
                    }
                }
                (R.id.btn_submit_product) -> {
                    if (validateProductDetails()){

                        uploadProductImage()
                    }

                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.pickImageFromGallery(this@AddProductActivity)
            }
            else{
                Toast.makeText(this,resources.getString(R.string.read_storage_permission_denied),Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.IMAGE_REQUEST_CODE){
            mSelectedProductImageUri = data?.data!!

            GlideLoader(this@AddProductActivity).loadProductPicture(mSelectedProductImageUri!!,iv_product_image)

        }
    }

    private fun validateProductDetails(): Boolean{
        return when {
            (mSelectedProductImageUri == null) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image),true)
                false
            }
            TextUtils.isEmpty(et_product_title.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title),true)
                false
            }
            TextUtils.isEmpty(et_product_price.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price),true)
                false
            }
            TextUtils.isEmpty(et_product_description.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_description),true)
                false
            }
            TextUtils.isEmpty(et_product_quantity.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_quantity),true)
                false
            }
            else ->{
                true
            }

        }
    }

    /**
     * A function to notify the success result of image upload to the Cloud Storage.
     *
     * @param imageURL After successful upload the Firebase Cloud returns the URL.
     */
    fun imageUploadSuccess(imageURL: String) {

        hideProgressDialog()

        mProductImageURL = imageURL

        uploadProductDetails()

    }

    private fun uploadProductImage(){
        showProgressDialog()
        FirestoreClass().uploadImageToCloudStorage(this@AddProductActivity,mSelectedProductImageUri,Constants.PRODUCT_IMAGE)
    }

    private fun uploadProductDetails(){
        val userName = this.getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE).getString(Constants.LOGGED_IN_USERNAME,"")!!
        val product = Product(
                FirestoreClass().getCurrentUserID(),
                userName,
                et_product_title.text.toString().trim { it <= ' ' },
                et_product_price.text.toString().trim { it <= ' '},
                et_product_description.text.toString().trim { it <= ' '},
                et_product_quantity.text.toString().trim { it <= ' ' },
                mProductImageURL
        )

        FirestoreClass().uploadProductDetails(this@AddProductActivity, product)
    }

    fun productUploadSuccess(){
        hideProgressDialog()

        Toast.makeText(this@AddProductActivity,resources.getString(R.string.product_uploaded_success_image),
                       Toast.LENGTH_SHORT).show()
        finish()
    }
}