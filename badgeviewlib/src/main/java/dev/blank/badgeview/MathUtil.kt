package dev.blank.badgeview

import android.graphics.PointF
import kotlin.math.*

/**
 * early @author: chqiu
 * Email:qstumn@163.com
 * @author: Blankyyyy
 */
object MathUtil {
    private const val CIRCLE_RADIAN = 2 * Math.PI
    @JvmStatic
    fun getTanRadian(atan: Double, quadrant: Int): Double {
        var atanResult = atan
        if (atan < 0) {
            atanResult += CIRCLE_RADIAN / 4
        }
        atanResult += CIRCLE_RADIAN / 4 * (quadrant - 1)
        return atanResult
    }

    @JvmStatic
    fun radianToAngle(radian: Double): Double {
        return 360 * (radian / CIRCLE_RADIAN)
    }

    @JvmStatic
    fun getQuadrant(p: PointF, center: PointF): Int {
        if (p.x > center.x) {
            if (p.y > center.y) {
                return 4
            } else if (p.y < center.y) {
                return 1
            }
        } else if (p.x < center.x) {
            if (p.y > center.y) {
                return 3
            } else if (p.y < center.y) {
                return 2
            }
        }
        return -1
    }

    @JvmStatic
    fun getPointDistance(p1: PointF, p2: PointF): Float {
        return sqrt(
            (p1.x - p2.x).toDouble().pow(2.0) + (p1.y - p2.y).toDouble().pow(2.0)
        ).toFloat()
    }

    /**
     * this formula is designed by mabeijianxi
     * website : http://blog.csdn.net/mabeijianxi/article/details/50560361
     *
     * @param circleCenter The circle center point.
     * @param radius       The circle radius.
     * @param slopeLine    The slope of line which cross the pMiddle.
     */
    @JvmStatic
    fun getInnerTangentPoints(
        circleCenter: PointF,
        radius: Float,
        slopeLine: Double?,
        points: MutableList<PointF?>
    ) {
        val radian: Float
        val xOffset: Float
        val yOffset: Float
        if (slopeLine != null) {
            radian = atan(slopeLine).toFloat()
            xOffset = (cos(radian.toDouble()) * radius).toFloat()
            yOffset = (sin(radian.toDouble()) * radius).toFloat()
        } else {
            xOffset = radius
            yOffset = 0f
        }
        points.add(PointF(circleCenter.x + xOffset, circleCenter.y + yOffset))
        points.add(PointF(circleCenter.x - xOffset, circleCenter.y - yOffset))
    }
}