package com.example.movie_omdbapi_api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MovieResult(
    val Title: String,
    val Year: String,
    val imdbID: String,
    val Type: String,
    val Poster: String,
):Parcelable