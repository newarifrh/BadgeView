package dev.blank.badgeviewdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import dev.blank.badgeview.Badge
import dev.blank.badgeview.Badge.OnDragStateChangedListener.Companion.STATE_SUCCEED
import dev.blank.badgeview.QBadgeView

open class ListViewActivity : AppCompatActivity() {
    private var listview: ListView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view)
        listview = findViewById(R.id.listview)
        listview!!.adapter = ListAdapter()
    }

    internal inner class ListAdapter : BaseAdapter(), android.widget.ListAdapter {
        private val data: List<String> = DataSupport().data
        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(position: Int): Any {
            return data[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, view: View, parent: ViewGroup): View {
            var convertView: View? = view
            val holder: Holder
            if (convertView == null) {
                holder = Holder()
                convertView = LayoutInflater.from(this@ListViewActivity)
                    .inflate(R.layout.item_view, parent, false)
                holder.textView = convertView.findViewById<View>(R.id.tv_content) as TextView
                holder.badge =
                    QBadgeView(this@ListViewActivity).bindTarget(convertView.findViewById<View>(R.id.imageview))
                holder.badge?.setBadgeTextSize(12f, true)
                convertView.tag = holder
            } else {
                holder = convertView.tag as Holder
            }
            holder.textView?.text = data[position]
            holder.badge?.setBadgeNumber(position)
            holder.badge?.setOnDragStateChangedListener(object : Badge.OnDragStateChangedListener {
                override fun onDragStateChanged(dragState: Int, badge: Badge?, targetView: View?) {
                    if (dragState == STATE_SUCCEED) {
                        Toast.makeText(
                            this@ListViewActivity,
                            position.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            })
            return convertView!!
        }

        internal inner class Holder {
            var textView: TextView? = null
            var badge: Badge? = null
        }

    }
}