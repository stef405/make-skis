package edu.umich.zhukevin.kotlinChatter

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableArrayList
import edu.umich.zhukevin.kotlinChatter.PieceStore.pieces
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.reflect.full.declaredMemberProperties

object PieceStore {
    val pieces = ObservableArrayList<Piece>()
    private val nFields = Piece::class.declaredMemberProperties.size
    private const val serverUrl = "https://64179f201b0effb9b4937772.mockapi.io/Piece/1/" //Replace with actual BE
    private val client = OkHttpClient()

    fun postPiece(context: Context, piece: Piece, imageUri: Uri?, completion: (String) -> Unit) {
        val mpFD = MultipartBody.Builder().setType(MultipartBody.FORM)

        imageUri?.run {
            toFile(context)?.let {
                mpFD.addFormDataPart("image", "puzzlePieceImage",
                    it.asRequestBody("image/jpeg".toMediaType()))
            } ?: context.toast("Unsupported image format")
        }

        val request = Request.Builder()
            .url(PieceStore.serverUrl +"postpiece/") //Replace with actual route
            .post(mpFD.build())
            .build()

        context.toast("Posting . . . wait for 'Chatt posted!'")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(e.localizedMessage ?: "Posting failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    PieceStore.getPieces()
                    completion("Chatt posted!")
                }
            }
        })


    }

    fun getPieces() {
        val request = Request.Builder()
            .url(PieceStore.serverUrl + "savedpuzzles/")
            .build()

        PieceStore.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("savedpuzzles", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val piecesReceived = try { JSONObject(response.body?.string() ?: "").getJSONArray("puzzle_pieces_images") } catch (e: JSONException) { JSONArray() }

                    PieceStore.pieces.clear()
                    for (i in 0 until piecesReceived.length()) {
                        val piece = piecesReceived[i] as JSONArray
                        if (piece.length() == PieceStore.nFields) {
                            PieceStore.pieces.add(Piece(imageUrl = piece[0].toString()))
                        } else {
                            Log.e("getPieces", "Received unexpected number of fields " + piece.length().toString() + " instead of " + PieceStore.nFields.toString())
                        }
                    }
                }
            }
        })
    }

}

