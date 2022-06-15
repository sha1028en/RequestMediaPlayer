package com.mrhi.exampleandroid20220531.requestmediaplayer.recyclermodule

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.mrhi.exampleandroid20220531.requestmediaplayer.MainActivity
import com.mrhi.exampleandroid20220531.requestmediaplayer.MediaEnhancedActivity
import com.mrhi.exampleandroid20220531.requestmediaplayer.databinding.ItemRecyclerMediaBinding
import com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule.DBOpenHelper
import com.mrhi.exampleandroid20220531.requestmediaplayer.mediamodule.MediaPlay

/*************************
 *
 *  2022/06/15
 *  Optimizing Logic
 *
 ************************/


class RecyclerAdapter( private val context: Context ,var items: MutableList< MediaPlay>):
    RecyclerView.Adapter< RecyclerVHolder>() {
    private val dbHelper by lazy { DBOpenHelper( this.context) }

    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): RecyclerVHolder {
        val bind = ItemRecyclerMediaBinding.inflate( LayoutInflater.from( parent.context), parent, false)
        val viewHolder = RecyclerVHolder( bind)

        // when Favorite Checked!   update DB
        bind.ItemRecyclerMediaFavorite.setOnCheckedChangeListener { compoundButton, b ->
            val isFavorite = if( b) { 1L} else { 523L}

            // save this viewHolder's state( ATTR_favorite)
            if( viewHolder.bind.ItemRecyclerMediaFavorite.isChecked )   { this.items[ viewHolder.adapterPosition].favorite = 1L }
            else                                                        { this.items[ viewHolder.adapterPosition].favorite = 523L }

            // UPDATE DB ATTR_favorite
            this.dbHelper.updateFavoriteById( isFavorite, this.items[ viewHolder.adapterPosition].id )
        }

        // when Item Clicked... goto MediaEnhancedActivity with Items
        bind.root.setOnClickListener {
            val intent = Intent( bind.root.context, MediaEnhancedActivity::class.java )
            val items  = this@RecyclerAdapter.items as ArrayList<Parcelable>

            intent.putExtra( "music", items)
            intent.putExtra( "pos", viewHolder.adapterPosition)

            bind.root.context.startActivity( intent)
        }

        // when Item Long Clicked.... remove this item
        bind.root.setOnLongClickListener {
            if( dbHelper.deleteItemById( items[ viewHolder.adapterPosition].id)) {
                this.removeItem( viewHolder.adapterPosition)
            }
            true
        }

        return viewHolder
    }


    override fun onBindViewHolder(holder: RecyclerVHolder, pos: Int) { holder.onBind( this.items[ pos]) }
    override fun getItemCount():            Int = this.items.size
    override fun getItemViewType(pos: Int): Int = pos


    private fun removeItem( pos: Int){
        this.items.removeAt( pos)
        notifyItemRemoved( pos)
    }
}