package edu.umich.zhukevin.kotlinChatter

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityVideoPlayBinding

class VideoPlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(view.root)

        view.videoView.setVideoURI(intent.getParcelableExtra("VIDEO_URI", Uri::class.java))

        with (MediaController(this)) {
            setAnchorView(view.videoView)
            view.videoView.setMediaController(this)
            view.videoView.setOnPreparedListener { show(0) }
        }
        view.videoView.setOnCompletionListener { finish() }
        view.videoView.start()
    }
}