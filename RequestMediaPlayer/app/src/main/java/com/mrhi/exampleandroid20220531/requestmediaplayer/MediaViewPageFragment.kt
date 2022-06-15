package com.mrhi.exampleandroid20220531.requestmediaplayer

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mrhi.exampleandroid20220531.requestmediaplayer.databinding.FragmentMediaViewPageBinding
import com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule.MediaRawItem
import com.mrhi.exampleandroid20220531.requestmediaplayer.mediamodule.MediaPlay


class MediaViewPageFragment:  Fragment() {
    private var bind: FragmentMediaViewPageBinding? = null
    private lateinit var media: MediaPlay


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this?.bind = FragmentMediaViewPageBinding.inflate( inflater, container, false)
        this.media = this.arguments?.getSerializable("media") as MediaPlay      // get this viewPage's item

        this.initView()
        return this.bind!!.root
    }

    // release Resource
    override fun onDestroyView() {
        super.onDestroyView()
        this.bind = null
    }

    // set Cover Art / title / artist
    private fun initView() {
        val coverArt: Bitmap? = media.getMediaCoverArt( this.bind!!.root.context, MediaPlay.MEDIA_COVER_ART_SIZE_96)

        // if this item have not coverArt? SET default Cover Art
        if( coverArt != null ) { this.bind?.FragmentMediaViewPagerCoverArt?.setImageBitmap( coverArt) }
        else { this.bind?.FragmentMediaViewPagerCoverArt?.setImageResource( R.drawable.icon_audio_24) }

        this.bind?.FragmentMediaViewPagerTitle?.text    = this.media.title
        this.bind?.FragmentMediaViewPagerArtist?.text   = this.media.artist
    }
}