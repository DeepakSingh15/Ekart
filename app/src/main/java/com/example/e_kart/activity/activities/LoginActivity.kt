package com.example.e_kart.activity.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.e_kart.R
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.User
import com.example.e_kart.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tv_forgot_password = findViewById<TextView>(R.id.tv_forgot_password)
        val btn_login = findViewById<Button>(R.id.btn_login)
        val tv_register = findViewById<TextView>(R.id.tv_register)
        // Click event assigned to Forgot Password text.
        tv_forgot_password.setOnClickListener(this)
        // Click event assigned to Login button.
        btn_login.setOnClickListener(this)
        // Click event assigned to Register text.
        tv_register.setOnClickListener(this)

    }

    override fun onClick(view : View?){
        if (view != null){
            when (view.id){
                R.id.tv_forgot_password ->{
                    val intentReset = Intent(this@LoginActivity,
                        ForgotPwdActivity::class.java)
                    startActivity(intentReset)
                    finish()
                }
                R.id.btn_login ->{
                    loginUser()
                }
                R.id.tv_register ->{
                    // Launch the register screen when the user clicks on the text.
                    val intentRegister = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intentRegister)
                    finish()
                }
            }
        }
    }
    // A function to validate the login entries of a user.

    private fun validateLoginDetails(): Boolean{

        val et_email = findViewById<EditText>(R.id.et_signIn_email)
        val et_password = findViewById<EditText>(R.id.et_signIn_password)
        return when{
            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) ->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' })->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else ->{
                true
            }
        }
    }

    private fun loginUser(){
        val et_email = findViewById<EditText>(R.id.et_signIn_email)
        val et_password = findViewById<EditText>(R.id.et_signIn_password)
        val email = et_email.text.toString()
        val password = et_password.text.toString()
        if (validateLoginDetails()){
            showProgressDialog()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        hideProgressDialog()
                        if (task.isSuccessful){
                            FirestoreClass().getUserDetails(this@LoginActivity)
                        }
                        else{
                            hideProgressDialog()
                            showErrorSnackBar(task.exception!!.message.toString(),true)
                        }
                    }
            }
        }

    // A function to notify user that logged in success and get the user details from the FireStore database after authentication.
    fun userLoggedInSuccess(user: User) {
        // Hide the progress dialog.
        hideProgressDialog()

        // Redirect the user to Main Screen after log in.
        if (user.profileCompleted == 0) {
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS,user)
            startActivity(intent)
            finish()
        } else {
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
            finish()
        }
    }

}