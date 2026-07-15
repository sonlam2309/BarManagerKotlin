package com.app.buchin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.R
import com.app.buchin.adapter.HistoryAdapter.HistoryViewHolder
import com.app.buchin.constant.Constants
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.History
import com.app.buchin.utils.DateTimeUtils

class HistoryAdapter(private val mListHistory: MutableList<History>?,
                     private val mIsShowDate: Boolean,
                     private val iManagerHistoryistener: IManagerHistoryListener?) : RecyclerView.Adapter<HistoryViewHolder?>() {

    interface IManagerHistoryListener {
        fun editHistory(history: History?)
        fun deleteHistory(history: History?)
        fun onClickItemHistory(history: History?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_buy_or_used, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = mListHistory?.get(position) ?: return
        if (history.isAdd()) {
            holder.layoutItemHistory?.setBackgroundResource(R.drawable.bg_white_corner_6_border_gray)
        } else {
            holder.layoutItemHistory?.setBackgroundResource(R.drawable.bg_gray_corner_radius_6)
        }
        if (mIsShowDate) {
            holder.tvDate?.visibility = View.VISIBLE
            holder.tvDate?.text = DateTimeUtils.convertTimeStampToDate(history.getDate().toString())
            holder.layoutItemHistory?.setOnClickListener(null)
        } else {
            holder.tvDate?.visibility = View.GONE
            holder.layoutItemHistory?.setOnClickListener(object : IOnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    iManagerHistoryistener?.onClickItemHistory(history)
                }
            })
        }
        holder.tvDrinkName?.text = history.getDrinkName()
        val strPrice = history.getPrice().toString() + Constants.CURRENCY
        holder.tvPrice?.text = strPrice
        val strQuantity = history.getQuantity().toString() + " " + history.getUnitName()
        holder.tvQuantity?.text = strQuantity
        val strTotalPrice = history.getTotalPrice().toString() + Constants.CURRENCY
        holder.tvTotalPrice?.text = strTotalPrice

        // Listener
        holder.imgEdit?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerHistoryistener?.editHistory(history)
            }
        })
        holder.imgDelete?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerHistoryistener?.deleteHistory(history)
            }
        })
    }

    override fun getItemCount(): Int {
        return mListHistory?.size ?: 0
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutItemHistory: LinearLayout? = itemView.findViewById<LinearLayout?>(R.id.layout_item_history)
        val tvDrinkName: TextView? = itemView.findViewById<TextView?>(R.id.tv_drink_name)
        val tvPrice: TextView? = itemView.findViewById<TextView?>(R.id.tv_price)
        val tvQuantity: TextView? = itemView.findViewById<TextView?>(R.id.tv_quantity)
        val tvTotalPrice: TextView? = itemView.findViewById<TextView?>(R.id.tv_total_price)
        val imgEdit: ImageView? = itemView.findViewById<ImageView?>(R.id.img_edit)
        val imgDelete: ImageView? = itemView.findViewById<ImageView?>(R.id.img_delete)
        val tvDate: TextView? = itemView.findViewById<TextView?>(R.id.tv_date)

    }
}