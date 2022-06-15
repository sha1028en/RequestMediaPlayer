package com.mrhi.exampleandroid20220531.requestmediaplayer

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrhi.exampleandroid20220531.requestmediaplayer.databinding.ActivityMainBinding
import com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule.DBOpenHelper
import com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule.MediaRawItem
import com.mrhi.exampleandroid20220531.requestmediaplayer.mediamodule.MediaPlay
import com.mrhi.exampleandroid20220531.requestmediaplayer.recyclermodule.RecyclerAdapter
import kotlin.NoSuchElementException


class MainActivity :
    AppCompatActivity(), androidx.appcompat.widget.SearchView.OnQueryTextListener {
    private val bind by lazy { ActivityMainBinding.inflate( layoutInflater) }
    private val dbHelper by lazy { DBOpenHelper( this) }
    private val permList by lazy { arrayOf( android.Manifest.permission.READ_EXTERNAL_STORAGE) }
    private lateinit var recyclerAdapter: RecyclerAdapter
    private var mediaItems = mutableListOf< MediaPlay>()
    private var isShowingFavorite = false

    private val PERM_REQUEST_CODE   = 1028

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( this.bind.root)

        if ( this.hasPerm()) { initDBTask() }
        else { ActivityCompat.requestPermissions( this, permList, this.PERM_REQUEST_CODE) }

        setSupportActionBar( this.bind.ActivityMainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled( true)
        supportActionBar?.setDisplayShowTitleEnabled( false)
    }

    // menu & toolBar init
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate( R.menu.menu_action, menu)

        val searchManager: SearchManager? = getSystemService( Context.SEARCH_SERVICE) as SearchManager
        ( menu?.findItem( R.id.menu_action_search)?.actionView as androidx.appcompat.widget.SearchView).apply {
            setSearchableInfo( searchManager?.getSearchableInfo( componentName))
            setOnQueryTextListener( this@MainActivity)
        }

        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean = true

    // search DB task Here!
    override fun onQueryTextChange(query: String?): Boolean {
        searchDBTask( query ?: "EMPTY")
        return true
    }


    // request to GRANT Perm
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when( requestCode){
            this.PERM_REQUEST_CODE -> {
                if( grantResults[ 0] == PackageManager.PERMISSION_GRANTED ) { this.initDBTask() }
                else { Toast.makeText( this, "need Perm", Toast.LENGTH_SHORT).show()}
            }
        }
    } //endl onRequestPermResult


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when( item.itemId) {
            // when, menu 즐겨찿기 clicked!
            R.id.menu_action_favorite -> {
                this.isShowingFavorite = true   // mode Favorite
                searchFavorDBTask()             // show only favorite items
            }
            // when, menu 전부 보기 clicked!
            R.id.menu_action_show_all -> {
                this.isShowingFavorite = false  // mode All
                initDBTask()                    // show All items
            }
        }

        return super.onOptionsItemSelected( item)
    }

    // when DB _EMPTY_ Request to Music data from ContentResolver
    private fun getMediaList(): MutableList < MediaRawItem> {
        val mediaListUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION)
        val items = mutableListOf< MediaRawItem>()
        val mediaCursor = contentResolver.query( mediaListUri, projection, null, null, null)
        try{
            while( mediaCursor!!.moveToNext()){

                // ' care
                var title = mediaCursor.getString( 1)
                var artist = mediaCursor.getString( 2)
                title = title.replace("'", "")
                artist = artist.replace("'", "")

                items.add( MediaRawItem(
                    id = mediaCursor.getString( 0),
                    title = title,
                    artist = artist,
                    albumId = mediaCursor.getString( 3),
                    duration = mediaCursor.getLong( 4),
                    favorite = 0L))
            }
        } catch ( e: NullPointerException)  { // Logging.e("MainActivity.getMediaList NPE")
        } catch ( e: Exception)             { // Logging.e("MainActivity.getMediaList something happen? $e")
        } finally { mediaCursor?.close() }

        // Logging.d("MainActivity.getMediaList ${ items[0]}")
        return items
    }

    // when searching item, Jmp Here!
    private fun searchDBTask( query: String) {

        // if this has Permission? then ok. but havent Perm? stop execute this
        if ( !this.hasPerm()) { return }

        // when items are nothing? stop task
        var mediaItems = mutableListOf< MediaPlay>()
        val buf: MutableList<MediaRawItem> = this.dbHelper.selectItemsBySearch( query) ?: return

        val iterator = buf.iterator()
        try {
            while( iterator.hasNext() ) { mediaItems.add( MediaPlay( iterator.next())) }
        } catch ( e: NoSuchElementException )   {
        } catch ( e: Exception )                {  }

        this.setRecyclerView( mediaItems)
    }

    // show ONLY My Favorite Music from DB
    private fun searchFavorDBTask() {

        // if this has Permission? then ok. but havent Perm? stop execute this
        if ( !this.hasPerm()) { return }

        mediaItems = mutableListOf()
        val buf  = this.dbHelper.selectItemsByFavorite()

        val iterator = buf.iterator()
        try{
            while( iterator.hasNext()) {
                this.mediaItems.add( MediaPlay( iterator.next()))
            }

        } catch ( e: NoSuchElementException)    {
        } catch ( e: Exception)                 { }

        setRecyclerView( this.mediaItems)
    }

    // start to load Music from DB
    private fun initDBTask() {

        // if this has Permission? then ok. but havent Perm? stop execute this
        if ( !this.hasPerm()) { return }

        // is DB EMPTY? than, __INIT__ DB
        if ( this.dbHelper.selectAllItems() == null ) {
            val buf = this.getMediaList()
            val iterator = buf.iterator()

            try {
                while( iterator.hasNext()) {
                    var item = iterator.next()
                    this.dbHelper.insertItem( item)
                    this.mediaItems.add( MediaPlay( item))
                }
            } catch ( e: NoSuchElementException)    { // Logging.e("MainActivity.startDBTask.db init NoSuchException")
            } catch ( e: Exception )                { } // Logging.e("MainActivity.startDBTask.db init something happen?") }


        // DB is already have... load to current DB
        } else {
            try {
                this.mediaItems = mutableListOf()

                val buf = this.dbHelper.selectAllItems()!!
                val iterator = buf.iterator()
                while( iterator.hasNext()) {
                    this.mediaItems.add( MediaPlay( iterator.next()))
                }

            } catch ( e: NullPointerException )     { // Logging.e("MainActivity.startDBTask NPE")
            } catch ( e: NoSuchElementException )   { // Logging.e("MainActivity.startDBTask NoSuchException")
            } catch ( e: Exception )                { } // Logging.e("MainActivity.startDBTask something happen?") }
        }

        this.setRecyclerView( this.mediaItems)
    }

    // set RecyclerView by items
    private fun setRecyclerView(items: MutableList< MediaPlay>) {
        this.recyclerAdapter                            = RecyclerAdapter(this, items)
        this.bind.ActivityMainRecycler.adapter          = this.recyclerAdapter
        this.bind.ActivityMainRecycler.layoutManager    = LinearLayoutManager( this)
        this.recyclerAdapter.notifyDataSetChanged()
    }

    // this guy have Permission???
    private fun hasPerm(): Boolean {
        return  ContextCompat.checkSelfPermission( this, this.permList[ 0]) ==
                PackageManager.PERMISSION_GRANTED
    }
}