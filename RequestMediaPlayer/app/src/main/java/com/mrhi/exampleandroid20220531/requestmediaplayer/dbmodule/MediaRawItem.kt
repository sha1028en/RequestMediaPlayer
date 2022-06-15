package com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule

import java.io.Serializable

/*
                "$ATTR_ID TEXT primary key, " +
                "$ATTR_TITLE TEXT, " +
                "$ATTR_ARTIST TEXT, "  +
                "$ATTR_ALBUMID TEXT, " +
                "$ATTR_DURATION INTEGER )"
 */

/******************************************************

 *  result set =    MediaRawItem.
 *                      OR
 *                  mutableList< MediaRawItem>


 *      ATTR favorite: Int...
 *          - 1       = true
 *          - other   = false
 *      need  to call MediaPlay.isFavorite()



 *      when DB query after, result put this
 *      this use to DBHelper...like recodeSet
        before use MediaPlay

 *****************************************************/


data class MediaRawItem(
    val id: String,
    val title: String?,
    val artist: String?,
    val albumId: String?,
    val duration: Long?,
    val favorite: Long ) : Serializable