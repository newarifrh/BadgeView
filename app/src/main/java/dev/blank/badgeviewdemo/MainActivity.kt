package dev.blank.badgeviewdemo

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import dev.blank.badgeview.Badge
import dev.blank.badgeview.QBadgeView
import java.lang.Exception
import java.util.ArrayList

open class MainActivity : AppCompatActivity() {
    var textview: TextView? = null
    var tv_offsetx: TextView? = null
    var tv_padding: TextView? = null
    var tv_offsety: TextView? = null
    var tv_numbersize: TextView? = null
    var tv_dragstate: TextView? = null
    var et_badgenumber: EditText? = null
    var et_badgetext: EditText? = null
    var imageview: ImageView? = null
    var iv_badgecolor: ImageView? = null
    var iv_numbercolor: ImageView? = null
    var button: Button? = null
    var btn_animation: Button? = null
    var radioButtons: MutableList<RadioButton> = ArrayList<RadioButton>()
    var lastRadioButton: CompoundButton? = null
    var seekBar_offsetx: SeekBar? = null
    var seekBar_padding: SeekBar? = null
    var seekBar_offsety: SeekBar? = null
    var seekBar_numbersize: SeekBar? = null
    var swicth_exact: Switch? = null
    var swicth_draggable: Switch? = null
    var swicth_shadow: Switch? = null
    var badges: MutableList<Badge>? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initListener()
        initBadge()
        swicth_draggable!!.isChecked = true
    }

    private fun initBadge() {
        badges = ArrayList<Badge>()
        badges!!.add(QBadgeView(this).bindTarget(textview)!!.setBadgeNumber(5)!!)
        badges!!.add(
            QBadgeView(this).bindTarget(imageview)!!.setBadgeText("PNG")!!.setBadgeTextColor(0x00000000)!!
                .setBadgeGravity(Gravity.BOTTOM or Gravity.END)!!.setBadgeBackgroundColor(-0xfc560c)!!
                .setBadgeBackground(resources.getDrawable(R.drawable.shape_round_rect))!!
        )
        badges!!.add(
            QBadgeView(this).bindTarget(button)!!.setBadgeText("新")!!.setBadgeTextSize(13f, true)!!
                .setBadgeBackgroundColor(-0x14c5)!!.setBadgeTextColor(-0x1000000)!!
                .stroke(-0x1000000, 1f, true)!!
        )
    }

    private fun initView() {
        textview = findViewById(R.id.textview) as TextView?
        tv_offsetx = findViewById(R.id.tv_offsetx) as TextView?
        tv_offsety = findViewById(R.id.tv_offsety) as TextView?
        tv_padding = findViewById(R.id.tv_padding) as TextView?
        tv_numbersize = findViewById(R.id.tv_numbersize) as TextView?
        tv_dragstate = findViewById(R.id.tv_dragstate) as TextView?
        et_badgenumber = findViewById(R.id.et_badgenumber) as EditText?
        et_badgetext = findViewById(R.id.et_badgetext) as EditText?
        imageview = findViewById(R.id.imageview) as ImageView?
        iv_badgecolor = findViewById(R.id.iv_badgecolor) as ImageView?
        iv_numbercolor = findViewById(R.id.iv_numbercolor) as ImageView?
        iv_numbercolor = findViewById(R.id.iv_numbercolor) as ImageView?
        button = findViewById(R.id.button) as Button?
        btn_animation = findViewById(R.id.btn_animation) as Button?
        radioButtons.add(findViewById(R.id.rb_st) as RadioButton)
        radioButtons.add(findViewById(R.id.rb_sb) as RadioButton)
        val rb_et: RadioButton = findViewById(R.id.rb_et) as RadioButton
        lastRadioButton = rb_et
        radioButtons.add(rb_et)
        radioButtons.add(findViewById(R.id.rb_eb) as RadioButton)
        radioButtons.add(findViewById(R.id.rb_ct) as RadioButton)
        radioButtons.add(findViewById(R.id.rb_ce) as RadioButton)
        radioButtons.add(findViewById(R.id.rb_cb) as RadioButton)
        radioButtons.add(findViewById(R.id.rb_cs) as RadioButton)
        radioButtons.add(findViewById(R.id.rb_c) as RadioButton)
        seekBar_offsetx = findViewById(R.id.seekBar_offsetx) as SeekBar?
        seekBar_offsety = findViewById(R.id.seekBar_offsety) as SeekBar?
        seekBar_padding = findViewById(R.id.seekBar_padding) as SeekBar?
        seekBar_numbersize = findViewById(R.id.seekBar_numbersize) as SeekBar?
        swicth_exact = findViewById(R.id.swicth_exact) as Switch?
        swicth_draggable = findViewById(R.id.swicth_draggable) as Switch?
        swicth_shadow = findViewById(R.id.swicth_shadow) as Switch?
    }

    private fun initListener() {
        val checkedChangeListener: CompoundButton.OnCheckedChangeListener =
            object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                    if (!isChecked) {
                        return
                    }
                    if (lastRadioButton != null) {
                        lastRadioButton!!.setChecked(false)
                    }
                    lastRadioButton = buttonView
                    for (badge in badges!!) {
                        when (buttonView.getId()) {
                            R.id.rb_st -> badge.setBadgeGravity(Gravity.START or Gravity.TOP)
                            R.id.rb_sb -> badge.setBadgeGravity(Gravity.START or Gravity.BOTTOM)
                            R.id.rb_et -> badge.setBadgeGravity(Gravity.END or Gravity.TOP)
                            R.id.rb_eb -> badge.setBadgeGravity(Gravity.END or Gravity.BOTTOM)
                            R.id.rb_ct -> badge.setBadgeGravity(Gravity.CENTER or Gravity.TOP)
                            R.id.rb_ce -> badge.setBadgeGravity(Gravity.CENTER or Gravity.END)
                            R.id.rb_cb -> badge.setBadgeGravity(Gravity.CENTER or Gravity.BOTTOM)
                            R.id.rb_cs -> badge.setBadgeGravity(Gravity.CENTER or Gravity.START)
                            R.id.rb_c -> badge.setBadgeGravity(Gravity.CENTER)
                        }
                    }
                }
            }
        for (rb in radioButtons) {
            rb.setOnCheckedChangeListener(checkedChangeListener)
        }
        val onSeekBarChangeListener: SeekBar.OnSeekBarChangeListener = object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                for (badge in badges!!) {
                    if (seekBar === seekBar_offsetx || seekBar === seekBar_offsety) {
                        val x: Int = seekBar_offsetx!!.getProgress()
                        val y: Int = seekBar_offsety!!.getProgress()
                        tv_offsetx!!.setText("GravityOffsetX : $x")
                        tv_offsety!!.setText("GravityOffsetY : $y")
                        badge.setGravityOffset(x.toFloat(), y.toFloat(), true)
                    } else if (seekBar === seekBar_padding) {
                        tv_padding!!.setText("BadgePadding : $progress")
                        badge.setBadgePadding(progress.toFloat(), true)
                    } else if (seekBar === seekBar_numbersize) {
                        tv_numbersize!!.setText("TextSize : $progress")
                        badge.setBadgeTextSize(progress.toFloat(), true)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }
        seekBar_offsetx!!.setOnSeekBarChangeListener(onSeekBarChangeListener)
        seekBar_offsety!!.setOnSeekBarChangeListener(onSeekBarChangeListener)
        seekBar_padding!!.setOnSeekBarChangeListener(onSeekBarChangeListener)
        seekBar_numbersize!!.setOnSeekBarChangeListener(onSeekBarChangeListener)
        val onClickListener = View.OnClickListener { v ->
            if (v === iv_badgecolor) {
                selectorColor(object : OnColorClickListener {
                    override fun onColorClick(color: Int) {
                        iv_badgecolor!!.setBackgroundColor(color)
                        for (badge in badges!!) {
                            badge.setBadgeBackgroundColor(color)
                        }
                    }
                })
            } else if (v === iv_numbercolor) {
                selectorColor(object : OnColorClickListener {
                    override fun onColorClick(color: Int) {
                        iv_numbercolor!!.setBackgroundColor(color)
                        for (badge in badges!!) {
                            badge.setBadgeTextColor(color)
                        }
                    }
                })
            } else if (v === btn_animation) {
                for (badge in badges!!) {
                    badge.hide(true)
                }
            }
        }
        iv_badgecolor!!.setOnClickListener(onClickListener)
        iv_numbercolor!!.setOnClickListener(onClickListener)
        btn_animation!!.setOnClickListener(onClickListener)
        class MyTextWatcher(editText: EditText?) : TextWatcher {
            private val editText: EditText?
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    for (badge in badges!!) {
                        if (editText === et_badgenumber) {
                            val num = if (TextUtils.isEmpty(s)) 0 else s.toString().toInt()
                            badge.setBadgeNumber(num)
                        } else if (editText === et_badgetext) {
                            badge.setBadgeText(s.toString())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable) {}

            init {
                this.editText = editText
            }
        }
        et_badgenumber!!.addTextChangedListener(MyTextWatcher(et_badgenumber))
        et_badgetext!!.addTextChangedListener(MyTextWatcher(et_badgetext))
        val onCheckedChangeListener: CompoundButton.OnCheckedChangeListener =
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                for (badge in badges!!) {
                    if (buttonView === swicth_exact) {
                        badge.setExactMode(isChecked)
                    } else if (buttonView === swicth_draggable) {
                        badge.setOnDragStateChangedListener(if (isChecked) object :
                            Badge.OnDragStateChangedListener {
                            override fun onDragStateChanged(
                                dragState: Int,
                                badge: Badge?,
                                targetView: View?
                            ) {
                                when (dragState) {
                                    Badge.OnDragStateChangedListener.STATE_START -> tv_dragstate!!.setText(
                                        "STATE_START"
                                    )
                                    Badge.OnDragStateChangedListener.STATE_DRAGGING -> tv_dragstate!!.setText(
                                        "STATE_DRAGGING"
                                    )
                                    Badge.OnDragStateChangedListener.STATE_DRAGGING_OUT_OF_RANGE -> tv_dragstate!!.setText(
                                        "STATE_DRAGGING_OUT_OF_RANGE"
                                    )
                                    Badge.OnDragStateChangedListener.STATE_SUCCEED -> tv_dragstate!!.setText(
                                        "STATE_SUCCEED"
                                    )
                                    Badge.OnDragStateChangedListener.STATE_CANCELED -> tv_dragstate!!.setText(
                                        "STATE_CANCELED"
                                    )
                                }
                            }

                        } else null)
                    } else if (buttonView === swicth_shadow) {
                        badge.setShowShadow(isChecked)
                    }
                }
            }
        swicth_exact!!.setOnCheckedChangeListener(onCheckedChangeListener)
        swicth_draggable!!.setOnCheckedChangeListener(onCheckedChangeListener)
        swicth_shadow!!.setOnCheckedChangeListener(onCheckedChangeListener)
    }

    private fun selectorColor(l: OnColorClickListener) {
        val dialog: AlertDialog = AlertDialog.Builder(this).create()
        val gv = GridView(this)
        gv.setNumColumns(4)
        gv.setAdapter(object : BaseAdapter() {
            var colors = intArrayOf(
                Color.TRANSPARENT,
                -0x1,
                -0x1000000,
                -0x1ae3dd,
                -0x17b1c0,
                -0x63d850,
                -0x98c549,
                -0xc0ae4b,
                -0xa98804,
                -0xfc560c,
                -0xff432c,
                -0xff6978,
                -0xda64dc,
                -0x743cb6,
                -0x3223c7,
                -0x14c5,
                -0x3ef9,
                -0x6800,
                -0xa8de,
                -0x86aab8
            )

            override fun getCount(): Int {
                return colors.size
            }

            override fun getItem(position: Int): Any {
                return colors[position]
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                val v = View(this@MainActivity)
                v.setBackgroundColor(colors[position])
                v.setOnClickListener {
                    l.onColorClick(colors[position])
                    dialog.dismiss()
                }
                val dm = DisplayMetrics()
                val wm: WindowManager = this@MainActivity
                    .getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.getDefaultDisplay().getMetrics(dm)
                val lp: AbsListView.LayoutParams = AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    (dm.widthPixels / 5f).toInt()
                )
                v.layoutParams = lp
                return v
            }
        })
        dialog.setView(gv)
        dialog.show()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0x33FFFFFF))
    }

    internal interface OnColorClickListener {
        fun onColorClick(color: Int)
    }
}