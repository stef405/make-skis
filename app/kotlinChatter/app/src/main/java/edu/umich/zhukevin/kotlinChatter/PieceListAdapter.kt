package edu.umich.zhukevin.kotlinChatter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import coil.load
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
            listItemView.root.setBackgroundColor(Color.parseColor(if (position % 2 == 0) "#E0E0E0" else "#EEEEEE"))
            // show image
            imageUrl?.let {
                listItemView.pieceImage.setVisibility(View.VISIBLE)
                listItemView.pieceImage.load(it) {
                    crossfade(true)
                    crossfade(1000)
                }
            } ?: run {
                listItemView.pieceImage.setVisibility(View.GONE)
                listItemView.pieceImage.setImageBitmap(null)
            }

        }
        return listItemView.root
    }

}