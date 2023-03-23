package edu.umich.zhukevin.kotlinChatter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import coil.load
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityListitemPuzzleBinding

class PuzzleListAdapter(context: Context, pieces: List<Puzzle>) :
    ArrayAdapter<Puzzle>(context, 0, pieces) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context).inflate(R.layout.activity_listitem_piece, parent, false)
            rowView.tag = ActivityListitemPuzzleBinding.bind(rowView) // cache binding
            rowView.tag
        }) as ActivityListitemPuzzleBinding

        getItem(position)?.run {
            listItemView.root.setBackgroundColor(Color.parseColor(if (position % 2 == 0) "#E0E0E0" else "#EEEEEE"))
            // show image
            imageUrl?.let {
                listItemView.puzzleImage.setVisibility(View.VISIBLE)
                listItemView.puzzleImage.load(it) {
                    crossfade(true)
                    crossfade(1000)
                }
            } ?: run {
                listItemView.puzzleImage.setVisibility(View.GONE)
                listItemView.puzzleImage.setImageBitmap(null)
            }

        }
        return listItemView.root
    }

}