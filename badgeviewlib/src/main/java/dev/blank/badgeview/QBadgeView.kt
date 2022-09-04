package dev.blank.badgeview

import android.content.Context
import android.graphics.*
import dev.blank.badgeview.DisplayUtil.dp2px
import dev.blank.badgeview.MathUtil.getPointDistance
import dev.blank.badgeview.MathUtil.getQuadrant
import dev.blank.badgeview.MathUtil.radianToAngle
import dev.blank.badgeview.MathUtil.getTanRadian
import dev.blank.badgeview.DisplayUtil.px2dp
import android.graphics.drawable.Drawable
import android.text.TextPaint
import dev.blank.badgeview.Badge.OnDragStateChangedListener
import android.os.Build
import android.widget.RelativeLayout
import android.widget.FrameLayout
import android.text.TextUtils
import android.util.SparseArray
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import dev.blank.badgeview.MathUtil.getInnerTangentPoints
import java.lang.IllegalStateException
import java.util.ArrayList

/**
 * early @author chqiu
 * Email:qstumn@163.com
 * @author: Blankyyyy
 */
class QBadgeView private constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Badge {
    override var badgeBackgroundColor = 0
        protected set
    protected var mColorBackgroundBorder = 0
    override var badgeTextColor = 0
        protected set
    override var badgeBackground: Drawable? = null
        protected set
    protected var mBitmapClip: Bitmap? = null
    protected var mDrawableBackgroundClip = false
    protected var mBackgroundBorderWidth = 0f
    protected var mBadgeTextSize = 0f
    protected var mBadgePadding = 0f
    override var badgeNumber = 0
        protected set
    override var badgeText: String? = null
        protected set
    override var isDraggable = false
        protected set
    protected var mDragging = false
    override var isExactMode = false
        protected set
    override var isShowShadow = false
        protected set
    override var badgeGravity = 0
        protected set
    protected var mGravityOffsetX = 0f
    protected var mGravityOffsetY = 0f
    protected var mDefalutRadius = 0f
    protected var mFinalDragDistance = 0f
    protected var mDragQuadrant = 0
    protected var mDragOutOfRange = false
    protected var mBadgeTextRect: RectF? = null
    protected var mBadgeBackgroundRect: RectF? = null
    protected var mDragPath: Path? = null
    protected var mBadgeTextFontMetrics: Paint.FontMetrics? = null
    protected var mBadgeCenter: PointF? = null
    protected var mDragCenter: PointF? = null
    protected var mRowBadgeCenter: PointF? = null
    protected var mControlPoint: PointF? = null
    protected var mInnertangentPoints: MutableList<PointF?>? = null
    override var targetView: View? = null
        protected set
    protected var mWidth = 0
    protected var mHeight = 0
    protected var mBadgeTextPaint: TextPaint? = null
    protected var mBadgeBackgroundPaint: Paint? = null
    protected var mBadgeBackgroundBorderPaint: Paint? = null
    protected var mAnimator: BadgeAnimator? = null
    protected var mDragStateChangedListener: OnDragStateChangedListener? = null
    protected var mActivityRoot: ViewGroup? = null

    constructor(context: Context) : this(context, null) {}

