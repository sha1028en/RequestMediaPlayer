package com.mrhi.exampleandroid20220531.requestmediaplayer.mediamodule

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule.MediaRawItem
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.io.FileNotFoundException
import java.io.Serializable
import java.util.*
import kotlin.NoSuchElementException


/**********************
*
*   2022/06/15
*   Parcelize Task
*
***********************/


@Parcelize
class MediaPlay( val item: MediaRawItem):
    Serializable, Comparable< MediaPlay>, Parcelable{
    var id                  = ""
    var title: String?      = null
    var artist: String?     = null
    var albumId: String?    = null
    var duration: Long?     = null
    var favorite: Long      = 523L


    companion object : Parceler< MediaPlay> {
        const val MEDIA_COVER_ART_SIZE_150  = 150
        const val MEDIA_COVER_ART_SIZE_96   = 96
        const val MODE_READ                 = "r"

        override fun create( parcel: Parcel): MediaPlay { return MediaPlay( parcel) }

        override fun MediaPlay.write(parcel: Parcel, flags: Int) {
            parcel.writeString( this.id)
            parcel.writeString( this.title)
            parcel.writeString( this.artist)
            parcel.writeString( this.albumId)
            parcel.writeLong( this.duration ?: 0L)
            parcel.writeLong( this.favorite ?: 523L)
        }
    }

    constructor( parcel: Parcel): this (
        MediaRawItem(
            id          = parcel.readString().toString(),
            title       = parcel.readString().toString(),
            artist      = parcel.readString().toString(),
            albumId     = parcel.readString().toString(),
            duration    = parcel.readLong(),
            favorite    = parcel.readLong()))

    // set this media Information
    init {
        this.id         = item.id
        this.title      = item.title
        this.artist     = item.artist
        this.albumId    = item.albumId
        this.duration   = item.duration
        this.favorite   = item.favorite
    }

    private fun getMediaPath(): Uri = Uri.parse("content://media/external/audio/albumart/" + this.albumId)
    fun getMediaInfo(): Uri = Uri.withAppendedPath( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, this.id)
    fun getMediaCoverArt( context: Context, coverSize: Int ): Bitmap? {
        val contentResolver: ContentResolver = context.contentResolver
        val uri = this.getMediaPath()
        val coverArtOptions = BitmapFactory.Options()
        var coverArt: Bitmap? = null

        // is not file available ??
        if( uri == null ) { return null }

        var fileDescriptor: ParcelFileDescriptor? = null
        try {
            fileDescriptor = contentResolver.openFileDescriptor( uri, MODE_READ)

            coverArt = BitmapFactory.decodeFileDescriptor(
                fileDescriptor!!.fileDescriptor,
                null,
                coverArtOptions )

            if( coverArt != null ) {
                if( coverArtOptions.outWidth != coverSize ||
                    coverArtOptions.outWidth != coverSize ) {
                    val tmpCoverArt = Bitmap.createScaledBitmap( coverArt, coverSize, coverSize, true)
                    coverArt.recycle()
                    coverArt = tmpCoverArt
                }
            } else { return null }
        } catch ( e: FileNotFoundException) {
        } catch ( e: Exception )            {
        } finally                           { fileDescriptor?.close() }

        return coverArt
    }

    // is this item( Music) Favorite???
    fun isFavorite(): Boolean = this.favorite == 1L

    // so where my Position?
    fun whereMyPos(items: MutableList< MediaPlay>): Int {
        var pos = 0
        val iterator = items.iterator()
        try {
            while (iterator.hasNext()) {
                if (this == iterator.next()) { return pos }
                pos++
            }
        } catch ( e: NoSuchElementException )   {
        } catch ( e: Exception)                 { }
        return -1
    }

    // return my items
    override fun toString(): String = "$id $title $artist $albumId $duration $favorite"

    // compare...? ATTR_id
    override fun equals(other: Any?): Boolean {
        return try {
            val tmp = other as MediaPlay
            this.hashCode() == tmp.hashCode()
        } catch (e: IllegalArgumentException ) { false }
    }

    // compare....? ATTR_id
    override fun compareTo(other: MediaPlay): Int = this.hashCode() - other.hashCode()
    override fun hashCode(): Int =  Objects.hashCode( this.id)
}