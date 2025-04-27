package com.example.stockmanagement

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PrimaryRecordsFragment() // FIRST TAB NOW => Create Records
            1 -> ListRecordsFragment()     // SECOND TAB => List Records
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}