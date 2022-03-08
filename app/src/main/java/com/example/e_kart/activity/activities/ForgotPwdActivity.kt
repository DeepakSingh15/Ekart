package com.example.e_kart.activity.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.example.e_kart.R
import com.example.e_kart.utils.MSPButton
import com.example.e_kart.utils.MSPEditText
import com.google.firebase.auth.FirebaseAuth

class ForgotPwdActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pwd)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val submit = findViewById<MSPButton>(R.id.btn_submit)

        setUpActionBar()

        submit.setOnClickListener{
            resetPassowrd()
        }

    }

    private fun setUpActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_forgot_password_activity)

        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar.setNavigationOnClickListener{
            backToLoginScreen()
        }
    }


    private fun resetPassowrd(){
        val et_reset_email = findViewById<MSPEditText>(R.id.et_reset_email)
        val email = et_reset_email.text.toString().trim { it <= ' ' }

        if (email.isEmpty()) {
            showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
        }
        else{
            showProgressDialog()

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->

                        hideProgressDialog()
                        if (task.isSuccessful){
                            showErrorSnackBar(resources.getString(R.string.email_sent_success), false)
                            backToLoginScreen()
                        }
                        else{
                            showErrorSnackBar(task.exception!!.message.toString(),true)
                        }
                    }
        }
    }

    private fun backToLoginScreen(){
        val intent = Intent(this@ForgotPwdActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}