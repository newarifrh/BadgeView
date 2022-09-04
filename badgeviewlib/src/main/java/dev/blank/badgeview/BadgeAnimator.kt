package dev.blank.badgeview

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.PointF
import android.animation.ValueAnimator
import android.animation.AnimatorListenerAdapter
import android.graphics.Canvas
import android.graphics.Paint
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * early @author: chqiu
 * Email:qstumn@163.com
 * @author: Blankyyyy
 * Animation borrowed from https://github.com/tyrantgit/ExplosionField
 */
class BadgeAnimator(badgeBitmap: Bitmap, center: PointF, badge: QBadgeView) : ValueAnimator() {
    private val mFragments: Array<Array<BitmapFragment?>>
    private val mWeakBadge: WeakReference<QBadgeView>
    fun draw(canvas: Canvas) {
        for (i in mFragments.indices) {
            for (j in 0 until mFragments[i].size) {
                val bf = mFragments[i][j]
                val value = animatedValue.toString().toFloat()
                bf!!.update(value, canvas)
            }
        }
    }

    private fun getFragments(badgeBitmap: Bitmap, center: PointF): Array<Array<BitmapFragment?>> {
        val width = badgeBitmap.width
        val height = badgeBitmap.height
        val fragmentSize = min(width, height) / 6f
        val startX = center.x - badgeBitmap.width / 2f
        val startY = center.y - badgeBitmap.height / 2f
        val fragments =
            Array((height / fragmentSize).toInt()) { arrayOfNulls<BitmapFragment>((width / fragmentSize).toInt()) }
        for (i in fragments.indices) {
            for (j in 0 until fragments[i].size) {
                val bf = BitmapFragment()
                bf.color =
                    badgeBitmap.getPixel((j * fragmentSize).toInt(), (i * fragmentSize).toInt())
                bf.x = startX + j * fragmentSize
                bf.y = startY + i * fragmentSize
                bf.size = fragmentSize
                bf.maxSize = max(width, height)
                fragments[i][j] = bf
            }
        }
        badgeBitmap.recycle()
        return fragments
    }

    private inner class BitmapFragment {
        var random: Random
        var x = 0f
        var y = 0f
        var size = 0f
        var color = 0
        var maxSize = 0
        var paint: Paint = Paint()
        fun update(value: Float, canvas: Canvas) {
            paint.color = color
            x += 0.1f * random.nextInt(maxSize) * (random.nextFloat() - 0.5f)
            y += 0.1f * random.nextInt(maxSize) * (random.nextFloat() - 0.5f)
            canvas.drawCircle(x, y, size - value * size, paint)
        }

        init {
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            random = Random()
        }
    }

    init {
        mWeakBadge = WeakReference(badge)
        setFloatValues(0f, 1f)
        duration = 500
        mFragments = getFragments(badgeBitmap, center)
        addUpdateListener {
            val badgeView = mWeakBadge.get()
            if (badgeView == null || !badgeView.isShown) {
                cancel()
            } else {
                badgeView.invalidate()
            }
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val badgeView = mWeakBadge.get()
                badgeView?.reset()
            }
        })
    }
}