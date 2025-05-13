package com.example.clubs

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnViewData = findViewById<Button>(R.id.btnViewData)

        btnRegister.setOnClickListener {
            // Aquí iría el intent para la pantalla de registro
            // startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnViewData.setOnClickListener {
            // Aquí iría el intent para la pantalla de ver datos
            // startActivity(Intent(this, ViewDataActivity::class.java))
        }
    }
}
