package com.mrhi.exampleandroid20220531.requestmediaplayer

import android.content.res.Configuration
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.viewpager2.widget.ViewPager2
import com.mrhi.exampleandroid20220531.requestmediaplayer.databinding.ActivityMediaEnhancedBinding
import com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule.DBOpenHelper
import com.mrhi.exampleandroid20220531.requestmediaplayer.dbmodule.MediaRawItem
import com.mrhi.exampleandroid20220531.requestmediaplayer.mediamodule.MediaPlay
import com.mrhi.exampleandroid20220531.requestmediaplayer.utill.Logging
import com.mrhi.exampleandroid20220531.requestmediaplayer.viewpagermodule.ViewPagerAdapter
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

/********************
 *
 *  2022/06/15
 *  Parcelize Task
 *
 ********************/




class MediaEnhancedActivity :
    AppCompatActivity(), View.OnClickListener {
    private val bind by lazy { ActivityMediaEnhancedBinding.inflate( layoutInflater) }
    private val items by lazy { mutableListOf< MediaPlay>() }
    private var mediaPlayer: MediaPlayer? = null
    private var mediaLauncher: CoroutineScope? = null
    private var mediaJob: Job? = null
    private var mediaPos = 0 // mediaPlayer CURSOR


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val theme = newConfig.uiMode

        if( theme == Configuration.UI_MODE_NIGHT_NO )   { } // AppCompatDelegate.setDefaultNightMode( MODE_NIGHT_YES ) }
        else                                            {  }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( this.bind.root)

        this.initProperties()
        this.initView()
        this.initMediaProgress( this.mediaPos)
    }

    // release Resource
    override fun onDestroy() {
        super.onDestroy()

        this.releaseMedia()

        if ( this.mediaJob != null ){ this.mediaJob?.cancel() }
        if( this.mediaLauncher != null) { this.mediaLauncher?.cancel() }
    }

    override fun onClick( v: View?) {
        when( v?.id ?: -2) {

            // PLAY or PAUSE music
            R.id.ActivityMediaEnhanced_action -> {

                // is playing? -> pause media
                if( this.mediaPlayer!!.isPlaying) { this.pauseMedia() }

                // not playing? -> start media
                else { this.startMedia() }
            }

            R.id.ActivityMediaEnhanced_actionStop -> { this.stopMedia() }
            R.id.ActivityMediaEnhanced_actionMenu -> { this.finish() }
        }
    }

    // start Media Player
    private fun startMedia() {
        this.mediaPlayer?.start()
        this.bind.ActivityMediaEnhancedAction.setImageResource( R.drawable.icon_pause_24)

        // ASYNC task...
        this.mediaLauncher = CoroutineScope( Dispatchers.Default + Job())
        this.mediaJob = this.mediaLauncher?.launch {

            try {
                while (mediaPlayer!!.isPlaying) {
                    runOnUiThread {
                        try {
                            var currentPos = mediaPlayer?.currentPosition!!
                            bind.ActivityMediaEnhancedDurationFront.text =
                                SimpleDateFormat("mm:ss").format(currentPos)
                            bind.ActivityMediaEnhancedDurationProgress.progress = currentPos
                        } catch (e: NullPointerException)   {
                        } catch (e: Exception)              {
                        }
                    }
                    delay(100)
                } // endl while( ...)
            } catch ( e: IllegalStateException) {
            } catch ( e: Exception)             {  }

            // when play has ended...
            try {
                runOnUiThread {
                    try {
                        if (mediaPlayer!!.currentPosition >= (bind.ActivityMediaEnhancedDurationProgress.max - 100)) {
                            bind.ActivityMediaEnhancedDurationFront.text = "00:00"
                            bind.ActivityMediaEnhancedDurationProgress.progress = 0
                            bind.ActivityMediaEnhancedAction.setImageResource(R.drawable.icon_play_24)
                        }
                    } catch (e: NullPointerException)   {
                    } catch (e: IllegalStateException)  {
                    } catch (e: Exception)              {
                    }
                }
            } catch ( e: IllegalStateException )    {
            } catch ( e: Exception )                {  }

        }// endl ASYNC task!
    }

    // pause Media Player
    private fun pauseMedia() {
        this.mediaPlayer?.pause()
        this.bind.ActivityMediaEnhancedAction.setImageResource( R.drawable.icon_play_24)
    }

    // stop Media Player
    private fun stopMedia() {
        this.mediaPlayer?.pause()
        this.mediaPlayer?.seekTo( 0)
        this.bind.ActivityMediaEnhancedDurationProgress.progress = 0
        this.bind.ActivityMediaEnhancedDurationFront.text = "00:00"
        this.bind.ActivityMediaEnhancedAction.setImageResource( R.drawable.icon_play_24)
    }

    // release Resource
    private fun releaseMedia() {
        if( this.mediaPlayer != null && mediaPlayer!!.isPlaying) {
            this.mediaPlayer?.stop()
            this.mediaPlayer?.release()
        } else { this.mediaPlayer?.release() ?: Logging.d("release Media -> media is NULL")}
    }

    // init Properties
    private fun initProperties()  {
        val buf: ArrayList< MediaPlay>? = intent?.getParcelableArrayListExtra("music")

        // set music Items
        val iterator = buf!!.iterator()
        try { while ( iterator.hasNext()) { this.items.add( iterator.next()) } }
        catch ( e: NoSuchElementException) {  }

        // set ViewPager's Position
        this.mediaPos = intent.getIntExtra("pos", 0)
    }

    // init this View
    private fun initView() {

        // init ViewPager's adapter
        val adapter = ViewPagerAdapter( this)
        val iterator = items.iterator()
        try {
            while ( iterator.hasNext()) {
                var item = MediaViewPageFragment()
                item.arguments = Bundle().apply { this.putSerializable("media", iterator.next()) }
                adapter.addItem(item)
            }
        } catch ( e: NoSuchElementException ) { } // Logging.e("MediaEnhanced.initProperties noElementException item size : ${ items.size}") }

        // init ViewPager
        this.bind.activityMediaEnhancedViewPager.adapter = adapter
        this.bind.activityMediaEnhancedViewPager.registerOnPageChangeCallback( object :
            ViewPager2.OnPageChangeCallback() {

            // when Page Selected. play that's item( Music).
            override fun onPageSelected( pos: Int) {
                super.onPageSelected( pos)

                mediaPos = pos
                initMediaProgress( mediaPos)
                // Logging.d("viewPager pos : $pos")
            }
        })

        // init seekbar EVT
        this.bind?.ActivityMediaEnhancedDurationProgress.setOnSeekBarChangeListener (
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged( seekbar: SeekBar?, progress: Int, fromUsr: Boolean) {

                    // when seekbar seek, update them
                    if ( fromUsr){
                        mediaPlayer?.seekTo( progress)
                        bind.ActivityMediaEnhancedDurationFront.text =
                            SimpleDateFormat("mm:ss").format( progress)
                    }
                }

                override fun onStartTrackingTouch( seekbar: SeekBar?) {}

                override fun onStopTrackingTouch( seekbar: SeekBar?) {}
            }
        )// endl Seekbar.onProgressChanged()
    }// endl initView()

    // reset seekBar and more view....
    private fun initMediaProgress( itemPos: Int) {
        this.releaseMedia()
        this.mediaPlayer = MediaPlayer.create(
            this@MediaEnhancedActivity.applicationContext,
            items[ itemPos].getMediaInfo() )

        this.bind?.ActivityMediaEnhancedDurationProgress.progress   = 0
        this.bind?.ActivityMediaEnhancedAction.setImageResource( R.drawable.icon_play_24)
        this.bind?.activityMediaEnhancedViewPager.currentItem       = itemPos
        this.bind?.ActivityMediaEnhancedDurationRear.text           = SimpleDateFormat("mm:ss").format( items[ itemPos].duration)
        this.bind?.ActivityMediaEnhancedDurationFront.text          = "00:00"
        this.bind?.ActivityMediaEnhancedDurationProgress.max        = this.items[ itemPos].duration!!.toInt()

        // Logging.d("MEDIA INFO -> title : ${ items[ itemPos].title} artist : ${ items[ itemPos].artist} id : ${ items[ itemPos].id}")
    }
}