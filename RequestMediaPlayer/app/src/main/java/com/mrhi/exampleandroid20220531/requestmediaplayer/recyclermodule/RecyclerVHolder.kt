package com.mrhi.exampleandroid20220531.requestmediaplayer.recyclermodule

import android.os.Build.VERSION_CODES.P
import androidx.recyclerview.widget.RecyclerView
import com.mrhi.exampleandroid20220531.requestmediaplayer.R
import com.mrhi.exampleandroid20220531.requestmediaplayer.databinding.ItemRecyclerMediaBinding
import com.mrhi.exampleandroid20220531.requestmediaplayer.mediamodule.MediaPlay
import java.text.SimpleDateFormat

class RecyclerVHolder( val bind: ItemRecyclerMediaBinding):
    RecyclerView.ViewHolder( bind.root) {

    // init Recycler Item
    fun onBind( item: MediaPlay) {
        this.bind.ItemRecyclerMediaArtist.text          = item.artist
        this.bind.ItemRecyclerMediaDuration.text        = SimpleDateFormat("mm:ss").format( item.duration)
        this.bind.ItemRecyclerMediaTitle.text           = item.title

        // loading this viewHolder's state( ATTR_favorite)
        this.bind.ItemRecyclerMediaFavorite.isChecked   = item.isFavorite()

        val coverImg = item.getMediaCoverArt( this.bind.root.context, MediaPlay.MEDIA_COVER_ART_SIZE_96)

        // set CoverArt
        if( coverImg != null) { this.bind.ItemRecyclerMediaCoverArt.setImageBitmap( coverImg) }
        else { this.bind.ItemRecyclerMediaCoverArt.setImageResource( R.drawable.icon_audio_24) }

        // this.bind.ItemRecyclerMediaFavorite.isChecked =  item.favorite == 1L
    }
}