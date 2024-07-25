package com.example.movie_omdbapi_api.fragments.list

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_omdbapi_api.MovieResult
import com.example.movie_omdbapi_api.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ListFragment : Fragment() {

    private var currentPage = 1
    private var maxPageCount = 100
    private var minPageCount = 1
    private val sharedPreferences by lazy {
        requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_list, container, false)

        val adapter = MovieListAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val savedSearchValue = sharedPreferences.getString("searchValue", "")

        // when navigating back from details fragment, fetch movie data using saved search value and currentPage count
        if (!TextUtils.isEmpty(savedSearchValue)) {
            fetchMovieData(savedSearchValue.toString(), currentPage.toString()).start()
            view.findViewById<FloatingActionButton>(R.id.fabPageRight).visibility = View.VISIBLE
            if (currentPage == 1) {
                view.findViewById<FloatingActionButton>(R.id.fabPageLeft).visibility = View.GONE
            }
        // on initial load, hide page right and left buttons
        } else {
            view.findViewById<FloatingActionButton>(R.id.fabPageLeft).visibility = View.GONE
            view.findViewById<FloatingActionButton>(R.id.fabPageRight).visibility = View.GONE
        }

        // search button
        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val searchValue = view.findViewById<EditText>(R.id.tvSearchTitle).text
            if (!TextUtils.isEmpty(searchValue)) {
                fetchMovieData(searchValue.toString(), minPageCount.toString()).start()
                view.findViewById<FloatingActionButton>(R.id.fabPageRight).visibility = View.VISIBLE
                currentPage = minPageCount
                // save text value
                saveValue()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please ensure search field is filled out.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // page right button
        view.findViewById<FloatingActionButton>(R.id.fabPageRight).setOnClickListener {
            val searchValue = view.findViewById<EditText>(R.id.tvSearchTitle).text
            if (!TextUtils.isEmpty(searchValue) && currentPage < maxPageCount) {
                currentPage++
                fetchMovieData(searchValue.toString(), currentPage.toString()).start()
                view.findViewById<FloatingActionButton>(R.id.fabPageLeft).visibility = View.VISIBLE
            } else if (currentPage == maxPageCount){
                currentPage = minPageCount
                fetchMovieData(searchValue.toString(), currentPage.toString()).start()
                view.findViewById<FloatingActionButton>(R.id.fabPageLeft).visibility = View.GONE
            } else if (TextUtils.isEmpty(searchValue)) {
                clearList()
            }
        }

        // page left button
        view.findViewById<FloatingActionButton>(R.id.fabPageLeft).setOnClickListener {
            val searchValue = view.findViewById<EditText>(R.id.tvSearchTitle).text
            if (!TextUtils.isEmpty(searchValue) && currentPage > (minPageCount + 1)) {
                currentPage--
                fetchMovieData(searchValue.toString(), currentPage.toString()).start()
            } else if (currentPage == (minPageCount + 1)) {
                currentPage = minPageCount
                fetchMovieData(searchValue.toString(), currentPage.toString()).start()
                view.findViewById<FloatingActionButton>(R.id.fabPageLeft).visibility = View.GONE
            } else if (TextUtils.isEmpty(searchValue)) {
                clearList()
            }
        }

        // clear button
        view.findViewById<ImageButton>(R.id.btnClear).setOnClickListener {
           clearList()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedValue = sharedPreferences.getString("searchValue", "")
        view?.findViewById<EditText>(R.id.tvSearchTitle)?.setText(savedValue)
    }

    // function to save current search value to sharedPreferences
    private fun saveValue() {
        val textValue = view?.findViewById<EditText>(R.id.tvSearchTitle)?.text?.toString()
        sharedPreferences.edit().putString("searchValue", textValue).apply()
        Toast.makeText(requireContext(), "Movies Found!", Toast.LENGTH_SHORT).show()
    }

    // function to clear current search value and sharedPreferences, as well as set recycler view to an
    // empty list and hide page right and left buttons
    private fun clearList() {
        view?.findViewById<EditText>(R.id.tvSearchTitle)?.text?.clear()
        val adapter = view?.findViewById<RecyclerView>(R.id.rv)?.adapter as? MovieListAdapter
        adapter?.setData(emptyList())
        view?.findViewById<FloatingActionButton>(R.id.fabPageLeft)?.visibility = View.GONE
        view?.findViewById<FloatingActionButton>(R.id.fabPageRight)?.visibility = View.GONE
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    // fetch movie from ombdb api
    private fun fetchMovieData(searchValue: String, page: String): Thread {
        return Thread {
            try {
                val url = URL("https://www.omdbapi.com/?apikey=c6149b8e&s=$searchValue&page=$page")
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

                    // if query returns, no more results - alert user and do nothing
                    if (searchResponse.Search == null) {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "No More Movies Found!", Toast.LENGTH_SHORT).show()
                        }
                    // update recycler view with response data
                    } else {
                        activity?.runOnUiThread {
                            val adapter =
                                view?.findViewById<RecyclerView>(R.id.rv)?.adapter as? MovieListAdapter
                            adapter?.setData(searchResponse.Search)
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "HTTP error code: $responseCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                connection.disconnect()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error fetching data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    data class SearchResponse(
        val Search: List<MovieResult>,
    )
}
