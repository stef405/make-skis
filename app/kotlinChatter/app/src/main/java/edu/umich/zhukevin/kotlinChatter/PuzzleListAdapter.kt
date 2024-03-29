package edu.umich.zhukevin.kotlinChatter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import coil.load
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.deletePuzzle
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.puzzles
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityListitemPuzzleBinding

class PuzzleListAdapter(context: Context, puzzle: List<Puzzle>) :
    ArrayAdapter<Puzzle>(context, 0, puzzle) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context).inflate(R.layout.activity_listitem_puzzle, parent, false)
            rowView.tag = ActivityListitemPuzzleBinding.bind(rowView) // cache binding
            rowView.tag
        }) as ActivityListitemPuzzleBinding

        getItem(position)?.run {
            // show image
            imageUrl?.let {
                listItemView.puzzleTextView.text = "Puzzle " + puzzle_id
                listItemView.puzzleImage.setVisibility(View.VISIBLE)
                listItemView.puzzleImage.load(it) {
                    crossfade(true)
                    crossfade(1000)
                }
                listItemView.puzzleImage.setOnClickListener{
                    val intent = Intent(context, PieceActivity::class.java)
                    intent.putExtra("puzzle_id",puzzle_id)
                    context.startActivity(intent)
                }
            } ?: run {
                listItemView.puzzleImage.setVisibility(View.GONE)
                listItemView.puzzleImage.setImageBitmap(null)
            }
            //pressing garbage can to delete entry
            listItemView.deleteEntry.setOnClickListener{
                deletePuzzle(this.puzzle_id)
            }

        }

        return listItemView.root
    }

}