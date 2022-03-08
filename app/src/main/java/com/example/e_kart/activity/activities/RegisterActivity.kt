package com.example.e_kart.activity.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import com.example.e_kart.R
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btn_register = findViewById(R.id.btn_register) as Button
        val tv_login = findViewById(R.id.tv_login) as TextView

        setupActionBar()

        btn_register.setOnClickListener {
           registerUser()
        }

        tv_login.setOnClickListener{
            backToLogincreen()
        }
    }

    // A function for actionBar Setup.
    private fun setupActionBar() {

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_register_activity)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar.setNavigationOnClickListener {
            backToLogincreen()
        }
    }

     // A function to move to login screeen
    private fun backToLogincreen(){
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // A function to validate the entries of a new user.
    private fun validateRegisterDetails(): Boolean {

        var et_first_name = findViewById<EditText>(R.id.et_first_name)
        var et_last_name = findViewById<EditText>(R.id.et_last_name)
        var et_email = findViewById<EditText>(R.id.et_email)
        var et_password = findViewById<EditText>(R.id.et_password)
        var et_confirm_password = findViewById<EditText>(R.id.et_confirm_password)
        var cb_terms_and_condition = findViewById<CheckBox>(R.id.cb_terms_and_condition)

        return when {
            TextUtils.isEmpty(et_first_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(et_last_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
                false
            }

            et_password.text.toString().trim { it <= ' ' } != et_confirm_password.text.toString()
                    .trim { it <= ' ' } -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), true)
                false
            }
            !cb_terms_and_condition.isChecked -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun registerUser(){

        var et_first_name = findViewById<EditText>(R.id.et_first_name)
        var et_last_name = findViewById<EditText>(R.id.et_last_name)
        var et_email = findViewById<EditText>(R.id.et_email)
        var et_password = findViewById<EditText>(R.id.et_password)

        if (validateRegisterDetails()){

            showProgressDialog()

            val firstName = et_first_name.text.toString().trim { it <= ' ' }
            val lastName = et_last_name.text.toString().trim { it <= ' ' }
            val email = et_email.text.toString().trim() { it <= ' '}
            val password = et_password.text.toString().trim { it <= ' ' }
            // Create an instance and create a register a user with email and password.
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        hideProgressDialog()
                        if (task.isSuccessful){
                            val firebaseUser :FirebaseUser = task.result!!.user!!

                            // Here we have passed only four values in the constructor as there are only four values at registration. So, instead of giving it blank or default.
                            // We have already added the default values in the data model class itself. Make sure the passing value order is correct.
                            val user = User(firebaseUser.uid, firstName, lastName, email)

                            // Pass the required values in the constructor.
                            FirestoreClass().registerUser(this@RegisterActivity, user)
                        }
                        else{
                            // Hide the progress dialog
                            hideProgressDialog()
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    }
                )
        }
    }

    fun userRegistrationSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(this@RegisterActivity, resources.getString(R.string.register_success), Toast.LENGTH_SHORT).show()

        // Here the new user registered is automatically signed-in so we just sign-out the user from firebase
        // and send him to Intro Screen for Sign-In
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        finish()
    }
}