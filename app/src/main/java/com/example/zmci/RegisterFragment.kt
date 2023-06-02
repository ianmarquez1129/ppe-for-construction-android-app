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
import com.example.zmci.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout



class RegisterFragment : AppCompatActivity(), View.OnClickListener {

    /*
        RegisterFragment Methods:
            - onCreate              (savedInstanceState: Bundle?)
            - initViews             ()
            - initListeners         ()
            - initObjects           ()
            - onClick               (v: View)
            - postDataToSQLite      ()
            - emptyInputEditText    ()
     */

    private val activity = this@RegisterFragment

    private lateinit var nestedScrollView: NestedScrollView

    private lateinit var textInputLayoutName: TextInputLayout
    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var textInputLayoutConfirmPassword: TextInputLayout

    private lateinit var textInputEditTextName: TextInputEditText
    private lateinit var textInputEditTextEmail: TextInputEditText
    private lateinit var textInputEditTextPassword: TextInputEditText
    private lateinit var textInputEditTextConfirmPassword: TextInputEditText

    private lateinit var appCompatButtonRegister: AppCompatButton
    private lateinit var appCompatTextViewLoginLink: AppCompatTextView

    private lateinit var inputValidation: InputValidation
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_register)

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
        nestedScrollView = findViewById(R.id.nestedScrollView)

        textInputLayoutName = findViewById(R.id.textInputLayoutName)
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail)
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword)
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword)

        textInputEditTextName = findViewById(R.id.textInputEditTextName)
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail)
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword)
        textInputEditTextConfirmPassword = findViewById(R.id.textInputEditTextConfirmPassword)

        appCompatButtonRegister = findViewById(R.id.appCompatButtonRegister)

        appCompatTextViewLoginLink = findViewById(R.id.appCompatTextViewLoginLink)

    }

    /**
     * This method is to initialize listeners
     */
    private fun initListeners() {
        appCompatButtonRegister.setOnClickListener(this)
        appCompatTextViewLoginLink.setOnClickListener(this)

    }

    /**
     * This method is to initialize objects to be used
     */
    private fun initObjects() {
        inputValidation = InputValidation(activity)
        databaseHelper = DatabaseHelper(activity)


    }


    /**
     * This implemented method is to listen the click on view
     *
     * @param v
     */
    override fun onClick(v: View) {
        when (v.id) {

            R.id.appCompatButtonRegister -> postDataToSQLite()

            R.id.appCompatTextViewLoginLink -> finish()
        }
    }

    /**
     * This method is to validate the input text fields and post data to SQLite
     */
    private fun postDataToSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextName, textInputLayoutName, getString(R.string.error_message_name))) {
            return
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_password))) {
            return
        }
        if (!inputValidation.isInputEditTextMatches(textInputEditTextPassword, textInputEditTextConfirmPassword,
                textInputLayoutConfirmPassword, getString(R.string.error_password_match))) {
            return
        }

        if (!databaseHelper.checkUser(textInputEditTextEmail.text.toString().trim())) {

            val user = User(name = textInputEditTextName.text.toString().trim(),
                email = textInputEditTextEmail.text.toString().trim(),
                password = textInputEditTextPassword.text.toString().trim())

            databaseHelper.addUser(user)

            // Snack Bar to show success message that record saved successfully
            Snackbar.make(nestedScrollView, getString(R.string.success_message), Snackbar.LENGTH_LONG).show()
            emptyInputEditText()
            val intentLogin = Intent(this, LoginFragment::class.java)
            startActivity(intentLogin)
            finish()


        } else {
            // Snack Bar to show error message that record already exists
            Snackbar.make(nestedScrollView, getString(R.string.error_email_exists), Snackbar.LENGTH_LONG).show()
        }


    }

    /**
     * This method is to empty all input edit text
     */
    private fun emptyInputEditText() {
        textInputEditTextName.text = null
        textInputEditTextEmail.text = null
        textInputEditTextPassword.text = null
        textInputEditTextConfirmPassword.text = null
    }
}