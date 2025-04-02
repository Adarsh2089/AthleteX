package com.example.athletex.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.athletex.R

/**
 * Fragment class for the second onboarding screen
 */
class SecondOnboardingFragment : Fragment() {

    // Called to create the view for this fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.second_fragment_onboarding, container, false)

        // Find the LottieAnimationView after inflating the view
        val lottieAnimationView = view.findViewById<LottieAnimationView>(R.id.lottieAnimation)
        lottieAnimationView.setAnimation(R.raw.animation3) // JSON file from res/raw
        lottieAnimationView.playAnimation() // Start animation

        return view

       }
}