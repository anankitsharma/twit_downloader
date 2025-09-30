package com.rit.twitdownloader.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.rit.twitdownloader.R

class StarRatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var starCount = 5
    private var rating = 0 // Start with 0 for animation
    private var animatedRating = 0f // For smooth animation
    private var starSize = 0f
    private var starSpacing = 0f
    private var starRadius = 0f
    private var isAnimating = false
    private var fifthStarPulse = 1f // For 5th star pulse animation
    
    private val starFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.rating_star_fill)
        style = Paint.Style.FILL
    }
    
    private val starBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.rating_star_background)
        style = Paint.Style.FILL
    }
    
    private val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    
    private var onRatingChangedListener: ((Int) -> Unit)? = null
    
    init {
        setWillNotDraw(false)
        // Start animation to fill stars from 1 to 5
        startFillAnimation()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDimensions()
    }
    
    private fun calculateDimensions() {
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        
        starSize = minOf(availableWidth / starCount, availableHeight).toFloat()
        starSpacing = (availableWidth - starSize * starCount) / (starCount - 1)
        starRadius = starSize * 0.4f
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerY = height / 2f
        
        for (i in 0 until starCount) {
            val centerX = paddingLeft + starSize / 2 + i * (starSize + starSpacing)
            val isFilled = i < animatedRating
            val isFifthStar = i == 4
            
            // Draw star background circle
            canvas.drawCircle(centerX, centerY, starRadius, starBackgroundPaint)
            
            // Draw star with special effect for 5th star
            if (isFifthStar && isFilled) {
                // Draw 5th star with pulse effect
                canvas.save()
                canvas.scale(fifthStarPulse, fifthStarPulse, centerX, centerY)
                drawStar(canvas, centerX, centerY, starSize * 0.3f, isFilled)
                canvas.restore()
                
                // Draw sparkle effect for 5th star
                drawSparkles(canvas, centerX, centerY)
            } else {
                // Draw regular star
                drawStar(canvas, centerX, centerY, starSize * 0.3f, isFilled)
            }
        }
    }
    
    private fun drawStar(canvas: Canvas, centerX: Float, centerY: Float, size: Float, filled: Boolean) {
        val paint = starFillPaint
        
        val path = Path()
        val outerRadius = size
        val innerRadius = size * 0.4f
        val points = 5
        
        for (i in 0 until points * 2) {
            val angle = (i * Math.PI / points - Math.PI / 2).toFloat()
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val x = centerX + radius * kotlin.math.cos(angle)
            val y = centerY + radius * kotlin.math.sin(angle)
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        
        canvas.drawPath(path, paint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val starIndex = ((x - paddingLeft) / (starSize + starSpacing)).toInt()
            
            if (starIndex in 0 until starCount) {
                val newRating = starIndex + 1
                if (newRating != rating) {
                    rating = newRating
                    invalidate()
                    onRatingChangedListener?.invoke(rating)
                    
                    // Add haptic feedback
                    performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    fun setRating(rating: Int) {
        this.rating = rating.coerceIn(0, starCount)
        invalidate()
    }
    
    fun getRating(): Int = rating
    
    fun setOnRatingChangedListener(listener: (Int) -> Unit) {
        onRatingChangedListener = listener
    }
    
    private fun drawSparkles(canvas: Canvas, centerX: Float, centerY: Float) {
        val sparkleSize = starSize * 0.15f
        
        // Draw sparkle lines radiating from the star
        canvas.drawLine(centerX + sparkleSize, centerY - sparkleSize, centerX + sparkleSize * 1.5f, centerY - sparkleSize * 1.5f, sparklePaint)
        canvas.drawLine(centerX - sparkleSize, centerY - sparkleSize, centerX - sparkleSize * 1.5f, centerY - sparkleSize * 1.5f, sparklePaint)
        canvas.drawLine(centerX + sparkleSize, centerY + sparkleSize, centerX + sparkleSize * 1.5f, centerY + sparkleSize * 1.5f, sparklePaint)
        canvas.drawLine(centerX - sparkleSize, centerY + sparkleSize, centerX - sparkleSize * 1.5f, centerY + sparkleSize * 1.5f, sparklePaint)
    }
    
    private fun startFillAnimation() {
        if (isAnimating) return
        isAnimating = true
        
        val animator = android.animation.ValueAnimator.ofFloat(0f, 5f)
        animator.duration = 1500 // 1.5 seconds total
        animator.addUpdateListener { animation ->
            animatedRating = animation.animatedValue as Float
            invalidate()
        }
        animator.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) {
                rating = 5
                animatedRating = 5f
                isAnimating = false
                onRatingChangedListener?.invoke(5)
                
                // Start 5th star pulse animation
                startFifthStarPulse()
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
        animator.start()
    }
    
    private fun startFifthStarPulse() {
        val pulseAnimator = android.animation.ValueAnimator.ofFloat(1f, 1.2f, 1f)
        pulseAnimator.duration = 800
        pulseAnimator.repeatCount = android.animation.ValueAnimator.INFINITE
        pulseAnimator.addUpdateListener { animation ->
            fifthStarPulse = animation.animatedValue as Float
            invalidate()
        }
        pulseAnimator.start()
    }
}
