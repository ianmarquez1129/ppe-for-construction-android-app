package com.example.zmci

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashFragment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash)

        supportActionBar?.hide()

        Handler().postDelayed({
            val intent = Intent(this@SplashFragment,LoginFragment::class.java)
            startActivity(intent)
            finish()
        },3000)

    }
}