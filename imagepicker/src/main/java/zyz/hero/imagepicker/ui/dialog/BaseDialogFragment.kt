package zyz.hero.imagepicker.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import zyz.hero.imagepicker.R

/**
 * Created by hupei on 2017/3/29.
 */

abstract class BaseDialogFragment : DialogFragment() {
    var systemBarConfig: SystemBarConfig? = null
        private set
    private var mGravity = Gravity.CENTER //对话框的位置
    private var mCanceledOnTouchOutside = true //是否触摸外部关闭
    private var mCanceledBack = true //是否返回键关闭
    private var mWidth = 0.9f //对话框宽度，范围：0-1；1整屏宽
    private var mMaxHeight: Float = 0.toFloat() //对话框高度，范围：0-1；1整屏高
    private var mPadding: IntArray? = null //对话框与屏幕边缘距离
    private var mAnimStyle: Int = R.style.zoom_in_out_animStyle //显示动画
    private var isDimEnabled = true
    private var mBackgroundColor = Color.TRANSPARENT //对话框的背景色
    private var mRadius = 0 //对话框的圆角半径
    private var mAlpha = 1f //对话框透明度，范围：0-1；1不透明
    private var mX: Int = 0
    private var mY: Int = 0
    private var mSystemUiVisibility: Int = 0

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        systemBarConfig = SystemBarConfig(requireActivity())
        //设置 无标题 无边框
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        if (savedInstanceState != null) {
            mGravity = savedInstanceState.getInt(SAVED_GRAVITY)
            mCanceledOnTouchOutside = savedInstanceState.getBoolean(SAVED_TOUCH_OUT)
            mCanceledBack = savedInstanceState.getBoolean(SAVED_CANCELED_BACK)
            mWidth = savedInstanceState.getFloat(SAVED_WIDTH)
            mMaxHeight = savedInstanceState.getFloat(SAVED_HEIGHT_MAX)
            mPadding = savedInstanceState.getIntArray(SAVED_PADDING)
            mAnimStyle = savedInstanceState.getInt(SAVED_ANIM_STYLE)
            isDimEnabled = savedInstanceState.getBoolean(SAVED_DIM_ENABLED)
            mBackgroundColor = savedInstanceState.getInt(SAVED_BACKGROUND_COLOR)
            mRadius = savedInstanceState.getInt(SAVED_RADIUS)
            mAlpha = savedInstanceState.getFloat(SAVED_ALPHA)
            mX = savedInstanceState.getInt(SAVED_X)
            mY = savedInstanceState.getInt(SAVED_Y)
        }
    }

    override fun onStart() {
        if (view != null && mMaxHeight > 0) {
            view?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val height = view?.height
                    val screenHeight = systemBarConfig?.screenHeight
                    val maxHeight = ((screenHeight ?: 0) * mMaxHeight).toInt()
                    if (height ?: 0 > maxHeight) {
                        view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        view?.layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, maxHeight)
                    }
                }
            })
        }
        val dialog = dialog
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside)
            dialog.setCancelable(mCanceledBack)
            setDialogGravity(dialog) //设置对话框布局

            if (mSystemUiVisibility != 0) {
                dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            }
            if (mCanceledBack) {
                dialog.setOnKeyListener { _, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismissAllowingStateLoss()
                    }
                    false
                }
            }
        }
        super.onStart()
        if (dialog != null && mSystemUiVisibility != 0) {
            dialog.window!!.decorView.systemUiVisibility = mSystemUiVisibility
            dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_GRAVITY, mGravity)
        outState.putBoolean(SAVED_TOUCH_OUT, mCanceledOnTouchOutside)
        outState.putBoolean(SAVED_CANCELED_BACK, mCanceledBack)
        outState.putFloat(SAVED_WIDTH, mWidth)
        outState.putFloat(SAVED_HEIGHT_MAX, mMaxHeight)
        if (mPadding != null) outState.putIntArray(SAVED_PADDING, mPadding)
        outState.putInt(SAVED_ANIM_STYLE, mAnimStyle)
        outState.putBoolean(SAVED_DIM_ENABLED, isDimEnabled)
        outState.putInt(SAVED_BACKGROUND_COLOR, mBackgroundColor)
        outState.putInt(SAVED_RADIUS, mRadius)
        outState.putFloat(SAVED_ALPHA, mAlpha)
        outState.putInt(SAVED_X, mX)
        outState.putInt(SAVED_Y, mY)
    }

    class CircleDrawable(
        backgroundColor: Int,
        leftTopRadius: Int,
        rightTopRadius: Int,
        rightBottomRadius: Int,
        leftBottomRadius: Int,
    ) : ShapeDrawable() {

        constructor(backgroundColor: Int, radius: Int) : this(backgroundColor,
            radius,
            radius,
            radius,
            radius)

        init {
            paint.color = backgroundColor //内部填充颜色
            //圆角半径
            shape = getRoundRectShape(leftTopRadius,
                rightTopRadius,
                rightBottomRadius,
                leftBottomRadius)
        }

        private fun getRoundRectShape(
            leftTop: Int,
            rightTop: Int,
            rightBottom: Int,
            leftBottom: Int,
        ): RoundRectShape {
            val outerRadii = FloatArray(8)
            if (leftTop > 0) {
                outerRadii[0] = leftTop.toFloat()
                outerRadii[1] = leftTop.toFloat()
            }
            if (rightTop > 0) {
                outerRadii[2] = rightTop.toFloat()
                outerRadii[3] = rightTop.toFloat()
            }
            if (rightBottom > 0) {
                outerRadii[4] = rightBottom.toFloat()
                outerRadii[5] = rightBottom.toFloat()
            }
            if (leftBottom > 0) {
                outerRadii[6] = leftBottom.toFloat()
                outerRadii[7] = leftBottom.toFloat()
            }
            return RoundRectShape(outerRadii, null, null)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = createView(context, inflater, container)
        val circleDrawable =
            CircleDrawable(mBackgroundColor, dp2px(requireContext(), mRadius.toFloat()))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.background = circleDrawable
        } else {
            view.setBackgroundDrawable(circleDrawable)
        }

        view.alpha = mAlpha
        return view
    }

    abstract fun createView(
        context: Context?,
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): View

    /**
     * 设置对话框位置
     * [默认][Gravity.CENTER]
     *
     * @param gravity 位置
     */
    protected fun setGravity(gravity: Int) {
        mGravity = gravity
    }

    /**
     * 设置对话框点击外部关闭
     *
     * @param cancel true允许
     */
    protected fun setCanceledOnTouchOutside(cancel: Boolean) {
        mCanceledOnTouchOutside = cancel
    }

    /**
     * 设置对话框返回键关闭关闭
     *
     * @param cancel true允许
     */
    protected fun setCanceledBack(cancel: Boolean) {
        mCanceledBack = cancel
    }

    /**
     * 设置对话框宽度
     *
     * @param width 0.0 - 1.0
     */
    protected fun setWidth(@FloatRange(from = 0.0, to = 1.0) width: Float) {
        mWidth = width
    }

    /**
     * 设置对话框最大高度
     *
     * @param maxHeight 0f - 1f 之间
     */
    protected fun setMaxHeight(@FloatRange(from = 0.0, to = 1.0) maxHeight: Float) {
        mMaxHeight = maxHeight
    }

    /**
     * 设置边距
     *
     * @param left   px
     * @param top    px
     * @param right  px
     * @param bottom px
     */
    protected fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mPadding = intArrayOf(left, top, right, bottom)
    }

    /**
     * 弹出对话框的动画
     *
     * @param animStyle StyleRes
     */
    protected fun setAnimations(animStyle: Int) {
        mAnimStyle = animStyle
    }


    /**
     * 设置背景是否昏暗，默认true
     *
     * @param dimEnabled true昏暗
     */
    protected fun setDimEnabled(dimEnabled: Boolean) {
        isDimEnabled = dimEnabled
    }

    /**
     * 设置对话框背景色
     *
     * @param color 颜色值
     */
    protected fun setBackgroundColor(@ColorInt color: Int) {
        mBackgroundColor = color
    }

    /**
     * 设置对话框圆角
     *
     * @param radius 半径
     */
    protected fun setRadius(radius: Int) {
        mRadius = radius
    }

    /**
     * 设置对话框透明度
     *
     * @param alpha 0.0 - 1.0
     */
    protected fun setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        mAlpha = alpha
    }

    protected fun setX(x: Int) {
        mX = x
    }

    protected fun setY(y: Int) {
        mY = y
    }

    /**
     * 底部位置且充满宽
     */
    protected fun bottomFull() {
        this.mGravity = Gravity.BOTTOM
        this.mRadius = 0
        this.mWidth = 1f
        this.mY = 0
    }

    protected fun setSystemUiVisibility(systemUiVisibility: Int) {
        this.mSystemUiVisibility = systemUiVisibility
    }

    //显示键盘
    protected fun setSoftInputMode() {
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    /**
     * 对话框配置
     *
     * @param dialog
     */
    private fun setDialogGravity(dialog: Dialog) {
        val window = dialog.window
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val wlp = window.attributes
        val screenWidth = systemBarConfig!!.screenWidth
        if (mWidth > 0 && mWidth <= 1) {
            wlp.width = (screenWidth * mWidth).toInt() //宽度按屏幕大小的百分比设置
        } else {
            wlp.width = mWidth.toInt()
        }
        wlp.gravity = mGravity
        wlp.x = mX
        wlp.y = mY
        //边距
        if (mPadding != null) {
            val padding = mPadding
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT
            window.decorView.setPadding(padding!![0], padding[1], padding[2], padding[3])
        }
        window.attributes = wlp
        //动画
        if (mAnimStyle != 0) {
            window.setWindowAnimations(mAnimStyle)
        }

        //背景灰暗
        if (isDimEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    inner class SystemBarConfig(activity: Activity) {

        /**
         * 获取status bar状态栏高度
         *
         * @return 状态栏高度的像素值
         */
        val statusBarHeight: Int
        val screenWidth: Int
        val screenHeight: Int

        val screenSize: IntArray
            get() = intArrayOf(screenWidth, screenHeight)

        init {
            val res = activity.resources
            statusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME)

            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
        }

        //通过此方法获取资源对应的像素值
        private fun getInternalDimensionSize(res: Resources, key: String): Int {
            var result = 0
            val resourceId = res.getIdentifier(key, "dimen", "android")
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId)
            }
            return result
        }
    }

    var dismissListener: ((DialogInterface?) -> Unit)? = null
    override fun onDismiss(dialog: DialogInterface) {
        dismissListener?.invoke(dialog)
        super.onDismiss(dialog)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroyView() {
        try {
            dialog?.let {
                var clazz = it.javaClass
                clazz.declaredFields.filter { it.name == "mHandler" }.forEach { filed ->
                    filed.isAccessible = true
                    (filed.get(dialog) as? Handler)?.let { handler ->
                        //发送一个空的message
                        //                        handler.looper?.queue?.addIdleHandler {
                        //                            handler.obtainMessage().sendToTarget()
                        //                            true
                        //                        }
                        handler.sendMessage(Message())
                        handler.removeCallbacksAndMessages(null)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            super.onDestroyView()
        }


    }

    fun dp2px(context: Context, value: Float): Int {
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics) + 0.5f).toInt()
    }

    companion object {
        private val STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height"
        private val SAVED_GRAVITY = "circle:baseGravity"
        private val SAVED_TOUCH_OUT = "circle:baseTouchOut"
        private val SAVED_CANCELED_BACK = "circle:baseCanceledBack"
        private val SAVED_WIDTH = "circle:baseWidth"
        private val SAVED_HEIGHT_MAX = "circle:baseMaxHeight"
        private val SAVED_PADDING = "circle:basePadding"
        private val SAVED_ANIM_STYLE = "circle:baseAnimStyle"
        private val SAVED_DIM_ENABLED = "circle:baseDimEnabled"
        private val SAVED_BACKGROUND_COLOR = "circle:baseBackgroundColor"
        private val SAVED_RADIUS = "circle:baseRadius"
        private val SAVED_ALPHA = "circle:baseAlpha"
        private val SAVED_X = "circle:baseX"
        private val SAVED_Y = "circle:baseY"
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager?.beginTransaction()
            ft?.add(this, tag)
            ft?.commitNowAllowingStateLoss()
        } catch (ignored: IllegalStateException) {
        }
    }

    override fun dismiss() {
        try {
            dismissAllowingStateLoss()
        } catch (e: Exception) {
        }
    }
}
