package dev.blank.badgeviewdemo

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.blank.badgeview.Badge
import dev.blank.badgeview.QBadgeView

class RecyclerViewActivity : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        recyclerView = findViewById(R.id.recyclerView) as RecyclerView?
        recyclerView!!.setLayoutManager(LinearLayoutManager(this))
        recyclerView!!.setAdapter(RecyclerAdapter())
    }

    internal inner class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.Holder?>() {
        private val data: List<String> = DataSupport().data
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(
                LayoutInflater.from(this@RecyclerViewActivity)
                    .inflate(R.layout.item_view, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.textView.setText(data[position])
            holder.badge.setBadgeNumber(position)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        internal inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: TextView
            var badge: Badge

            init {
                textView = itemView.findViewById<View>(R.id.tv_content) as TextView
                badge =
                    QBadgeView(this@RecyclerViewActivity).bindTarget(itemView.findViewById<View>(R.id.root))!!
                badge.setBadgeGravity(Gravity.CENTER or Gravity.END)
                badge.setBadgeTextSize(14f, true)
                badge.setBadgePadding(6f, true)
                badge.setOnDragStateChangedListener(object : Badge.OnDragStateChangedListener {

                    override fun onDragStateChanged(
                        dragState: Int,
                        badge: Badge?,
                        targetView: View?
                    ) {
                        if (dragState == Badge.OnDragStateChangedListener.STATE_SUCCEED) {
                            Toast.makeText(
                                this@RecyclerViewActivity,
                                java.lang.String.valueOf(getAdapterPosition()),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        }

    }
}