package dev.blank.badgeview

import android.content.Context

/**
 * early @author: chqiu
 * Email:qstumn@163.com
 * @author: Blankyyyy
 */
object DisplayUtil {
    @JvmStatic
    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    @JvmStatic
    fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
}