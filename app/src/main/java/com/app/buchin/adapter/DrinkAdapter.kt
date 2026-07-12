package com.app.buchin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.R
import com.app.buchin.adapter.DrinkAdapter.DrinkViewHolder
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.Drink

class DrinkAdapter(private val mListDrink: MutableList<Drink>?,
                   private val iManagerDrinkListener: IManagerDrinkListener?) : RecyclerView.Adapter<DrinkViewHolder?>() {
    interface IManagerDrinkListener {
        fun editDrink(drink: Drink?)
        fun deleteDrink(drink: Drink?)
        fun onClickItemDrink(drink: Drink?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drink, parent, false)
        return DrinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
        val drink = mListDrink?.get(position)
        holder.tvName?.text = drink?.getName()
        holder.tvUnitName?.text = drink?.getUnitName()

        // Listener
        holder.imgEdit?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerDrinkListener?.editDrink(drink)
            }
        })
        holder.imgDelete?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerDrinkListener?.deleteDrink(drink)
            }
        })
        holder.layoutItem?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerDrinkListener?.onClickItemDrink(drink)
            }
        })
    }

    override fun getItemCount(): Int {
        return mListDrink?.size ?: 0
    }

    class DrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView? = itemView.findViewById<TextView?>(R.id.tv_name)
        val tvUnitName: TextView? = itemView.findViewById<TextView?>(R.id.tv_unit_name)
        val imgEdit: ImageView? = itemView.findViewById<ImageView?>(R.id.img_edit)
        val imgDelete: ImageView? = itemView.findViewById<ImageView?>(R.id.img_delete)
        val layoutItem: RelativeLayout? = itemView.findViewById<RelativeLayout?>(R.id.layout_item)

    }
}