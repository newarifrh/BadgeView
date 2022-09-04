package dev.blank.badgeview

import android.graphics.drawable.Drawable
import android.graphics.PointF
import android.view.View

/**
 * early @author: chqiu
 * Email:qstumn@163.com
 * @author: Blankyyyy
 */
interface Badge {
    fun setBadgeNumber(badgeNum: Int): Badge?
    val badgeNumber: Int
    fun setBadgeText(badgeText: String?): Badge?
    val badgeText: String?
    fun setExactMode(isExact: Boolean): Badge?
    val isExactMode: Boolean
    fun setShowShadow(showShadow: Boolean): Badge?
    val isShowShadow: Boolean
    fun setBadgeBackgroundColor(color: Int): Badge?
    fun stroke(color: Int, width: Float, isDpValue: Boolean): Badge?
    val badgeBackgroundColor: Int
    fun setBadgeBackground(drawable: Drawable?): Badge?
    fun setBadgeBackground(drawable: Drawable?, clip: Boolean): Badge?
    val badgeBackground: Drawable?
    fun setBadgeTextColor(color: Int): Badge?
    val badgeTextColor: Int
    fun setBadgeTextSize(size: Float, isSpValue: Boolean): Badge?
    fun getBadgeTextSize(isSpValue: Boolean): Float
    fun setBadgePadding(padding: Float, isDpValue: Boolean): Badge?
    fun getBadgePadding(isDpValue: Boolean): Float
    val isDraggable: Boolean
    fun setBadgeGravity(gravity: Int): Badge?
    val badgeGravity: Int
    fun setGravityOffset(offset: Float, isDpValue: Boolean): Badge?
    fun setGravityOffset(offsetX: Float, offsetY: Float, isDpValue: Boolean): Badge?
    fun getGravityOffsetX(isDpValue: Boolean): Float
    fun getGravityOffsetY(isDpValue: Boolean): Float
    fun setOnDragStateChangedListener(l: OnDragStateChangedListener?): Badge?
    val dragCenter: PointF?
    fun bindTarget(view: View?): Badge?
    val targetView: View?
    fun hide(animate: Boolean)
    interface OnDragStateChangedListener {
        fun onDragStateChanged(dragState: Int, badge: Badge?, targetView: View?)

        companion object {
            const val STATE_START = 1
            const val STATE_DRAGGING = 2
            const val STATE_DRAGGING_OUT_OF_RANGE = 3
            const val STATE_CANCELED = 4
            const val STATE_SUCCEED = 5
        }
    }
}