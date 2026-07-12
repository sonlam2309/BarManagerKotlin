package com.app.buchin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.R
import com.app.buchin.constant.Constants
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.Statistical

class StatisticalAdapter(private val mListStatisticals: MutableList<Statistical>?,
                         private val iManagerStatisticalListener: IManagerStatisticalListener?) : RecyclerView.Adapter<StatisticalAdapter.StatisticalViewHolder?>() {

    interface IManagerStatisticalListener {
        fun onClickItem(statistical: Statistical?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_statistical, parent, false)
        return StatisticalViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatisticalViewHolder, position: Int) {
        val statistical = mListStatisticals?.get(position) ?: return
        holder.tvStt?.text = (position + 1).toString()
        holder.tvDrinkName?.text = statistical.getDrinkName()
        val strQuantity = statistical.getQuantity().toString() + " " + statistical.getDrinkUnitName()
        holder.tvQuantity?.text = strQuantity
        val strTotalPrice = statistical.getTotalPrice().toString() + Constants.CURRENCY
        holder.tvTotalPrice?.text = strTotalPrice

        holder.layoutItem?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerStatisticalListener?.onClickItem(statistical)
            }
        })
    }

    override fun getItemCount(): Int {
        return mListStatisticals?.size ?: 0
    }

    class StatisticalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStt: TextView? = itemView.findViewById<TextView?>(R.id.tv_stt)
        val tvDrinkName: TextView? = itemView.findViewById<TextView?>(R.id.tv_drink_name)
        val tvQuantity: TextView? = itemView.findViewById<TextView?>(R.id.tv_quantity)
        val tvTotalPrice: TextView? = itemView.findViewById<TextView?>(R.id.tv_total_price)
        val layoutItem: LinearLayout? = itemView.findViewById<LinearLayout?>(R.id.layout_item)

    }
}