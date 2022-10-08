package com.example.zmci

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import com.example.zmci.database.DatabaseHelper
import com.example.zmci.helpers.InputValidation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class LoginFragment : AppCompatActivity(), View.OnClickListener {

    private val activity = this@LoginFragment

    private lateinit var nestedScrollView: NestedScrollView

    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout

    private lateinit var textInputEditTextEmail: TextInputEditText
    private lateinit var textInputEditTextPassword: TextInputEditText

    private lateinit var appCompatButtonLogin: AppCompatButton

    private lateinit var textViewLinkRegister: AppCompatTextView

    private lateinit var inputValidation: InputValidation
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_login)

        // hiding the action bar
        supportActionBar!!.hide()

        // initializing the views
        initViews()

        // initializing the listeners
        initListeners()

        // initializing the objects
        initObjects()
    }

    /**
     * This method is to initialize views
     */
    private fun initViews() {

        nestedScrollView = findViewById<View>(R.id.nestedScrollView) as NestedScrollView

        textInputLayoutEmail = findViewById<View>(R.id.textInputLayoutEmail) as TextInputLayout
        textInputLayoutPassword = findViewById<View>(R.id.textInputLayoutPassword) as TextInputLayout

        textInputEditTextEmail = findViewById<View>(R.id.textInputEditTextEmail) as TextInputEditText
        textInputEditTextPassword = findViewById<View>(R.id.textInputEditTextPassword) as TextInputEditText

        appCompatButtonLogin = findViewById<View>(R.id.appCompatButtonLogin) as AppCompatButton

        textViewLinkRegister = findViewById<View>(R.id.textViewLinkRegister) as AppCompatTextView

    }

    /**
     * This method is to initialize listeners
     */
    private fun initListeners() {

        appCompatButtonLogin!!.setOnClickListener(this)
        textViewLinkRegister!!.setOnClickListener(this)
    }

    /**
     * This method is to initialize objects to be used
     */
    private fun initObjects() {

        databaseHelper = DatabaseHelper(activity)
        inputValidation = InputValidation(activity)

    }

    /**
     * This implemented method is to listen the click on view
     *
     * @param v
     */
    override fun onClick(v: View) {
        when (v.id) {
            R.id.appCompatButtonLogin -> verifyFromSQLite()
            R.id.textViewLinkRegister -> {
                // Navigate to RegisterActivity
                val intentRegister = Intent(applicationContext, RegisterFragment::class.java)
                startActivity(intentRegister)
            }
        }
    }

    /**
     * This method is to validate the input text fields and verify login credentials from SQLite
     */
    private fun verifyFromSQLite() {

        if (!inputValidation!!.isInputEditTextFilled(textInputEditTextEmail!!, textInputLayoutEmail!!, getString(R.string.error_message_email))) {
            return
        }
        if (!inputValidation!!.isInputEditTextEmail(textInputEditTextEmail!!, textInputLayoutEmail!!, getString(R.string.error_message_email))) {
            return
        }
        if (!inputValidation!!.isInputEditTextFilled(textInputEditTextPassword!!, textInputLayoutPassword!!, getString(R.string.error_message_password))) {
            return
        }

        if (databaseHelper!!.checkUser(textInputEditTextEmail!!.text.toString().trim { it <= ' ' }, textInputEditTextPassword!!.text.toString().trim { it <= ' ' })) {


            val accountsIntent = Intent(activity, UsersListFragment::class.java)
            accountsIntent.putExtra("EMAIL", textInputEditTextEmail!!.text.toString().trim { it <= ' ' })
            emptyInputEditText()
            startActivity(accountsIntent)


        } else {

            // Snack Bar to show success message that record is wrong
            Snackbar.make(nestedScrollView!!, getString(R.string.error_valid_email_password), Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * This method is to empty all input edit text
     */
    private fun emptyInputEditText() {
        textInputEditTextEmail!!.text = null
        textInputEditTextPassword!!.text = null
    }
}