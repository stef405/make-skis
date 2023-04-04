package edu.umich.zhukevin.kotlinChatter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import coil.load
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.deletePiece
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityListitemPieceBinding

class PieceListAdapter(context: Context, pieces: List<Piece>) :
    ArrayAdapter<Piece>(context, 0, pieces) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context).inflate(R.layout.activity_listitem_piece, parent, false)
            rowView.tag = ActivityListitemPieceBinding.bind(rowView) // cache binding
            rowView.tag
        }) as ActivityListitemPieceBinding



        getItem(position)?.run {
            // show image
            piece_img?.let {
                listItemView.pieceImage.setVisibility(View.VISIBLE)
                listItemView.pieceImage.load(it) {
                    crossfade(true)
                    crossfade(1000)
                }
            } ?: run {
                listItemView.pieceImage.setVisibility(View.GONE)
                listItemView.pieceImage.setImageBitmap(null)
            }

            //pressing garbage to delete piece
            listItemView.pieceDelete.setOnClickListener {
                deletePiece(piece_id,puzzle_id)
            }

        }
        return listItemView.root
    }

}