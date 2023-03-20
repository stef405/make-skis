package edu.umich.zxiris.kotlinChatter

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import edu.umich.zxiris.kotlinChatter.ChattStore.chatts
import edu.umich.zxiris.kotlinChatter.ChattStore.getChatts
import edu.umich.zxiris.kotlinChatter.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {
    private lateinit var view: ActivityMainBinding
    private lateinit var chattListAdapter: ChattListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityMainBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#E0E0E0"))
        setContentView(view.root)

        chattListAdapter = ChattListAdapter(this, chatts)
        view.chattListView.setAdapter(chattListAdapter)

        // setup refreshContainer here later
        view.refreshContainer.setOnRefreshListener {
            refreshTimeline()
        }

        refreshTimeline()
    }

    private fun refreshTimeline() {
        getChatts(applicationContext) {
            runOnUiThread {
                // inform the list adapter that data set has changed
                // so that it can redraw the screen.
                chattListAdapter.notifyDataSetChanged()
            }
            // stop the refreshing animation upon completion:
            view.refreshContainer.isRefreshing = false
        }
    }

    fun startPost(view: View?) = startActivity(Intent(this, PostActivity::class.java))

    fun retakeAlertMessage(view: View){
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(getString(R.string.fail_title))
            setMessage(getString(R.string.error_message))
            setPositiveButton(getString(R.string.retake_photo), DialogInterface.OnClickListener(positiveButtonClick))
            setNegativeButton(getString(R.string.exit_alert_dialog), negativeButtonClick)
            show()
        }
    }

    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            android.R.string.yes, Toast.LENGTH_SHORT).show()
    }

    val negativeButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            android.R.string.no, Toast.LENGTH_SHORT).show()
    }
}