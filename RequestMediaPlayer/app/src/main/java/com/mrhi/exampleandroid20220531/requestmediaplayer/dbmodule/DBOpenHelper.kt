package com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule


import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mrhi.exampleandroid20220531.requestmediaplayer.utill.Logging
import kotlin.NullPointerException

class DBOpenHelper( context: Context)
    : SQLiteOpenHelper( context, DB_NAME, null, DB_VER ) {

    private companion object {
        const val DB_NAME   = "ExampleDB.db"
        const val DB_VER    = 3
        const val DB_TABLE  = "mediaTable"

        const val ATTR_ID       = "id"
        const val ATTR_TITLE    = "title"
        const val ATTR_ARTIST   = "artist"
        const val ATTR_ALBUMID  = "albumId"
        const val ATTR_DURATION = "duration"
        const val ATTR_FAVORITE = "favorite"
    }

    override fun onCreate( db: SQLiteDatabase?) {
        val SYNTAX_CREATE_TABLE = "create table $DB_TABLE( " +
                "$ATTR_ID TEXT primary key, " +
                "$ATTR_TITLE TEXT, " +
                "$ATTR_ARTIST TEXT, "  +
                "$ATTR_ALBUMID TEXT, " +
                "$ATTR_DURATION INTEGER, " +
                "$ATTR_FAVORITE INTEGER )"       // 1 = TRUE, OTHER = FALSE
        try {
            db?.execSQL(SYNTAX_CREATE_TABLE)
        } catch ( e: SQLException ) { // Logging.e("DB.create SQL Exception")
        } catch ( e: Exception )    { } // Logging.e("DB.create something happen...?") }
    }

    override fun onUpgrade( db: SQLiteDatabase?, oldVer: Int, newVer: Int) {
        val SYNTAX_DROP_TABLE = "drop table $DB_TABLE"
        db?.execSQL( SYNTAX_DROP_TABLE)
        this.onCreate( db)
    }

    // INSERT INTO ~~~ VALUES( ~~~)
    fun insertItem( item: MediaRawItem) {
        val SYNTAX_INSERT = "insert into $DB_TABLE( " +
                "$ATTR_ID, " +
                "$ATTR_TITLE, " +
                "$ATTR_ARTIST, " +
                "$ATTR_ALBUMID, " +
                "$ATTR_DURATION, " +
                "$ATTR_FAVORITE) " +

                "values( " +
                "'${item.id}', " +
                "'${item.title}', " +
                "'${item.artist}', " +
                "'${item.albumId}', " +
                "${item.duration}, " +
                "${item.favorite} )"
        val db = this.writableDatabase
        try {
            db.execSQL(SYNTAX_INSERT)
        } catch ( e: SQLException)  { // Logging.e("DB.insert SQL Exception  \n$e")
        } catch ( e: Exception)     { // Logging.e("DB.insert something happen...?")
        } finally                   { db.close() }
    }

    // delete from DB where ATTR_id
    fun deleteItemById( id: String): Boolean{
        var isDelete = false
        val SYNTAX_DELETE = "delete from $DB_TABLE "+
                "where $ATTR_ID = '${ id}'"

        val db = this.writableDatabase
        try{
            db.execSQL( SYNTAX_DELETE)
            isDelete = this.selectItemById( id) == null

        } catch ( e: SQLException)          {   // Logging.e("delete err $e" )
        } catch (e : NullPointerException)  {
        } catch ( e: Exception)             {
        } finally                           { db.close() }


        return isDelete
    }

    // return ALL Items
    fun selectAllItems(): MutableList< MediaRawItem>? {
        val SYNTAX_SELECT_ALL    = "select * from $DB_TABLE"
        var items: MutableList<MediaRawItem>? = null
        val db = this.readableDatabase
        val cursor = db.rawQuery( SYNTAX_SELECT_ALL, null)

        try {
            // Logging.d("DB.selectAll cursor size :   ${ cursor.count}")
            if( cursor.count > 0) {
                items = mutableListOf() // init items
                while( cursor.moveToNext()) {   // has item?... put them
                    items?.add( MediaRawItem(
                        id      = cursor.getString( 0),
                        title   = cursor.getString( 1),
                        artist  = cursor.getString( 2),
                        albumId = cursor.getString( 3),
                        duration= cursor.getLong( 4),
                        favorite= cursor.getLong( 5)))
                }// endl while
            }// endl if()
        } catch ( e: SQLException)              { // Logging.e("DB.select SQL Exception \n$e")
        } catch ( e: IndexOutOfBoundsException) { // Logging.e("DB.select OutOfBounds Exception")
        } catch ( e: Exception)                 { // Logging.e("DB.select something happen...?")
        } finally                               {
            cursor.close()
            db.close()
        }

        return items
    }

    // select * where ATTR_artist or ATTR_title
    fun selectItemsBySearch(query: String): MutableList< MediaRawItem>?{
        val SYNTAX_SELECT_ITEM =  "select * from $DB_TABLE where "+
                "$ATTR_TITLE  like '%$query%' or "+
                "$ATTR_ARTIST like '%$query%'"
        var items: MutableList< MediaRawItem>? = null
        val db = this.readableDatabase
        val cursor = db.rawQuery(SYNTAX_SELECT_ITEM, null)
        try {

            if( cursor.count > 0) {
                items = mutableListOf()
                while (cursor.moveToNext()) {
                    items?.add(MediaRawItem(
                            id = cursor.getString(0),
                            title = cursor.getString(1),
                            artist = cursor.getString(2),
                            albumId = cursor.getString(3),
                            duration = cursor.getLong(4),
                            favorite = cursor.getLong(5)))
                }
            }
        } catch ( e: SQLException)              {   // Logging.e( "DBHelper.searchItem SQLException $e")
        } catch ( e: NoSuchElementException )   {
        } catch ( e: NullPointerException )     {
        } catch ( e: Exception)                 {
        } finally                               {
            cursor.close()
            db.close()
        }
        // Logging.d("search result set : ${ items?.get( 0)?.title}, ${ items?.get( 0)?.artist} ")
        return items
    }

    // search *  by Is it FAVORITE Item???
    fun selectItemsByFavorite(): MutableList< MediaRawItem>{
        val SYNTAX_SELECT_FAVOR = "select * from $DB_TABLE where + " +
                "$ATTR_FAVORITE = ${ 1L}"
        val items = mutableListOf< MediaRawItem>()
        val db = this.writableDatabase
        val cursor = db.rawQuery( SYNTAX_SELECT_FAVOR, null)
        try{
            while( cursor.moveToNext()){
                items.add( MediaRawItem(
                    id = cursor.getString( 0),
                    title = cursor.getString( 1),
                    artist = cursor.getString( 2),
                    albumId = cursor.getString( 3),
                    duration = cursor.getLong( 4),
                    favorite = cursor.getLong( 5)))
            }
        } catch ( e: SQLException)  {
        } catch ( e: Exception)     {
        } finally {
            cursor?.close()
            db.close()
        }

        return items
    }

    // select * from table where ATTR_id
    fun selectItemById( id: String): MediaRawItem? {
        val SYNTAX_SELECT_ITEM   = "select * from $DB_TABLE where " +
                "$ATTR_ID = '${ id}'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(SYNTAX_SELECT_ITEM, null)
        var item: MediaRawItem? = null

        try {
            if ( cursor.moveToFirst()) {
                item =  MediaRawItem(
                    id = cursor.getString(0),
                    title = cursor.getString(1),
                    artist = cursor.getString(2),
                    albumId = cursor.getString(3),
                    duration = cursor.getLong(4),
                    favorite = cursor.getLong( 5))
            }
        } catch ( e: SQLException)              { // Logging.e("DB.select SQL Exception")
        } catch ( e: IndexOutOfBoundsException) { // Logging.e("DB.select OutOfBound Exception")
        } catch ( e: NullPointerException )     { // Logging.e("DB.select NPE")
        } catch ( e: Exception)                 { // Logging.e("DB.select something happen...?")
        } finally {
            cursor.close()
            db.close()
        }

        return item
    }

    // Favorite Music item ATTR_favorite Update
    fun updateFavoriteById( favorite: Long, id: String) {
        val SYNTAX_UPDATE = "update $DB_TABLE set " +
                "$ATTR_FAVORITE = '${ favorite}' " +
                "where '${ id}' = $ATTR_ID"
        val db = this.writableDatabase
        try {
            db.execSQL( SYNTAX_UPDATE)

        } catch ( e: SQLException)  {   // Logging.d("DBHelper.insertFavorite SQL Exception $e")
        } catch ( e: Exception )    {   // Logging.d("DBHelper.insertFavorite something happen??")
        } finally                   { db.close() }
    }
}