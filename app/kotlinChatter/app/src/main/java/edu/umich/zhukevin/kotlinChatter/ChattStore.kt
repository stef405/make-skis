package edu.umich.zhukevin.kotlinChatter

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableArrayList
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.reflect.full.declaredMemberProperties

object ChattStore {
    val chatts = ObservableArrayList<Chatt>()
    private val nFields = Chatt::class.declaredMemberProperties.size

    private const val serverUrl = "https://3.145.45.188/"

    private val client = OkHttpClient()

    fun postChatt(context: Context, chatt: Chatt, imageUri: Uri?, videoUri: Uri?,
                  completion: (String) -> Unit) {
        val mpFD = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("username", chatt.username ?: "")
            .addFormDataPart("message", chatt.message ?: "")

        imageUri?.run {
            toFile(context)?.let {
                mpFD.addFormDataPart("image", "chattImage",
                    it.asRequestBody("image/jpeg".toMediaType()))
            } ?: context.toast("Unsupported image format")
        }

        videoUri?.run {
            toFile(context)?.let {
                mpFD.addFormDataPart("video", "chattVideo",
                    it.asRequestBody("video/mp4".toMediaType()))
            } ?: context.toast("Unsupported video format")
        }

        val request = Request.Builder()
            .url(serverUrl+"postimages/")
            .post(mpFD.build())
            .build()

        context.toast("Posting . . . wait for 'Chatt posted!'")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(e.localizedMessage ?: "Posting failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    getChatts()
                    completion("Chatt posted!")
                }
            }
        })
    }

    fun getChatts() {
        val request = Request.Builder()
            .url(serverUrl+"getimages/")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("getChatts", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val chattsReceived = try { JSONObject(response.body?.string() ?: "").getJSONArray("chatts") } catch (e: JSONException) { JSONArray() }

                    chatts.clear()
                    for (i in 0 until chattsReceived.length()) {
                        val chattEntry = chattsReceived[i] as JSONArray
                        if (chattEntry.length() == nFields) {
                            chatts.add(Chatt(username = chattEntry[0].toString(),
                                message = chattEntry[1].toString(),
                                timestamp = chattEntry[2].toString(),
                                imageUrl = chattEntry[3].toString(),
                                videoUrl = chattEntry[4].toString(),
                            ))
                        } else {
                            Log.e("getChatts", "Received unexpected number of fields " + chattEntry.length().toString() + " instead of " + nFields.toString())
                        }
                    }
                }
            }
        })
    }
}