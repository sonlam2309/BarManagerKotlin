package com.app.buchin.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.buchin.fragment.DrinkDetailAddedFragment
import com.app.buchin.fragment.DrinkDetailUsedFragment
import com.app.buchin.model.Drink

class MyPagerAdapter(fragmentActivity: FragmentActivity,
                     private val mDrink: Drink?) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return if (position == 1) {
            DrinkDetailUsedFragment(mDrink)
        } else DrinkDetailAddedFragment(mDrink)
    }

    override fun getItemCount(): Int {
        return 2
    }
}