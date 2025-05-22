package com.example.taqs360.splash.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taqs360.R
import com.example.taqs360.databinding.ActivitySplashBinding
import com.example.taqs360.splash.viewmodel.SplashViewModel
import com.example.taqs360.home.view.WeatherActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeInLottie = AnimationUtils.loadAnimation(this, R.anim.fade_in_lottie)

        fadeIn.duration = (fadeIn.duration * 2)
        fadeInLottie.duration = (fadeInLottie.duration * 2)

        binding.titleTextView.startAnimation(fadeIn)
        binding.sloganTextView.startAnimation(fadeIn)
        binding.lottieAnimationView.startAnimation(fadeInLottie)

        viewModel.loadAnimation()
        binding.lottieAnimationView.speed = 0.5f

        viewModel.animationResource.observe(this) { resourceId ->
            binding.lottieAnimationView.setAnimation(resourceId) // Fixed line
            binding.lottieAnimationView.playAnimation()
            Log.d("SplashActivity", "Lottie animation set with resource ID: $resourceId")
        }

        binding.lottieAnimationView.setFailureListener { throwable ->
            Log.e("SplashActivity", "Lottie error: ${throwable.message}", throwable)
        }

        viewModel.navigateToMain.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                startActivity(Intent(this, WeatherActivity::class.java))
                finish()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.triggerNavigation()
        }, 8000)
    }
}