package com.prm.tasksboard

import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.ImageView

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        val signInButton = findViewById<MaterialButton>(R.id.sign_in_button)
        signInButton.startAnimation(fadeInAnimation)
        setButtonAnimation(signInButton)

        val logo = findViewById<ImageView>(R.id.logo)
        logo.startAnimation(fadeInAnimation)
    }

    private fun setButtonAnimation(button: MaterialButton) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Scale down the button when it's pressed
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).start()
                }
                MotionEvent.ACTION_UP -> {
                    // Scale up the button when it's released
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    v.performClick()  // Perform the click action
                }
                MotionEvent.ACTION_CANCEL -> {
                    // Scale up the button when the touch is cancelled
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                }
            }
            true  // Return true to indicate that the touch event has been handled
        }
    }
}