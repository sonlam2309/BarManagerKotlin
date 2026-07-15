package com.app.buchin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.R
import com.app.buchin.adapter.UnitAdapter.UnitViewHolder
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.UnitObject

class UnitAdapter(private val mListUnit: MutableList<UnitObject>?,
                  private val iManagerUnitListener: IManagerUnitListener?) : RecyclerView.Adapter<UnitViewHolder?>() {

    interface IManagerUnitListener {
        fun editUnit(unitObject: UnitObject?)
        fun deleteUnit(unitObject: UnitObject?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_unit, parent, false)
        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unitObject = mListUnit?.get(position) ?: return
        holder.tvName?.text = unitObject.getName()

        // Listener
        holder.imgEdit?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerUnitListener?.editUnit(unitObject)
            }
        })
        holder.imgDelete?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerUnitListener?.deleteUnit(unitObject)
            }
        })
    }

    override fun getItemCount(): Int {
        return mListUnit?.size ?: 0
    }

    class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView? = itemView.findViewById<TextView?>(R.id.tv_name)
        val imgEdit: ImageView? = itemView.findViewById<ImageView?>(R.id.img_edit)
        val imgDelete: ImageView? = itemView.findViewById<ImageView?>(R.id.img_delete)

    }
}