//package edu.umich.zhukevin.kotlinChatter
//
//import android.view.Menu.FIRST
//import android.view.Menu.NONE
//
//import android.Manifest
//import android.app.Activity
//import android.content.ComponentName
//import android.content.ContentValues
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Bundle
//import android.os.Environment
//import android.provider.MediaStore
//import android.util.Log
//import android.view.Menu
//import android.view.MenuItem
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.ViewModel
//import edu.umich.zhukevin.kotlinChatter.ChattStore.postChatt
//import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPostBinding
//
//class PostActivity : AppCompatActivity() {
//    private lateinit var view: ActivityPostBinding
//    private var enableSend = true
//    private val viewState: PostViewState by viewModels()
//    private lateinit var forCropResult: ActivityResultLauncher<Intent>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        view = ActivityPostBinding.inflate(layoutInflater)
//        setContentView(view.root)
//
//        view.videoButton.setImageResource(viewState.videoIcon)
//        viewState.imageUri?.let { view.previewImage.display(it) }
//
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
//            results.forEach {
//                if (!it.value) {
//                    toast("${it.key} access denied")
//                    finish()
//                }
//            }
//        }.launch(arrayOf(Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.READ_MEDIA_IMAGES,
//            Manifest.permission.READ_MEDIA_VIDEO))
//
//        val cropIntent = initCropIntent()
//        val forPickedResult =
//            registerForActivityResult(ActivityResultContracts.GetContent(), fun(uri: Uri?) {
//                uri?.let {
//                    if (it.toString().contains("video")) {
//                        viewState.videoUri = it
//                        viewState.videoIcon = android.R.drawable.presence_video_busy
//                        view.videoButton.setImageResource(viewState.videoIcon)
//                    } else {
//                        val inStream = contentResolver.openInputStream(it) ?: return
//                        viewState.imageUri = mediaStoreAlloc("image/jpeg")
//                        viewState.imageUri?.let {
//                            val outStream = contentResolver.openOutputStream(it) ?: return
//                            val buffer = ByteArray(8192)
//                            var read: Int
//                            while (inStream.read(buffer).also{ read = it } != -1) {
//                                outStream.write(buffer, 0, read)
//                            }
//                            outStream.flush()
//                            outStream.close()
//                            inStream.close()
//                        }
//                        doCrop(cropIntent)
//                    }
//                } ?: run { Log.d("Pick media", "failed") }
//            })
//        view.albumButton.setOnClickListener {
//            forPickedResult.launch("*/*")
//        }
//
//        forCropResult =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == Activity.RESULT_OK) {
//                    result.data?.data.let {
//                        viewState.imageUri?.run {
//                            if (!toString().contains("ORIGINAL")) {
//                                // delete uncropped photo taken for posting
//                                contentResolver.delete(this, null, null)
//                            }
//                        }
//                        viewState.imageUri = it
//                        viewState.imageUri?.let { view.previewImage.display(it) }
//                    }
//                } else {
//                    Log.d("Crop", result.resultCode.toString())
//                }
//            }
//
//        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
//            toast("Device has no camera!")
//            return
//        }
//
//        var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture())
//        { success ->
//            if (success) {
//                doCrop(cropIntent)
//            } else {
//                Log.d("TakePicture", "failed")
//            }
//        }
//        view.cameraButton.setOnClickListener {
//            viewState.imageUri = mediaStoreAlloc(mediaType="image/jpeg")
//            takePicture.launch(viewState.imageUri)
//        }
//
//        var captureVideo = registerForActivityResult(ActivityResultContracts.CaptureVideo())
//        {
//            viewState.videoIcon = android.R.drawable.presence_video_busy
//            view.videoButton.setImageResource(viewState.videoIcon)
//        }
//        view.videoButton.setOnClickListener {
//            viewState.videoUri = mediaStoreAlloc(mediaType="video/mp4")
//            captureVideo.launch(viewState.videoUri)
//        }
//
//    }
//
//    private fun initCropIntent(): Intent? {
//        // Is there any registered Activity on device to do image cropping?
//        val intent = Intent("com.android.camera.action.CROP")
//        intent.type = "image/*"
//        val listofCroppers = packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
//
//        // No image cropping Activity registered
//        if (listofCroppers.size == 0) {
//            toast("Device does not support image cropping")
//            return null
//        }
//
//        intent.component = ComponentName(
//            listofCroppers[0].activityInfo.packageName,
//            listofCroppers[0].activityInfo.name)
//
//        // create a square crop box:
//        intent.putExtra("outputX", 500)
//            .putExtra("outputY", 500)
//            .putExtra("aspectX", 1)
//            .putExtra("aspectY", 1)
//            // enable zoom and crop
//            .putExtra("scale", true)
//            .putExtra("crop", true)
//            .putExtra("return-data", true)
//
//        return intent
//    }
//
//    private fun mediaStoreAlloc(mediaType: String): Uri? {
//        val values = ContentValues()
//        values.put(MediaStore.MediaColumns.MIME_TYPE, mediaType)
//        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//
//        return contentResolver.insert(
//            if (mediaType.contains("video"))
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//            else
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            values)
//    }
//
//    private fun doCrop(intent: Intent?) {
//        intent ?: run {
//            viewState.imageUri?.let { view.previewImage.display(it) }
//            return
//        }
//
//        viewState.imageUri?.let {
//            intent.data = it
//            forCropResult.launch(intent)
//        }
//    }
//
//    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        with (menu) {
//            add(NONE, FIRST, NONE, getString(R.string.send))
//            getItem(0).setIcon(android.R.drawable.ic_menu_send).setEnabled(viewState.enableSend)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
//        }
//        return super.onPrepareOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == FIRST) {
//            viewState.enableSend = false
//            invalidateOptionsMenu()
//            submitChatt()
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
////    private fun submitChatt() {
////        val chatt = Chatt(username = view.usernameTextView.text.toString(),
////            message = view.messageTextView.text.toString())
////
////        postChatt(applicationContext, chatt, viewState.imageUri, viewState.videoUri) { msg ->
////            runOnUiThread {
////                toast(msg)
////            }
////            finish()
////        }
////    }
//}
//
//class PostViewState: ViewModel() {
//    var enableSend = true
//    var imageUri: Uri? = null
//    var videoUri: Uri? = null
//    var videoIcon = android.R.drawable.presence_video_online
//}