package com.rit.twitdownloader.ui.component

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.rit.twitdownloader.R

class RatingDialogFragment : DialogFragment() {

    private var onRatingSubmitted: ((Int) -> Unit)? = null
    private var onDismissed: (() -> Unit)? = null
    
    private lateinit var starRatingView: StarRatingView
    private lateinit var rateButton: Button
    private lateinit var laterButton: Button
    private lateinit var emojiText: TextView
    
    private var currentRating = 5 // Default to 5 stars as shown in reference

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        setupAnimations()
    }

    private fun initViews(view: View) {
        starRatingView = view.findViewById(R.id.star_rating_view)
        rateButton = view.findViewById(R.id.rate_button)
        laterButton = view.findViewById(R.id.later_button)
        emojiText = view.findViewById(R.id.emoji_text)
        
        // Stars will animate to 5 stars automatically
        currentRating = 5
    }

    private fun setupClickListeners() {
        starRatingView.setOnRatingChangedListener { rating ->
            currentRating = rating
            updateUIForRating(rating)
        }

        rateButton.setOnClickListener {
            if (currentRating >= 4) {
                // Open Play Store for rating
                openPlayStore()
            }
            onRatingSubmitted?.invoke(currentRating)
            dismissWithAnimation()
        }
        
        laterButton.setOnClickListener {
            onDismissed?.invoke()
            dismissWithAnimation()
        }
    }

    private fun setupAnimations() {
        // Entrance animation - slide up with fade in
        view?.let { v ->
            v.alpha = 0f
            v.translationY = 100f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start()
        }
        
        // Animate the celebration emoji
        startEmojiAnimation()
    }
    
    private fun startEmojiAnimation() {
        val bounceAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.emoji_bounce)
        emojiText.startAnimation(bounceAnimation)
        
        // Repeat the animation every 3 seconds
        emojiText.postDelayed({
            if (isAdded) {
                startEmojiAnimation()
            }
        }, 3000)
    }

    private fun updateUIForRating(rating: Int) {
        // Animate star selection with scale effect
        starRatingView.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(100)
            .withEndAction {
                starRatingView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()

        // Update button state based on rating
        if (rating >= 4) {
            rateButton.isEnabled = true
            rateButton.alpha = 1f
        } else {
            rateButton.isEnabled = false
            rateButton.alpha = 0.6f
        }
    }

    private fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${requireContext().packageName}")
                setPackage("com.android.vending")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    private fun dismissWithAnimation() {
        view?.let { v ->
            v.animate()
                .alpha(0f)
                .translationY(100f)
                .setDuration(200)
                .withEndAction {
                    dismiss()
                }
                .start()
        } ?: dismiss()
    }

    fun setOnRatingSubmittedListener(listener: (Int) -> Unit) {
        onRatingSubmitted = listener
    }

    fun setOnDismissedListener(listener: () -> Unit) {
        onDismissed = listener
    }

    companion object {
        fun newInstance(): RatingDialogFragment {
            return RatingDialogFragment()
        }
    }
}
