package com.example.clubs

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
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

        val registerCard = findViewById<View>(R.id.btnRegister)
        val viewDataCard = findViewById<View>(R.id.btnViewData)

        registerCard.setOnClickListener {
            navigateToActivity(RegisterActivity::class.java)
        }

        viewDataCard.setOnClickListener {
            navigateToActivity(DataActivity::class.java)
        }

        setupCardElevationEffects(registerCard)
        setupCardElevationEffects(viewDataCard)
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        val options = ActivityOptions.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        startActivity(intent, options.toBundle())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCardElevationEffects(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.translationZ = 8f
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.translationZ = 0f
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<View>(R.id.btnRegister).translationZ = 0f
        findViewById<View>(R.id.btnViewData).translationZ = 0f
    }
}