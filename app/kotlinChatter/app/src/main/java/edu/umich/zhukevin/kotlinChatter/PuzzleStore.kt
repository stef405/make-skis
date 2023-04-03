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
    private val nPuzzleFields = Puzzle::class.declaredMemberProperties.size
    private const val serverUrl = "https://3.16.218.169/"
    private val client = OkHttpClient()

    fun postPiece(context: Context, piece: Piece, imageUri: Uri?, completion: (String) -> Unit) {
        val mpFD = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("puzzle_id", piece.puzzle_id ?: "")
            .addFormDataPart("difficulty",piece.difficulty ?: "")
            .addFormDataPart("height", piece.height ?: "")
            .addFormDataPart("width", piece.width ?: "")

        imageUri?.run {
            toFile(context)?.let {
                mpFD.addFormDataPart("piece_img", "puzzlePieceImage",
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
                    getPieces(piece.puzzle_id)
                    completion("Chatt posted!")
                }
            }
        })
    }

    fun postPuzzle(context: Context, puzzle: Puzzle, imageUri: Uri?, completion: (String) -> Unit) {

//        var user_id_int: Int = puzzle.user_id.toInt()

        Log.d("postpuzzle", "imageUri = " + imageUri.toString())

        val mpFD = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("user_id", puzzle.user_id ?: "")
            .addFormDataPart("height", puzzle.height ?: "")
            .addFormDataPart("width", puzzle.width ?: "")

        imageUri?.run {
            toFile(context)?.let {
                mpFD.addFormDataPart("puzzle_img", "puzzlePieceImage",
                    it.asRequestBody("image/jpeg".toMediaType()))

            } ?: context.toast("Unsupported image format")
        }

        val request = Request.Builder()
            .url(serverUrl +"postpuzzle/") //+ puzzle.user_id ) //https://3.16.218.169/postpuzzle/1/
            .post(mpFD.build())
            .build()

        //context.toast("Posting . . . wait for 'Puzzle posted!'")


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

    fun getPieces(puzzle_id : String?) {
        val request = Request.Builder()
            .url(serverUrl + "getpieces/" + "24/")
            .build()

        Log.d("getpieces", request.toString())


        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("savedPieces", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("getpieces","Successful GET request") //successfully goes into function
                    val piecesReceived = try { JSONObject(response.body?.string() ?: "").getJSONArray("pieces") } catch (e: JSONException) { JSONArray() }

                    Log.d("getpieces","piecesRecieved= " + piecesReceived.length())
                    pieces.clear()
                    for (i in 0 until piecesReceived.length()) {
                        val piece = piecesReceived[i] as JSONObject
                        val pieceID: String = piece.getString("piece_id")
                        val pieceIMG: String = piece.getString("piece_img")
                        val solutionIMG: String = piece.getString("solution_img")
                        val difficulty: String = piece.getString("difficulty")
                        val width: String = piece.getString("width")
                        val height: String = piece.getString("height")
                        if (piece.length() == nFields) {
                            pieces.add(Piece(
                                piece_id = pieceID,
                                piece_img = pieceIMG,
                                solution_img = solutionIMG,
                                difficulty = difficulty,
                                width = width,
                                height = height),
                            )
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
            .url(serverUrl + "getpuzzles/" + "10")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("getpuzzles", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("getpuzzles","Successful GET request")
                    val puzzlesReceived = try { JSONObject(response.body?.string() ?: "").getJSONArray("puzzles") } catch (e: JSONException) { JSONArray() }

                    puzzles.clear()
                    for (i in 0 until puzzlesReceived.length()) {
                        val puzzle = puzzlesReceived[i] as JSONObject
                        val puzzleID: String = puzzle.getString("puzzle_id")
                        val puzzleIMG: String = puzzle.getString("puzzle_img")
                        val puzzleWidth: String = puzzle.getString("width")
                        val puzzleHeight: String = puzzle.getString("height")
                        if (puzzle.length() == nPuzzleFields - 1) {
                            puzzles.add(Puzzle(
                                user_id = "10",
                                puzzle_id = puzzleID,
                                height =  puzzleHeight,
                                width =  puzzleWidth,
                                imageUrl = puzzleIMG
                            ))
                        } else {
                            Log.d("getpuzzles", "Received unexpected number of fields " + puzzle.length().toString() + " instead of " + PuzzleStore.nFields.toString())
                        }
                    }
                }
            }
        })
    }

    fun deletePuzzle(id: String?) {
        val request = Request.Builder()
            .url(serverUrl + "deletepuzzle/" + id + "/")
            .delete()
            .build()

        Log.d("deleteWasCalled", request.toString())
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("deletePuzzle", "Failed DELETE request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    //Update list screen
                    getPuzzles()
                    Log.d("deletePuzzle","DELETE SUCCESS")
                }
            }
        })
    }

    fun deletePiece(id: String?,puzzle_id: String?) {
        val request = Request.Builder()
            .url(serverUrl + "deletepiece/" + id + "/")
            .delete()
            .build()

        Log.d("deleteWasCalled", "Delete was callaed")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("deletePiece", "Failed DELETE request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    //Update list screen
                    getPieces(puzzle_id)
                    Log.d("deletePiece","DELETE SUCCESS")
                }
            }

        })
    }

}

