package com.example.zmci

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.zmci.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this,
        R.layout.activity_main)

        drawerLayout = binding.drawerLayout

        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)

        // get reference to all views
//        var etUserName = findViewById(R.id.etUserName) as EditText
//        var etPassword = findViewById(R.id.etPassword) as EditText
//        var btnReset = findViewById(R.id.btnReset) as Button
//        var btnLogin = findViewById(R.id.btnLogin) as Button
//
//        btnReset.setOnClickListener {
//            // clearing user_name and password edit text views on reset button click
//            etUserName.setText("")
//            etPassword.setText("")
//        }
//        btnLogin.setOnClickListener {
//            if (etUserName.text.toString() == "admin" && etPassword.text.toString() == "pass123") {
//                //correct password
//                Toast.makeText(applicationContext,"Login Success!",Toast.LENGTH_SHORT).show()
//                val toast = Toast.makeText(applicationContext, "Hello ADMIN!", Toast.LENGTH_SHORT)
//                toast.show()
//            } else {
//                //wrong password
//                Toast.makeText(applicationContext, "Wrong credentials.", Toast.LENGTH_SHORT).show()
//            }
//        }


    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }
}