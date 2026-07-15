package com.app.buchin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.R
import com.app.buchin.adapter.FeatureAdapter.FeatureViewHolder
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.Feature

class FeatureAdapter(private val mListFeature: MutableList<Feature>?,
                     private val iManagerFeatureListener: IManagerFeatureListener?) : RecyclerView.Adapter<FeatureViewHolder?>() {
    interface IManagerFeatureListener {
        fun clickFeatureItem(feature: Feature?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feature, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        val feature = mListFeature?.get(position) ?: return
        holder.imgFeature?.setImageResource(feature.getResource())
        holder.tvFeature?.text = feature.getTitle()

        // Listener
        holder.layoutItem?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                iManagerFeatureListener?.clickFeatureItem(feature)
            }
        })
    }

    override fun getItemCount(): Int {
        return mListFeature?.size ?: 0
    }

    class FeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutItem: LinearLayout? = itemView.findViewById<LinearLayout?>(R.id.layout_item)
        val imgFeature: ImageView? = itemView.findViewById<ImageView?>(R.id.img_feature)
        val tvFeature: TextView? = itemView.findViewById<TextView?>(R.id.tv_feature)

    }
}