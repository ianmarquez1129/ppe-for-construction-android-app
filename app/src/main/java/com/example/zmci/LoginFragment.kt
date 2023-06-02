package com.example.zmci

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import com.example.zmci.database.DatabaseHelper
import com.example.zmci.helpers.InputValidation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : AppCompatActivity(), View.OnClickListener {

    /*
        LoginFragment Methods:
            - onCreate              ()
            - initViews             ()
            - initPreferences       ()
            - initListeners         ()
            - initObjects           ()
            - onClick               ()
            - verifyFromSQLite      ()
            - emptyInputEditText    ()
     */

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

    private lateinit var saveLoginCheckBox: CheckBox

    private lateinit var loginPreferences: SharedPreferences
    private lateinit var loginPrefsEditor: SharedPreferences.Editor
    private var saveLogin: Boolean = false
    private lateinit var userEmail: String
    private lateinit var userPassword: String

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

        // initializing the preferences
        initPreferences()

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
        saveLoginCheckBox = findViewById(R.id.saveLoginCheckBox)

    }

    /**
     * This method is to initialize preferences
     */
    private fun initPreferences() {
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        loginPrefsEditor = loginPreferences.edit()

        saveLogin = loginPreferences.getBoolean("saveLogin", false)
        if (saveLogin == true) {
            textInputEditTextEmail.setText(loginPreferences.getString("email", ""))
            textInputEditTextPassword.setText(loginPreferences.getString("password", ""))
            saveLoginCheckBox.isChecked = true
        }
    }

    /**
     * This method is to initialize listeners
     */
    private fun initListeners() {

        appCompatButtonLogin.setOnClickListener(this)
        textViewLinkRegister.setOnClickListener(this)
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
            R.id.appCompatButtonLogin -> {

                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(textInputEditTextEmail.windowToken, 0)

                userEmail = textInputEditTextEmail.text.toString()
                userPassword = textInputEditTextPassword.text.toString()

                if (saveLoginCheckBox.isChecked) {
                    loginPrefsEditor.putBoolean("saveLogin", true)
                    loginPrefsEditor.putString("email", userEmail)
                    loginPrefsEditor.putString("password", userPassword)
                    loginPrefsEditor.commit()
                } else {
                    loginPrefsEditor.clear()
                    loginPrefsEditor.commit()
                }

                verifyFromSQLite()
            }
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

        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_password))) {
            return
        }

        if (databaseHelper.checkUser(textInputEditTextEmail.text.toString().trim { it <= ' ' }, textInputEditTextPassword.text.toString().trim { it <= ' ' })) {


            val userName = textInputEditTextEmail.text.toString().trim { it <= ' ' }
            val userPassword = textInputEditTextPassword.text.toString().trim { it <= ' ' }
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("EMAIL", userName)
            intent.putExtra("PW", userPassword)

            startActivity(intent)
            emptyInputEditText()


        } else {

            // Snack Bar to show success message that record is wrong
            Snackbar.make(nestedScrollView, getString(R.string.error_valid_email_password), Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * This method is to empty all input edit text
     */
    private fun emptyInputEditText() {
        textInputEditTextEmail.text = null
        textInputEditTextPassword.text = null
    }
}