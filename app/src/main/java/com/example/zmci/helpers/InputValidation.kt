package com.example.zmci.helpers

import android.app.Activity
import android.view.WindowManager
import android.content.Context
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.view.inputmethod.InputMethodManager
import android.view.View
import androidx.appcompat.widget.AppCompatEditText


class InputValidation
/**
 * constructor
 *
 * @param context
 */
    (private val context: Context) {

    /**
     * method to check InputEditText filled .
     *
     * @param textInputEditText
     * @param textInputLayout
     * @param message
     * @return
     */
    fun isInputEditTextFilled(textInputEditText: AppCompatEditText, textInputLayout: TextInputLayout, message: String): Boolean {
        val value = textInputEditText.text.toString().trim()
        if (value.isEmpty()) {
            textInputLayout.error = message
            hideKeyboardFrom(textInputEditText)
            return false
        } else {
            textInputLayout.isErrorEnabled = false
        }

        return true
    }


    /**
     * method to check InputEditText has valid email .
     *
     * @param textInputEditText
     * @param textInputLayout
     * @param message
     * @return
     */
    fun isInputEditTextEmail(textInputEditText: AppCompatEditText, textInputLayout: TextInputLayout, message: String): Boolean {
        val value = textInputEditText.text.toString().trim()
        if (value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            textInputLayout.error = message
            hideKeyboardFrom(textInputEditText)
            return false
        } else {
            textInputLayout.isErrorEnabled = false
        }
        return true
    }

    /**
     * method to check both InputEditText value matches.
     *
     * @param textInputEditText1
     * @param textInputEditText2
     * @param textInputLayout
     * @param message
     * @return
     */
    fun isInputEditTextMatches(textInputEditText1: TextInputEditText, textInputEditText2: TextInputEditText, textInputLayout: TextInputLayout, message: String): Boolean {
        val value1 = textInputEditText1.text.toString().trim()
        val value2 = textInputEditText2.text.toString().trim()
        if (!value1.contentEquals(value2)) {
            textInputLayout.error = message
            hideKeyboardFrom(textInputEditText2)
            return false
        } else {
            textInputLayout.isErrorEnabled = false
        }
        return true
    }

    /**
     * method to Hide keyboard
     *
     * @param view
     */
    private fun hideKeyboardFrom(view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
}