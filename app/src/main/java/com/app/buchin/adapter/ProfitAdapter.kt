package com.app.buchin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.R
import com.app.buchin.constant.Constants
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.Profit

class ProfitAdapter(private var mContext: Context?, private val mListProfit: MutableList<Profit>?,
                    private val iManagerProfitListener: IManagerProfitListener?) : RecyclerView.Adapter<ProfitAdapter.StatisticalViewHolder?>() {

    interface IManagerProfitListener {
        fun onClickItem(profit: Profit?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profit, parent, false)
        return StatisticalViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatisticalViewHolder, position: Int) {
        val profit = mListProfit?.get(position) ?: return
        holder.tvStt?.text = (position + 1).toString()
        holder.tvDrinkName?.text = profit.getDrinkName()
        val strCurrentQuantity = profit.getCurrentQuantity().toString() + " " + profit.getDrinkUnitName()
        holder.tvCurrentQuantity?.text = strCurrentQuantity
        val profitValue = profit.getProfit()
        val strProfit: String
        if (profitValue > 0) {
            holder.tvProfit?.setTextColor(mContext!!.getResources().getColor(R.color.green))
            strProfit = "+" + profitValue + Constants.CURRENCY
        } else if (profitValue == 0) {
            holder.tvProfit?.setTextColor(mContext!!.resources.getColor(R.color.yellow))
            strProfit = profitValue.toString() + Constants.CURRENCY
        } else {
            holder.tvProfit?.setTextColor(mContext!!.resources.getColor(R.color.background_red))
            strProfit = profitValue.toString() + Constants.CURRENCY
        }
        holder.tvProfit?.text = strProfit
        holder.layoutItem?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerProfitListener?.onClickItem(profit)
            }
        })
    }

    override fun getItemCount(): Int {
        return mListProfit?.size ?: 0
    }

    fun release() {
        mContext = null
    }

    class StatisticalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStt: TextView? = itemView.findViewById<TextView?>(R.id.tv_stt)
        val tvDrinkName: TextView? = itemView.findViewById<TextView?>(R.id.tv_drink_name)
        val tvCurrentQuantity: TextView? = itemView.findViewById<TextView?>(R.id.tv_current_quantity)
        val tvProfit: TextView? = itemView.findViewById<TextView?>(R.id.tv_profit)
        val layoutItem: LinearLayout? = itemView.findViewById<LinearLayout?>(R.id.layout_item)

    }
}