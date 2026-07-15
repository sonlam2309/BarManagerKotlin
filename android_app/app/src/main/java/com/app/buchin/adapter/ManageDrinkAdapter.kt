package com.app.buchin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.R
import com.app.buchin.adapter.ManageDrinkAdapter.ManageDrinkViewHolder
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.Drink

class ManageDrinkAdapter(private val mListDrink: MutableList<Drink>?,
                         private val iManagerDrinkListener: IManagerDrinkListener?) : RecyclerView.Adapter<ManageDrinkViewHolder?>() {
    interface IManagerDrinkListener {
        fun clickItem(drink: Drink?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageDrinkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_drink, parent, false)
        return ManageDrinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageDrinkViewHolder, position: Int) {
        val drink = mListDrink?.get(position)
        holder.tvName?.text = drink?.getName()
        val strCurrentQuantity = drink?.getQuantity().toString() + " " + drink?.getUnitName()
        holder.tvCurrentQuantity?.text = strCurrentQuantity

        // Listener
        holder.layoutItem?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerDrinkListener?.clickItem(drink)
            }
        })
    }

    override fun getItemCount(): Int {
        return mListDrink?.size ?: 0
    }

    class ManageDrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView? = itemView.findViewById<TextView?>(R.id.tv_name)
        val tvCurrentQuantity: TextView? = itemView.findViewById<TextView?>(R.id.tv_current_quantity)
        val layoutItem: RelativeLayout? = itemView.findViewById<RelativeLayout?>(R.id.layout_item)

    }
}