package com.mrhi.exampleandroid20220531.requestmediaplayer.viewpagermodule

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mrhi.exampleandroid20220531.requestmediaplayer.MediaViewPageFragment

class ViewPagerAdapter( activity: FragmentActivity): FragmentStateAdapter( activity) {
    private val items by lazy { mutableListOf< MediaViewPageFragment>() }

    override fun getItemCount():            Int         = items.size
    override fun createFragment( pos: Int): Fragment    = items[ pos]


    fun addItem( item: MediaViewPageFragment) {
        this.items.add( item)
        this.notifyDataSetChanged()
    }
}