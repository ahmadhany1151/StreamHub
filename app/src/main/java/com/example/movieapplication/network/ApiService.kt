package com.example.movieapp.network

import com.example.movieapp.model.MovieResponse
import com.example.movieapp.model.CelebrityResponse
import com.example.movieapplication.model.CreditsResponse
import com.example.movieapplication.model.MovieDetails
import com.example.movieapplication.model.ReleaseDatesResponse
import com.example.movieapplication.model.VideoResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    data class PersonDetailsResponse(
        val id: Int,
        val name: String?,
        val known_for_department: String?,
        val profile_path: String?,
        val birthday: String?,
        val place_of_birth: String?,
        val biography: String?
    )

    data class PersonImagesResponse(
        @SerializedName("profiles") val profiles: List<PersonProfile>? = null
    )

    data class PersonProfile(
        @SerializedName("file_path") val filePath: String? = null
    )
    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String
    ): MovieResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String
    ): MovieResponse

    @GET("discover/movie")
    suspend fun getMovieByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int
    ): MovieResponse

    @GET("trending/person/week")
    suspend fun getTrendingCelebrities(
        @Query("api_key") apiKey: String
    ): CelebrityResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): MovieResponse

    @GET("search/person")
    suspend fun searchCelebrities(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): CelebrityResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): MovieDetails

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): CreditsResponse

    @GET("person/{person_id}")
    suspend fun getPersonDetails(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String
    ): PersonDetailsResponse

    @GET("person/{person_id}/images")
    suspend fun getPersonImages(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String
    ): PersonImagesResponse
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): VideoResponse
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "videos" // <-- add this
    ): MovieDetails

    @GET("movie/{movie_id}/release_dates")
    suspend fun getMovieAgeRating(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): ReleaseDatesResponse
}
