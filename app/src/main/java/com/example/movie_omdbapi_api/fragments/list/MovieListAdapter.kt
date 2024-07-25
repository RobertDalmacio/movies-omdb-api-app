package com.example.movie_omdbapi_api.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.movie_omdbapi_api.MovieResult
import com.example.movie_omdbapi_api.R

class MovieListAdapter: RecyclerView.Adapter<MovieListAdapter.ViewHolder>() {

    private var movieList = emptyList<MovieResult>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val movieTitle: TextView = itemView.findViewById(R.id.tvMovieTitle)
        val studioName: TextView = itemView.findViewById(R.id.tvStudioName)
        val criticsRating: TextView = itemView.findViewById(R.id.tvCriticsRating)
        val moviePoster: ImageView = itemView.findViewById(R.id.ivMoviePosterList)
        val constLayout: ConstraintLayout = itemView.findViewById(R.id.rowLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.custom_row, parent, false))
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = movieList[position]
        holder.movieTitle.text = currentItem.Title
        holder.studioName.text = currentItem.Type
        holder.criticsRating.text = currentItem.Year
        if (currentItem.Poster == "N/A") {
            holder.moviePoster.setImageResource(R.drawable.default_poster)
        } else {
            holder.moviePoster.load(currentItem.Poster)
        }

        // custom row item
        holder.constLayout.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToDetailsFragment(currentItem)
            // navigate to details fragment
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun setData(newList: List<MovieResult>?) {
        this.movieList = (newList ?: emptyList())
        notifyDataSetChanged()
    }
}