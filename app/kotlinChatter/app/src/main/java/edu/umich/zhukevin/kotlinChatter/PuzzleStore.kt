package edu.umich.zhukevin.kotlinChatter

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableArrayList
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

object PuzzleStore {
    val pieces = ObservableArrayList<Piece>()
    val puzzles = ObservableArrayList<Puzzle>()
    private val nFields = Piece::class.declaredMemberProperties.size
    private const val serverUrl = "https://3.16.218.169/"
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
            .url(serverUrl +"postpiece/") //https://3.16.218.169/postpiece/puzzlePieceImage.jpeg
            .post(mpFD.build())
            .build()

        context.toast("Posting . . . wait for 'Chatt posted!'")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(e.localizedMessage ?: "Posting failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    getPieces()
                    completion("Chatt posted!")
                }
            }
        })
    }

    fun postPuzzle(context: Context, puzzle: Puzzle, imageUri: Uri?, completion: (String) -> Unit) {

//        var user_id_int: Int = puzzle.user_id.toInt()

        Log.d("imageUri", imageUri.toString())

        val mpFD = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("user_id", puzzle.user_id ?: "")
            .addFormDataPart("piece_ct", puzzle.piece_ct ?: "")
            .addFormDataPart("height", puzzle.height ?: "")
            .addFormDataPart("width", puzzle.width ?: "")

        imageUri?.run {
            toFile(context)?.let {
                mpFD.addFormDataPart("puzzle_img", "puzzlePieceImage",
                    it.asRequestBody("image/jpeg".toMediaType()))
            } ?: context.toast("Unsupported image format")
        }

        val request = Request.Builder()
            .url(serverUrl +"postpuzzle/" + puzzle.user_id ) //https://3.16.218.169/postpuzzle/1/
            .post(mpFD.build())
            .build()

        context.toast("Posting . . . wait for 'Chatt posted!'")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("error posting", e.localizedMessage)
                completion(e.localizedMessage ?: "Posting failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    getPuzzles()
                    completion("Puzzle posted!")
                }
            }
        })
    }

    fun getPieces() {
        val request = Request.Builder()
            .url(serverUrl + "savedpieces/") //https://3.16.218.169/postpiece/puzzlePieceImage.jpeg
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("savedPieces", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val piecesReceived = try { JSONObject(response.body?.string() ?: "").getJSONArray("puzzle_pieces_images") } catch (e: JSONException) { JSONArray() }

                    pieces.clear()
                    for (i in 0 until piecesReceived.length()) {
                        val piece = piecesReceived[i] as JSONArray
                        if (piece.length() == nFields) {
                            pieces.add(Piece(imageUrl = piece[0].toString()))
                        } else {
                            Log.e("getPieces", "Received unexpected number of fields " + piece.length().toString() + " instead of " + PuzzleStore.nFields.toString())
                        }
                    }
                }
            }
        })
    }

    fun getPuzzles() {
        val request = Request.Builder()
            .url(serverUrl + "savedpuzzles/")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("savedpuzzles", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val puzzlesReceived = try { JSONObject(response.body?.string() ?: "").getJSONArray("puzzle_pieces_images") } catch (e: JSONException) { JSONArray() }

                    puzzles.clear()
                    for (i in 0 until puzzlesReceived.length()) {
                        val puzzle = puzzlesReceived[i] as JSONArray
                        if (puzzle.length() == nFields) {
                            puzzles.add(Puzzle(imageUrl = puzzle[0].toString()))
                        } else {
                            Log.e("getPieces", "Received unexpected number of fields " + puzzle.length().toString() + " instead of " + PuzzleStore.nFields.toString())
                        }
                    }
                }
            }
        })
    }

}

