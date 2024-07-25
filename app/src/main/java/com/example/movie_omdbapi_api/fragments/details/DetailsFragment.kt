package com.example.movie_omdbapi_api.fragments.details

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.movie_omdbapi_api.R
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DetailsFragment : Fragment() {

    private val args by navArgs<DetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)
        val currentItem = args.currentMovie

        // fetch current selected id using imdbID passed in as an argument
        fetchMovieData(currentItem!!.imdbID).start()

        return view
    }

    // fetch single movie and update image and text views with response data
    private fun fetchMovieData(movieId: String): Thread {
        return Thread {
            try {
                val url = URL("https://www.omdbapi.com/?apikey=c6149b8e&i=$movieId")
                val connection = url.openConnection() as HttpsURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val searchResponse = Gson().fromJson(response, SearchResponse::class.java)
                    activity?.runOnUiThread {
                        if(searchResponse.Poster == "N/A") {
                            view?.findViewById<ImageView>(R.id.ivPoster)?.setImageResource(R.drawable.default_poster)
                        } else {
                            view?.findViewById<ImageView>(R.id.ivPoster)?.load(searchResponse.Poster)
                        }
                        view?.findViewById<TextView>(R.id.tvTitle)?.text = searchResponse.Title
                        view?.findViewById<TextView>(R.id.tvMainDetails)?.text = "${searchResponse.Year} * ${searchResponse.Rated} * ${searchResponse.Runtime}s"
                        view?.findViewById<TextView>(R.id.tvGenre)?.text = searchResponse.Genre
                        view?.findViewById<TextView>(R.id.tvPlot)?.text = searchResponse.Plot
                        view?.findViewById<TextView>(R.id.tvDirector)?.text = searchResponse.Director
                        view?.findViewById<TextView>(R.id.tvWriter)?.text = searchResponse.Writer
                        view?.findViewById<TextView>(R.id.tvActors)?.text = searchResponse.Actors
                        view?.findViewById<TextView>(R.id.tvAwards)?.text = searchResponse.Awards

                        val imdbRating = searchResponse.Ratings.indexOfFirst { it.Source == "Internet Movie Database"}
                        val rottenTomatoesRating = searchResponse.Ratings.indexOfFirst { it.Source == "Rotten Tomatoes"}
                        val metaCriticRating = searchResponse.Ratings.indexOfFirst { it.Source == "Metacritic"}
                        if (imdbRating !== -1) {
                            view?.findViewById<TextView>(R.id.tvImdbRating)?.text = "IMDb: ${searchResponse.Ratings[imdbRating].Value}"
                        } else {
                            view?.findViewById<TextView>(R.id.tvImdbRating)?.text = "IMDb: N/A"
                        }
                        if (rottenTomatoesRating !== -1) {
                            view?.findViewById<TextView>(R.id.tvRottenTomatoesRating)?.text = "Rotten Tomatoes: ${searchResponse.Ratings[rottenTomatoesRating].Value}"
                        } else {
                            view?.findViewById<TextView>(R.id.tvRottenTomatoesRating)?.text = "Rotten Tomatoes: N/A"
                        }
                        if (metaCriticRating !== -1) {
                            view?.findViewById<TextView>(R.id.tvMetacriticRating)?.text = "Metacritic: ${searchResponse.Ratings[metaCriticRating].Value}"
                        } else {
                            view?.findViewById<TextView>(R.id.tvMetacriticRating)?.text = "Metacritic: N/A"
                        }
                        if (!TextUtils.isEmpty(searchResponse.BoxOffice)) {
                            view?.findViewById<TextView>(R.id.tvBoxOffice)?.text = searchResponse.BoxOffice
                        } else {
                            view?.findViewById<TextView>(R.id.tvBoxOffice)?.text = "N/A"
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "HTTP error code: $responseCode", Toast.LENGTH_SHORT).show()
                }
                connection.disconnect()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class SearchResponse(
        val Title: String,
        val Year: String,
        val Rated: String,
        val Runtime: String,
        val Genre: String,
        val Plot: String,
        val Director: String,
        val Writer: String,
        val Actors: String,
        val Awards: String,
        val Ratings: List<Ratings>,
        val BoxOffice: String,
        val Poster: String,
    )

    data class Ratings(
        val Source: String,
        val Value: String,
    )
}