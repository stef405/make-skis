package edu.umich.zhukevin.kotlinChatter

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableArrayList
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getPieces
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getPuzzles
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.pieces
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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
    var entry_popup : Boolean ?= null
    var last_puzzleID = ""
    var last_pieceID = ""

    var piece_popup : Boolean = false
    fun postPiece(context: Context, piece: Piece, imageUri: Uri?, completion: (String) -> Unit) : Boolean? {
        val mpFD = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("puzzle_id", piece.puzzle_id ?: "")
            .addFormDataPart("difficulty", piece.difficulty ?: "")

        imageUri?.run {
            toFile(context)?.let {
                mpFD.addFormDataPart(
                    "piece_img", "puzzlePieceImage",
                    it.asRequestBody("image/jpeg".toMediaType())
                )
            } ?: context.toast("Unsupported image format")
        }
        piece_popup = false
        val request = Request.Builder()
            .url(serverUrl + "postpiece/") //https://3.16.218.169/postpiece/puzzlePieceImage.jpeg
            .post(mpFD.build())
            .build()

        context.toast("Posting . . . wait for 'Chatt posted!'")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("PostPiece Fail: response call", e.localizedMessage );
                completion(e.localizedMessage ?: "Posting failed")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("PostPiece Success: response call", response.toString());
                if (response.isSuccessful) {
                    if (response.code == 202) {
                        piece_popup = true
                    }
                    getPieces(piece.puzzle_id)
                    completion("PIECE posted!")
                }
            }
        })
        return piece_popup
    }

    val scope = CoroutineScope(Dispatchers.IO)
    suspend fun postPuzzle(context: Context, puzzle: Puzzle, imageUri: Uri?, completion: (String) -> Unit) :
            Boolean {
        val res = CompletableDeferred<Boolean>()
        Log.d("postpuzzle", "imageUri = " + imageUri.toString())

        scope.launch {
            val mpFD = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("user_id", puzzle.user_id ?: "")

            imageUri?.run {
                toFile(context)?.let {
                    mpFD.addFormDataPart(
                        "puzzle_img", "puzzlePieceImage",
                        it.asRequestBody("image/jpeg".toMediaType())
                    )

                } ?: context.toast("Unsupported image format")
            }

            val request = Request.Builder()
                .url(serverUrl + "postpuzzle/") //+ puzzle.user_id ) //https://3.16.218.169/postpuzzle/1/
                .post(mpFD.build())
                .build()

            Log.d("postpuzzle", "request = " + request.toString())

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    completion(e.localizedMessage ?: "Posting failed")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        Log.d("response successful", response.code.toString())
                        if (response.code.toString() == "202") {
                            res.complete(true)
                            completion("Please Retake!")
                        } else {
//                            Log.d("success posting", "yay")
//                    getPuzzles()
                            res.complete(false)
                            completion("Puzzle posted!")
                        }
                    }
                }
            })
        }
        return res.await()
    }

    fun getPieces(puzzle_id: String?) {
        val request = Request.Builder()
            .url(serverUrl + "getpieces/" + puzzle_id + "/")
            .build()

        Log.d("getpieces", request.toString())

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("savedPieces", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("getpieces", "Successful GET request") //successfully goes into function
                    val piecesReceived = try {
                        JSONObject(response.body?.string() ?: "").getJSONArray("pieces")
                    } catch (e: JSONException) {
                        JSONArray()
                    }

                    Log.d("getpieces", "piecesRecieved= " + piecesReceived.length())
                    pieces.clear()
                    for (i in 0 until piecesReceived.length()) {
                        val piece = piecesReceived[i] as JSONObject
                        val pieceID: String = piece.getString("piece_id")
                        val pieceIMG: String = piece.getString("piece_img")
                        val solutionIMG: String = piece.getString("solution_img")
                        val difficulty: String = piece.getString("difficulty")
                        if (piece.length() == nFields - 1) {
                            pieces.add(
                                Piece(
                                    piece_id = pieceID,
                                    piece_img = pieceIMG,
                                    puzzle_id = puzzle_id,
                                    solution_img = solutionIMG,
                                    difficulty = difficulty
                                ),
                            )
                        } else {
                            Log.e(
                                "getPieces",
                                "Received unexpected number of fields " + piece.length()
                                    .toString() + " instead of " + PuzzleStore.nFields.toString()
                            )
                        }
                    }
                }
            }
        })
    }

    fun getPuzzles() {
        val request = Request.Builder()
            .url(serverUrl + "getpuzzles/" + "10" + "/")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("getpuzzles", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("getpuzzles", "Successful GET request")

                    Log.d("getpuzzles","response code = ${response.code}")
                    val puzzlesReceived = try {
                        JSONObject(response.body?.string() ?: "").getJSONArray("puzzles")
                    } catch (e: JSONException) {
                        JSONArray()
                    }
                    Log.d(
                        "getpuzzles",
                        "PuzzlesReceived length: ${puzzlesReceived.length()} (${puzzles.size})"
                    )
                    puzzles.clear()
                    Log.d("getpuzzles", "Puzzles size: ${puzzles.size}")
                    for (i in 0 until puzzlesReceived.length()) {
                        val puzzle = puzzlesReceived[i] as JSONObject
                        val puzzleID: String = puzzle.getString("puzzle_id")
                        val puzzleIMG: String = puzzle.getString("puzzle_img")
                        if (puzzle.length() == nPuzzleFields - 1) {
                            puzzles.add(
                                Puzzle(
                                    user_id = "10",
                                    puzzle_id = puzzleID,
                                    imageUrl = puzzleIMG
                                )
                            )
                        } else {
                            Log.d(
                                "getpuzzles",
                                "Received unexpected number of fields " + puzzle.length()
                                    .toString() + " instead of " + PuzzleStore.nFields.toString()
                            )
                        }
                    }
                }
            }
        })
    }

    fun getLastPuzzle(): String {
        val request = Request.Builder()
            .url(serverUrl + "getpuzzles/" + "10" + "/")
            .build()

        //var last_puzzleID = ""
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("getpuzzles", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("getLastPuzzle", "Successful GET request")
                    val puzzlesReceived = try {
                        JSONObject(response.body?.string() ?: "").getJSONArray("puzzles")
                    } catch (e: JSONException) {
                        JSONArray()
                    }
                    Log.d(
                        "getLastPuzzle",
                        "PuzzlesReceived length: ${puzzlesReceived.length()} (${puzzles.size})"
                    )
                    //puzzles.clear()
                    //Log.d("getpuzzles","Puzzles size: ${puzzles.size}")

                    val lastpuzzle = puzzlesReceived[puzzlesReceived.length() - 1] as JSONObject
                    last_puzzleID = lastpuzzle.getString("puzzle_id")
                    Log.d("getLastPuzzle", "puzzle_id = $last_puzzleID")

                }
            }
        })

        return last_puzzleID
    }

    var lastSolutionImg = ""
    fun getLastSolutionImg(puzzle: String?) : String {
        val request = Request.Builder()
            .url(serverUrl + "getpieces/" + puzzle + "/")
            .build()

        //var last_puzzleID = ""
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("getpuzzles", "Failed GET request")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("getLastSolutionImg", "Successful GET request")
                    val piecesReceived = try {
                        JSONObject(response.body?.string() ?: "").getJSONArray("puzzles")
                    } catch (e: JSONException) {
                        JSONArray()
                    }
                    Log.d(
                        "getLastSolutionImg",
                        "PuzzlesReceived length: ${piecesReceived.length()} (${pieces.size})"
                    )
                    //puzzles.clear()
                    //Log.d("getpuzzles","Puzzles size: ${puzzles.size}")
                    if (piecesReceived.length() >= 1) {
                        val lastpiece = piecesReceived[piecesReceived.length() - 1] as JSONObject
                        val last_pieceID = lastpiece.getString("puzzle_id")
                        Log.d("getLastSolutionImg", "piece_id = $last_pieceID")

                        lastSolutionImg = lastpiece.getString("solution_img")
                    }
                }
            }
        })
        return lastSolutionImg
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
                    Log.d("deletePuzzle", "DELETE SUCCESS")
                }
            }
        })
    }

    fun deletePiece(id: String?, puzzle_id: String?) {
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
                    Log.d("deletePiece", "DELETE SUCCESS")
                }
            }

        })
    }
}