    private fun init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        mBadgeTextRect = RectF()
        mBadgeBackgroundRect = RectF()
        mDragPath = Path()
        mBadgeCenter = PointF()
        mDragCenter = PointF()
        mRowBadgeCenter = PointF()
        mControlPoint = PointF()
        mInnertangentPoints = ArrayList()
        mBadgeTextPaint = TextPaint()
        mBadgeTextPaint!!.isAntiAlias = true
        mBadgeTextPaint!!.isSubpixelText = true
        mBadgeTextPaint!!.isFakeBoldText = true
        mBadgeTextPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        mBadgeBackgroundPaint = Paint()
        mBadgeBackgroundPaint!!.isAntiAlias = true
        mBadgeBackgroundPaint!!.style = Paint.Style.FILL
        mBadgeBackgroundBorderPaint = Paint()
        mBadgeBackgroundBorderPaint!!.isAntiAlias = true
        mBadgeBackgroundBorderPaint!!.style = Paint.Style.STROKE
        badgeBackgroundColor = -0x17b1c0
        badgeTextColor = -0x1
        mBadgeTextSize = dp2px(context, 11f).toFloat()
        mBadgePadding = dp2px(context, 5f).toFloat()
        badgeNumber = 0
        badgeGravity = Gravity.END or Gravity.TOP
        mGravityOffsetX = dp2px(context, 1f).toFloat()
        mGravityOffsetY = dp2px(context, 1f).toFloat()
        mFinalDragDistance = dp2px(context, 90f).toFloat()
        isShowShadow = true
        mDrawableBackgroundClip = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            translationZ = 1000f
        }
    }

    override fun bindTarget(targetView: View?): Badge? {
        checkNotNull(targetView) { "targetView can not be null" }
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
        val targetParent = targetView.parent
        if (targetParent != null && targetParent is ViewGroup) {
            this.targetView = targetView
            if (targetParent is BadgeContainer) {
                targetParent.addView(this)
            } else {
                val targetContainer = targetParent
                val index = targetContainer.indexOfChild(targetView)
                val targetParams = targetView.layoutParams
                targetContainer.removeView(targetView)
                val badgeContainer = BadgeContainer(context)
                if (targetContainer is RelativeLayout) {
                    badgeContainer.id = targetView.id
                }
                targetContainer.addView(badgeContainer, index, targetParams)
                badgeContainer.addView(targetView)
                badgeContainer.addView(this)
            }
        } else {
            throw IllegalStateException("targetView must have a parent")
        }
        return this
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mActivityRoot == null) findViewRoot(targetView)
    }

    private fun findViewRoot(view: View?) {
        mActivityRoot = view!!.rootView as ViewGroup
        if (mActivityRoot == null) {
            findActivityRoot(view)
        }
    }

    private fun findActivityRoot(view: View?) {
        if (view!!.parent != null && view.parent is View) {
            findActivityRoot(view.parent as View)
        } else if (view is ViewGroup) {
            mActivityRoot = view
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.x
                val y = event.y
                if (isDraggable && event.getPointerId(event.actionIndex) == 0 && x > mBadgeBackgroundRect!!.left && x < mBadgeBackgroundRect!!.right && y > mBadgeBackgroundRect!!.top && y < mBadgeBackgroundRect!!.bottom
                    && badgeText != null
                ) {
                    initRowBadgeCenter()
                    mDragging = true
                    updataListener(OnDragStateChangedListener.STATE_START)
                    mDefalutRadius = dp2px(context, 7f).toFloat()
                    parent.requestDisallowInterceptTouchEvent(true)
                    screenFromWindow(true)
                    mDragCenter!!.x = event.rawX
                    mDragCenter!!.y = event.rawY
                }
            }
            MotionEvent.ACTION_MOVE -> if (mDragging) {
                mDragCenter!!.x = event.rawX
                mDragCenter!!.y = event.rawY
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> if (event.getPointerId(
                    event.actionIndex
                ) == 0 && mDragging
            ) {
                mDragging = false
                onPointerUp()
            }
        }
        return mDragging || super.onTouchEvent(event)
    }

    private fun onPointerUp() {
        if (mDragOutOfRange) {
            animateHide(mDragCenter)
            updataListener(OnDragStateChangedListener.STATE_SUCCEED)
        } else {
            reset()
            updataListener(OnDragStateChangedListener.STATE_CANCELED)
        }
    }

    protected fun createBadgeBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            mBadgeBackgroundRect!!.width().toInt() + dp2px(context, 3f),
            mBadgeBackgroundRect!!.height().toInt() + dp2px(context, 3f), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawBadge(canvas, PointF(canvas.width / 2f, canvas.height / 2f), badgeCircleRadius)
        return bitmap
    }

    protected fun screenFromWindow(screen: Boolean) {
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
        if (screen) {
            mActivityRoot!!.addView(
                this, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        } else {
            bindTarget(targetView)
        }
    }

    private fun showShadowImpl(showShadow: Boolean) {
        var x = dp2px(context, 1f)
        var y = dp2px(context, 1.5f)
        when (mDragQuadrant) {
            1 -> {
                x = dp2px(context, 1f)
                y = dp2px(context, -1.5f)
            }
            2 -> {
                x = dp2px(context, -1f)
                y = dp2px(context, -1.5f)
            }
            3 -> {
                x = dp2px(context, -1f)
                y = dp2px(context, 1.5f)
            }
            4 -> {
                x = dp2px(context, 1f)
                y = dp2px(context, 1.5f)
            }
        }

        val radius = if (showShadow) {
            dp2px(context, 2f).toFloat()
        } else {
            0.toFloat()
        }
        mBadgeBackgroundPaint!!.setShadowLayer(
            radius,
            x.toFloat(),
            y.toFloat(),
            0x33000000
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        if (mAnimator != null && mAnimator!!.isRunning) {
            mAnimator!!.draw(canvas)
            return
        }
        if (badgeText != null) {
            initPaints()
            val badgeRadius = badgeCircleRadius
            val startCircleRadius = mDefalutRadius * (1 - getPointDistance(
                mRowBadgeCenter!!, mDragCenter!!
            ) / mFinalDragDistance)
            if (isDraggable && mDragging) {
                mDragQuadrant = getQuadrant(mDragCenter!!, mRowBadgeCenter!!)
                showShadowImpl(isShowShadow)
                if (startCircleRadius < dp2px(context, 1.5f)) {
                    mDragOutOfRange = true
                    updataListener(OnDragStateChangedListener.STATE_DRAGGING_OUT_OF_RANGE)
                    drawBadge(canvas, mDragCenter, badgeRadius)
                } else {
                    mDragOutOfRange = false
                    updataListener(OnDragStateChangedListener.STATE_DRAGGING)
                    drawDragging(canvas, startCircleRadius, badgeRadius)
                    drawBadge(canvas, mDragCenter, badgeRadius)
                }
            } else {
                findBadgeCenter()
                drawBadge(canvas, mBadgeCenter, badgeRadius)
            }
        }
    }

    private fun initPaints() {
        showShadowImpl(isShowShadow)
        mBadgeBackgroundPaint!!.color = badgeBackgroundColor
        mBadgeBackgroundBorderPaint!!.color = mColorBackgroundBorder
        mBadgeBackgroundBorderPaint!!.strokeWidth = mBackgroundBorderWidth
        mBadgeTextPaint!!.color = badgeTextColor
        mBadgeTextPaint!!.textAlign = Paint.Align.CENTER
    }

    private fun drawDragging(canvas: Canvas, startRadius: Float, badgeRadius: Float) {
        val dy = mDragCenter!!.y - mRowBadgeCenter!!.y
        val dx = mDragCenter!!.x - mRowBadgeCenter!!.x
        mInnertangentPoints!!.clear()
        if (dx != 0f) {
            val k1 = (dy / dx).toDouble()
            val k2 = -1 / k1
            getInnerTangentPoints(mDragCenter!!, badgeRadius, k2, mInnertangentPoints!!)
            getInnerTangentPoints(mRowBadgeCenter!!, startRadius, k2, mInnertangentPoints!!)
        } else {
            getInnerTangentPoints(mDragCenter!!, badgeRadius, 0.0, mInnertangentPoints!!)
            getInnerTangentPoints(mRowBadgeCenter!!, startRadius, 0.0, mInnertangentPoints!!)
        }
        mDragPath!!.reset()
        mDragPath!!.addCircle(
            mRowBadgeCenter!!.x, mRowBadgeCenter!!.y, startRadius,
            if (mDragQuadrant == 1 || mDragQuadrant == 2) Path.Direction.CCW else Path.Direction.CW
        )
        mControlPoint!!.x = (mRowBadgeCenter!!.x + mDragCenter!!.x) / 2.0f
        mControlPoint!!.y = (mRowBadgeCenter!!.y + mDragCenter!!.y) / 2.0f
        mDragPath!!.moveTo(mInnertangentPoints!![2]!!.x, mInnertangentPoints!![2]!!.y)
        mDragPath!!.quadTo(
            mControlPoint!!.x,
            mControlPoint!!.y,
            mInnertangentPoints!![0]!!.x,
            mInnertangentPoints!![0]!!.y
        )
        mDragPath!!.lineTo(mInnertangentPoints!![1]!!.x, mInnertangentPoints!![1]!!.y)
        mDragPath!!.quadTo(
            mControlPoint!!.x,
            mControlPoint!!.y,
            mInnertangentPoints!![3]!!.x,
            mInnertangentPoints!![3]!!.y
        )
        mDragPath!!.lineTo(mInnertangentPoints!![2]!!.x, mInnertangentPoints!![2]!!.y)
        mDragPath!!.close()
        canvas.drawPath(mDragPath!!, mBadgeBackgroundPaint!!)

        //draw dragging border
        if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
            mDragPath!!.reset()
            mDragPath!!.moveTo(mInnertangentPoints!![2]!!.x, mInnertangentPoints!![2]!!.y)
            mDragPath!!.quadTo(
                mControlPoint!!.x,
                mControlPoint!!.y,
                mInnertangentPoints!![0]!!.x,
                mInnertangentPoints!![0]!!.y
            )
            mDragPath!!.moveTo(mInnertangentPoints!![1]!!.x, mInnertangentPoints!![1]!!.y)
            mDragPath!!.quadTo(
                mControlPoint!!.x,
                mControlPoint!!.y,
                mInnertangentPoints!![3]!!.x,
                mInnertangentPoints!![3]!!.y
            )
            val startY: Float
            val startX: Float
            if (mDragQuadrant == 1 || mDragQuadrant == 2) {
                startX = mInnertangentPoints!![2]!!.x - mRowBadgeCenter!!.x
                startY = mRowBadgeCenter!!.y - mInnertangentPoints!![2]!!.y
            } else {
                startX = mInnertangentPoints!![3]!!.x - mRowBadgeCenter!!.x
                startY = mRowBadgeCenter!!.y - mInnertangentPoints!![3]!!.y
            }
            val startAngle = 360 - radianToAngle(
                getTanRadian(
                    Math.atan((startY / startX).toDouble()),
                    if (mDragQuadrant - 1 == 0) 4 else mDragQuadrant - 1
                )
            ).toFloat()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mDragPath!!.addArc(
                    mRowBadgeCenter!!.x - startRadius,
                    mRowBadgeCenter!!.y - startRadius,
                    mRowBadgeCenter!!.x + startRadius,
                    mRowBadgeCenter!!.y + startRadius,
                    startAngle,
                    180f
                )
            } else {
                mDragPath!!.addArc(
                    RectF(
                        mRowBadgeCenter!!.x - startRadius, mRowBadgeCenter!!.y - startRadius,
                        mRowBadgeCenter!!.x + startRadius, mRowBadgeCenter!!.y + startRadius
                    ), startAngle, 180f
                )
            }
            canvas.drawPath(mDragPath!!, mBadgeBackgroundBorderPaint!!)
        }
    }

    private fun drawBadge(canvas: Canvas, center: PointF?, radius: Float) {
        var radius = radius
        if (center!!.x == -1000f && center.y == -1000f) {
            return
        }
        if (badgeText!!.isEmpty() || badgeText!!.length == 1) {
            mBadgeBackgroundRect!!.left = center.x - radius.toInt()
            mBadgeBackgroundRect!!.top = center.y - radius.toInt()
            mBadgeBackgroundRect!!.right = center.x + radius.toInt()
            mBadgeBackgroundRect!!.bottom = center.y + radius.toInt()
            if (badgeBackground != null) {
                drawBadgeBackground(canvas)
            } else {
                canvas.drawCircle(center.x, center.y, radius, mBadgeBackgroundPaint!!)
                if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
                    canvas.drawCircle(center.x, center.y, radius, mBadgeBackgroundBorderPaint!!)
                }
            }
        } else {
            mBadgeBackgroundRect!!.left = center.x - (mBadgeTextRect!!.width() / 2f + mBadgePadding)
            mBadgeBackgroundRect!!.top =
                center.y - (mBadgeTextRect!!.height() / 2f + mBadgePadding * 0.5f)
            mBadgeBackgroundRect!!.right =
                center.x + (mBadgeTextRect!!.width() / 2f + mBadgePadding)
            mBadgeBackgroundRect!!.bottom =
                center.y + (mBadgeTextRect!!.height() / 2f + mBadgePadding * 0.5f)
            radius = mBadgeBackgroundRect!!.height() / 2f
            if (badgeBackground != null) {
                drawBadgeBackground(canvas)
            } else {
                canvas.drawRoundRect(
                    mBadgeBackgroundRect!!,
                    radius,
                    radius,
                    mBadgeBackgroundPaint!!
                )
                if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
                    canvas.drawRoundRect(
                        mBadgeBackgroundRect!!,
                        radius,
                        radius,
                        mBadgeBackgroundBorderPaint!!
                    )
                }
            }
        }
        if (!badgeText!!.isEmpty()) {
            canvas.drawText(
                badgeText!!, center.x,
                (mBadgeBackgroundRect!!.bottom + mBadgeBackgroundRect!!.top - mBadgeTextFontMetrics!!.bottom - mBadgeTextFontMetrics!!.top) / 2f,
                mBadgeTextPaint!!
            )
        }
    }

    private fun drawBadgeBackground(canvas: Canvas) {
        mBadgeBackgroundPaint!!.setShadowLayer(0f, 0f, 0f, 0)
        val left = mBadgeBackgroundRect!!.left.toInt()
        val top = mBadgeBackgroundRect!!.top.toInt()
        var right = mBadgeBackgroundRect!!.right.toInt()
        var bottom = mBadgeBackgroundRect!!.bottom.toInt()
        if (mDrawableBackgroundClip) {
            right = left + mBitmapClip!!.width
            bottom = top + mBitmapClip!!.height
            canvas.saveLayer(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                null,
                Canvas.ALL_SAVE_FLAG
            )
        }
        badgeBackground!!.setBounds(left, top, right, bottom)
        badgeBackground!!.draw(canvas)
        if (mDrawableBackgroundClip) {
            mBadgeBackgroundPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            canvas.drawBitmap(mBitmapClip!!, left.toFloat(), top.toFloat(), mBadgeBackgroundPaint)
            canvas.restore()
            mBadgeBackgroundPaint!!.xfermode = null
            if (badgeText!!.isEmpty() || badgeText!!.length == 1) {
                canvas.drawCircle(
                    mBadgeBackgroundRect!!.centerX(), mBadgeBackgroundRect!!.centerY(),
                    mBadgeBackgroundRect!!.width() / 2f, mBadgeBackgroundBorderPaint!!
                )
            } else {
                canvas.drawRoundRect(
                    mBadgeBackgroundRect!!,
                    mBadgeBackgroundRect!!.height() / 2, mBadgeBackgroundRect!!.height() / 2,
                    mBadgeBackgroundBorderPaint!!
                )
            }
        } else {
            canvas.drawRect(mBadgeBackgroundRect!!, mBadgeBackgroundBorderPaint!!)
        }
    }

    private fun createClipLayer() {
        if (badgeText == null) {
            return
        }
        if (!mDrawableBackgroundClip) {
            return
        }
        if (mBitmapClip != null && !mBitmapClip!!.isRecycled) {
            mBitmapClip!!.recycle()
        }
        val radius = badgeCircleRadius
        if (badgeText!!.isEmpty() || badgeText!!.length == 1) {
            mBitmapClip = Bitmap.createBitmap(
                radius.toInt() * 2, radius.toInt() * 2,
                Bitmap.Config.ARGB_4444
            )
            mBitmapClip?.let {
                val srcCanvas = Canvas(it)
                srcCanvas.drawCircle(
                    srcCanvas.width / 2f, srcCanvas.height / 2f,
                    srcCanvas.width / 2f, mBadgeBackgroundPaint!!
                )
            }


        } else {
            mBitmapClip = Bitmap.createBitmap(
                (mBadgeTextRect!!.width() + mBadgePadding * 2).toInt(),
                (mBadgeTextRect!!.height() + mBadgePadding).toInt(),
                Bitmap.Config.ARGB_4444
            )
            mBitmapClip?.let {
                val srcCanvas = Canvas(it)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    srcCanvas.drawRoundRect(
                        0f,
                        0f,
                        srcCanvas.width.toFloat(),
                        srcCanvas.height.toFloat(),
                        srcCanvas.height / 2f,
                        srcCanvas.height / 2f,
                        mBadgeBackgroundPaint!!
                    )
                } else {
                    srcCanvas.drawRoundRect(
                        RectF(
                            0F, 0F, srcCanvas.width.toFloat(), srcCanvas.height
                                .toFloat()
                        ),
                        srcCanvas.height / 2f, srcCanvas.height / 2f, mBadgeBackgroundPaint!!
                    )
                }
            }


        }
    }

    private val badgeCircleRadius: Float
        get() = if (badgeText!!.isEmpty()) {
            mBadgePadding
        } else if (badgeText!!.length == 1) {
            if (mBadgeTextRect!!.height() > mBadgeTextRect!!.width()) mBadgeTextRect!!.height() / 2f + mBadgePadding * 0.5f else mBadgeTextRect!!.width() / 2f + mBadgePadding * 0.5f
        } else {
            mBadgeBackgroundRect!!.height() / 2f
        }

    private fun findBadgeCenter() {
        val rectWidth =
            if (mBadgeTextRect!!.height() > mBadgeTextRect!!.width()) mBadgeTextRect!!.height() else mBadgeTextRect!!.width()
        when (badgeGravity) {
            Gravity.START or Gravity.TOP -> {
                mBadgeCenter!!.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                mBadgeCenter!!.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect!!.height() / 2f
            }
            Gravity.START or Gravity.BOTTOM -> {
                mBadgeCenter!!.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                mBadgeCenter!!.y =
                    mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect!!.height() / 2f)
            }
            Gravity.END or Gravity.TOP -> {
                mBadgeCenter!!.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                mBadgeCenter!!.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect!!.height() / 2f
            }
            Gravity.END or Gravity.BOTTOM -> {
                mBadgeCenter!!.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                mBadgeCenter!!.y =
                    mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect!!.height() / 2f)
            }
            Gravity.CENTER -> {
                mBadgeCenter!!.x = mWidth / 2f
                mBadgeCenter!!.y = mHeight / 2f
            }
            Gravity.CENTER or Gravity.TOP -> {
                mBadgeCenter!!.x = mWidth / 2f
                mBadgeCenter!!.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect!!.height() / 2f
            }
            Gravity.CENTER or Gravity.BOTTOM -> {
                mBadgeCenter!!.x = mWidth / 2f
                mBadgeCenter!!.y =
                    mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect!!.height() / 2f)
            }
            Gravity.CENTER or Gravity.START -> {
                mBadgeCenter!!.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                mBadgeCenter!!.y = mHeight / 2f
            }
            Gravity.CENTER or Gravity.END -> {
                mBadgeCenter!!.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                mBadgeCenter!!.y = mHeight / 2f
            }
        }
        initRowBadgeCenter()
    }

    private fun measureText() {
        mBadgeTextRect!!.left = 0f
        mBadgeTextRect!!.top = 0f
        if (TextUtils.isEmpty(badgeText)) {
            mBadgeTextRect!!.right = 0f
            mBadgeTextRect!!.bottom = 0f
        } else {
            mBadgeTextPaint!!.textSize = mBadgeTextSize
            mBadgeTextRect!!.right = mBadgeTextPaint!!.measureText(badgeText)
            mBadgeTextFontMetrics = mBadgeTextPaint!!.fontMetrics
            mBadgeTextRect!!.bottom =
                mBadgeTextFontMetrics!!.descent - mBadgeTextFontMetrics!!.ascent
        }
        createClipLayer()
    }

    private fun initRowBadgeCenter() {
        val screenPoint = IntArray(2)
        getLocationOnScreen(screenPoint)
        mRowBadgeCenter!!.x = mBadgeCenter!!.x + screenPoint[0]
        mRowBadgeCenter!!.y = mBadgeCenter!!.y + screenPoint[1]
    }

    protected fun animateHide(center: PointF?) {
        if (badgeText == null) {
            return
        }
        if (mAnimator == null || !mAnimator!!.isRunning) {
            screenFromWindow(true)
            mAnimator = BadgeAnimator(createBadgeBitmap(), center!!, this)
            mAnimator!!.start()
            setBadgeNumber(0)
        }
    }

    fun reset() {
        mDragCenter!!.x = -1000f
        mDragCenter!!.y = -1000f
        mDragQuadrant = 4
        screenFromWindow(false)
        parent.requestDisallowInterceptTouchEvent(false)
        invalidate()
    }

    override fun hide(animate: Boolean) {
        if (animate && mActivityRoot != null) {
            initRowBadgeCenter()
            animateHide(mRowBadgeCenter)
        } else {
            setBadgeNumber(0)
        }
    }

    /**
     * @param badgeNumber equal to zero badge will be hidden, less than zero show dot
     */
    override fun setBadgeNumber(badgeNumber: Int): Badge? {
        this.badgeNumber = badgeNumber
        if (this.badgeNumber < 0) {
            badgeText = ""
        } else if (this.badgeNumber > 99) {
            badgeText = if (isExactMode) badgeNumber.toString() else "99+"
        } else if (this.badgeNumber > 0 && this.badgeNumber <= 99) {
            badgeText = badgeNumber.toString()
        } else if (this.badgeNumber == 0) {
            badgeText = null
        }
        measureText()
        invalidate()
        return this
    }

    override fun setBadgeText(badgeText: String?): Badge? {
        this.badgeText = badgeText
        badgeNumber = 1
        measureText()
        invalidate()
        return this
    }

    override fun setExactMode(isExact: Boolean): Badge? {
        isExactMode = isExact
        if (badgeNumber > 99) {
            setBadgeNumber(badgeNumber)
        }
        return this
    }

    override fun setShowShadow(showShadow: Boolean): Badge? {
        isShowShadow = showShadow
        invalidate()
        return this
    }

    override fun setBadgeBackgroundColor(color: Int): Badge? {
        badgeBackgroundColor = color
        if (badgeBackgroundColor == Color.TRANSPARENT) {
            mBadgeTextPaint!!.xfermode = null
        } else {
            mBadgeTextPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
        invalidate()
        return this
    }

    override fun stroke(color: Int, width: Float, isDpValue: Boolean): Badge? {
        mColorBackgroundBorder = color
        mBackgroundBorderWidth = if (isDpValue) dp2px(context, width).toFloat() else width
        invalidate()
        return this
    }

    override fun setBadgeBackground(drawable: Drawable?): Badge? {
        return setBadgeBackground(drawable, false)
    }

    override fun setBadgeBackground(drawable: Drawable?, clip: Boolean): Badge? {
        mDrawableBackgroundClip = clip
        badgeBackground = drawable
        createClipLayer()
        invalidate()
        return this
    }

    override fun setBadgeTextColor(color: Int): Badge? {
        badgeTextColor = color
        invalidate()
        return this
    }

    override fun setBadgeTextSize(size: Float, isSpValue: Boolean): Badge? {
        mBadgeTextSize = if (isSpValue) dp2px(context, size).toFloat() else size
        measureText()
        invalidate()
        return this
    }

    override fun getBadgeTextSize(isSpValue: Boolean): Float {
        return if (isSpValue) px2dp(context, mBadgeTextSize).toFloat() else mBadgeTextSize
    }

    override fun setBadgePadding(padding: Float, isDpValue: Boolean): Badge? {
        mBadgePadding = if (isDpValue) dp2px(context, padding).toFloat() else padding
        createClipLayer()
        invalidate()
        return this
    }

    override fun getBadgePadding(isDpValue: Boolean): Float {
        return if (isDpValue) px2dp(context, mBadgePadding).toFloat() else mBadgePadding
    }

    /**
     * @param gravity only support Gravity.START | Gravity.TOP , Gravity.END | Gravity.TOP ,
     * Gravity.START | Gravity.BOTTOM , Gravity.END | Gravity.BOTTOM ,
     * Gravity.CENTER , Gravity.CENTER | Gravity.TOP , Gravity.CENTER | Gravity.BOTTOM ,
     * Gravity.CENTER | Gravity.START , Gravity.CENTER | Gravity.END
     */
    override fun setBadgeGravity(gravity: Int): Badge? {
        if (gravity == Gravity.START or Gravity.TOP || gravity == Gravity.END or Gravity.TOP || gravity == Gravity.START or Gravity.BOTTOM || gravity == Gravity.END or Gravity.BOTTOM || gravity == Gravity.CENTER || gravity == Gravity.CENTER or Gravity.TOP || gravity == Gravity.CENTER or Gravity.BOTTOM || gravity == Gravity.CENTER or Gravity.START || gravity == Gravity.CENTER or Gravity.END) {
            badgeGravity = gravity
            invalidate()
        } else {
            throw IllegalStateException(
                "only support Gravity.START | Gravity.TOP , Gravity.END | Gravity.TOP , " +
                        "Gravity.START | Gravity.BOTTOM , Gravity.END | Gravity.BOTTOM , Gravity.CENTER" +
                        " , Gravity.CENTER | Gravity.TOP , Gravity.CENTER | Gravity.BOTTOM ," +
                        "Gravity.CENTER | Gravity.START , Gravity.CENTER | Gravity.END"
            )
        }
        return this
    }

    override fun setGravityOffset(offset: Float, isDpValue: Boolean): Badge? {
        return setGravityOffset(offset, offset, isDpValue)
    }

    override fun setGravityOffset(offsetX: Float, offsetY: Float, isDpValue: Boolean): Badge? {
        mGravityOffsetX = if (isDpValue) dp2px(context, offsetX).toFloat() else offsetX
        mGravityOffsetY = if (isDpValue) dp2px(context, offsetY).toFloat() else offsetY
        invalidate()
        return this
    }

    override fun getGravityOffsetX(isDpValue: Boolean): Float {
        return if (isDpValue) px2dp(context, mGravityOffsetX).toFloat() else mGravityOffsetX
    }

    override fun getGravityOffsetY(isDpValue: Boolean): Float {
        return if (isDpValue) px2dp(context, mGravityOffsetY).toFloat() else mGravityOffsetY
    }

    private fun updataListener(state: Int) {
        if (mDragStateChangedListener != null) mDragStateChangedListener!!.onDragStateChanged(
            state,
            this,
            targetView
        )
    }

    override fun setOnDragStateChangedListener(l: OnDragStateChangedListener?): Badge? {
        isDraggable = l != null
        mDragStateChangedListener = l
        return this
    }

    override val dragCenter: PointF?
        get() = if (isDraggable && mDragging) mDragCenter else null

    private inner class BadgeContainer(context: Context?) : ViewGroup(context) {
        override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
            if (parent !is RelativeLayout) {
                super.dispatchRestoreInstanceState(container)
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.layout(0, 0, child.measuredWidth, child.measuredHeight)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var targetView: View? = null
            var badgeView: View? = null
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child !is QBadgeView) {
                    targetView = child
                } else {
                    badgeView = child
                }
            }
            if (targetView == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            } else {
                targetView.measure(widthMeasureSpec, heightMeasureSpec)
                badgeView?.measure(
                    MeasureSpec.makeMeasureSpec(targetView.measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(targetView.measuredHeight, MeasureSpec.EXACTLY)
                )
                setMeasuredDimension(targetView.measuredWidth, targetView.measuredHeight)
            }
        }
    }

    init {
        init()
    }
}