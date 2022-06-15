package com.mrhi.exampleandroid20220531.requestmediaplayer

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.mrhi.exampleandroid20220531.requestmediaplayer.databinding.ActivityMediaPlayBinding
import com.mrhi.exampleandroid20220531.requestmediaplayer.mediamodule.MediaPlay
import kotlinx.coroutines.*
import java.text.SimpleDateFormat


/******************************
 *
 *      LEGACY CODE
 *
 *      DIDNT USE ANYWHERE
 *
 *****************************/


// __DIDNT USE ANYWHERE__
class MediaPlayActivity :
    AppCompatActivity(), View.OnClickListener {
    private val bind by lazy { ActivityMediaPlayBinding.inflate( layoutInflater) }
    private lateinit var media: MediaPlay
    private var mediaPlayer: MediaPlayer? = null
    private var mediaLauncher: CoroutineScope? = null
    private var mediaJob: Job? = null


    override fun onCreate( savedInstanceState: Bundle?) {
        super.onCreate( savedInstanceState)
        setContentView( this.bind.root)
        this.media = intent.getSerializableExtra("music") as MediaPlay

        this.initView()
        this.initMediaPlayer()
        this.onSeekbarEvtHandle()
    }

    override fun onDestroy() {
        super.onDestroy()

        if( this.mediaPlayer != null ){
            this.mediaPlayer?.stop()
            this.mediaPlayer?.release()
//            this.mediaPlayer = null
        }

        if ( this.mediaJob != null ){
            this.mediaJob?.cancel()
//            this.mediaJob = null;
        }

        if( this.mediaLauncher != null) {
            this.mediaLauncher?.cancel()
        }
    }

    override fun onClick( v: View?) {

        when( v?.id ?: -1) {

            R.id.ActivityMediaPlay_action       -> {

                    // media stop
                if( this.mediaPlayer!!.isPlaying ) {
                    this.mediaPlayer?.pause()
                    this.bind.ActivityMediaPlayAction.setImageResource( R.drawable.icon_play_24)

                    // media start
                } else {
                    this.mediaPlayer?.start()
                    this.bind.ActivityMediaPlayAction.setImageResource( R.drawable.icon_pause_24)

                    this.mediaLauncher = CoroutineScope( Dispatchers.Default + Job())
                    this.mediaJob = this.mediaLauncher?.launch {

                        // ASYNC TASK
                        // while media playing
                        while( mediaPlayer!!.isPlaying) {
                            runOnUiThread {
                                try{
                                    // run on MAIN THREAD
                                    var currentPos = mediaPlayer?.currentPosition!!
                                    bind.ActivityMediaPlayDurationFront.text =
                                        SimpleDateFormat("mm:ss").format( currentPos)
                                    bind.ActivityMediaPlaySeekbar.progress = currentPos
                                } catch ( e: NullPointerException)  {
                                } catch ( e: Exception)             {  }
                            }
                            delay( 100)
                        }// endl while()

                        // when media end...
                        runOnUiThread{
                            try {
                                if( mediaPlayer!!.currentPosition >= ( bind.ActivityMediaPlaySeekbar.max - 100)){
                                    bind.ActivityMediaPlaySeekbar.progress = 0
                                    bind.ActivityMediaPlayDurationFront.text = "00:00"
                                    bind.ActivityMediaPlayAction.setImageResource( R.drawable.icon_play_24)
                                }
                            } catch ( e: NullPointerException)  {
                            } catch ( e: Exception)             {  }
                        }
                    }
                }
            }

            R.id.ActivityMediaPlay_actionMenu   -> { this.finish() }

            R.id.ActivityMediaPlay_actionStop   -> {
                this.mediaPlayer?.pause()
                this.mediaPlayer?.seekTo(0)
                this.bind.ActivityMediaPlaySeekbar.progress = 0
                this.bind.ActivityMediaPlayDurationFront.text = "00:00"
                this.bind.ActivityMediaPlayAction.setImageResource( R.drawable.icon_play_24)
                // this.mediaLauncher?.cancel()
            }
        }
    }

    private fun onSeekbarEvtHandle() {
        this.bind.ActivityMediaPlaySeekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged( seekbar: SeekBar?, progress: Int, fromUsr: Boolean) {
                    if( fromUsr){
                        mediaPlayer?.seekTo( progress)
                        bind.ActivityMediaPlayDurationFront.text = SimpleDateFormat("mm:ss").format( progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}
            }
        )
    }

    private fun initMediaPlayer() { this.mediaPlayer = MediaPlayer.create( this, this.media.getMediaInfo()) }

    private fun initView() {
        if( this.media == null) { return }

        this.bind.ActivityMediaPlayArtist.text  = this.media.artist
        this.bind.ActivityMediaPlayTitle.text   = this.media.title
        this.bind.ActivityMediaPlayDurationFront.text = "00:00"
        this.bind.ActivityMediaPlayDurationRear.text = SimpleDateFormat("mm:ss").format( this.media.duration)
        this.bind.ActivityMediaPlaySeekbar.max = this.media.duration!!.toInt()

        val coverArt = media.getMediaCoverArt( this, MediaPlay.MEDIA_COVER_ART_SIZE_150)

        if( coverArt != null) { this.bind.ActivityMediaPlayCoverArt.setImageBitmap( coverArt )}
        else { this.bind.ActivityMediaPlayCoverArt.setImageResource( R.drawable.icon_audio_24) }
    }
}