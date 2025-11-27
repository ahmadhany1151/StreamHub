package com.example.movieapp.data

import android.util.Log
import com.example.movieapp.BuildConfig
import com.example.movieapp.model.Celebrity
import com.example.movieapp.model.CelebrityResponse
import com.example.movieapp.model.MovieResponse
import com.example.movieapp.network.ApiService
import com.example.movieapp.network.RetrofitInstance
import com.example.movieapplication.model.CreditsResponse
import com.example.movieapplication.model.MovieDetails
import com.example.movieapplication.model.Video

class MovieRepository {

    private val apiService: ApiService = RetrofitInstance.api

    suspend fun getTrendingMovies(): MovieResponse? {
        return try {
            val response = apiService.getTrendingMovies(BuildConfig.TMDB_API_KEY)
            response
        } catch (e: Exception) {
//            Log.e("MoviesCheck", "Error fetching movies: ${e.message}")
            null
        }
    }

    suspend fun getPopularMovies(): MovieResponse? {
        return try {
            val response = apiService.getPopularMovies(BuildConfig.TMDB_API_KEY)
            response
        } catch (e: Exception) {
            Log.e("MoviesCheck", "Error fetching movies: ${e.message}")
            null
        }
    }

    suspend fun getMovieByGenre(genreId: Int): MovieResponse? {
        return try {
            val response = apiService.getMovieByGenre(
                apiKey = BuildConfig.TMDB_API_KEY,
                genreId = genreId
            )
            response
        } catch (e: Exception) {
            Log.e("MoviesCheck", "Error fetching movies by genre: ${e.message}")
            null
        }
    }

    suspend fun getTrendingCelebrities(): CelebrityResponse? {
        return try {
            val response = apiService.getTrendingCelebrities(BuildConfig.TMDB_API_KEY)
            response
        } catch (e: Exception) {
            Log.e("MoviesCheck", "Error fetching celebrities: ${e.message}")
            null
        }
    }

    suspend fun getCelebrityDetails(personId: Int): Celebrity? {
        return try {
            Log.d("REPO_DEBUG", "🔍 REPOSITORY: Fetching details for personId: $personId")
            val response = apiService.getPersonDetails(personId, BuildConfig.TMDB_API_KEY)
            Log.d("REPO_DEBUG", "🔍 REPOSITORY: Raw API response received")
            Log.d("REPO_DEBUG", "🔍 REPOSITORY - ID: ${response.id}")
            Log.d("REPO_DEBUG", "🔍 REPOSITORY - Name: '${response.name}'")
            Log.d("REPO_DEBUG", "🔍 REPOSITORY - Birthday: '${response.birthday}'")
            Log.d("REPO_DEBUG", "🔍 REPOSITORY - Place of Birth: '${response.place_of_birth}'")
            Log.d("REPO_DEBUG", "🔍 REPOSITORY - Biography: '${response.biography?.take(50)}...'")
            Log.d("REPO_DEBUG", "🔍 REPOSITORY - Known For: '${response.known_for_department}'")

            val celebrity = Celebrity(
                id = response.id,
                name = response.name ?: "Unknown",
                role = response.known_for_department,
                profilePath = response.profile_path,
                birthday = response.birthday,
                placeOfBirth = response.place_of_birth,
                biography = response.biography
            )
            Log.d("REPO_DEBUG", "🔍 REPOSITORY: Successfully converted to Celebrity object")
            celebrity
        } catch (e: Exception) {
            Log.e("REPO_DEBUG", "❌ REPOSITORY: Error fetching celebrity details - ${e.message}", e)
            null
        }
    }

    suspend fun getCelebrityImages(personId: Int): List<String> {
        return try {
            Log.d("REPO_DEBUG", "🖼️ REPOSITORY: Fetching images for personId: $personId")
            val response = apiService.getPersonImages(personId, BuildConfig.TMDB_API_KEY)
            val images = response.profiles?.mapNotNull { it.filePath } ?: emptyList()
            Log.d("REPO_DEBUG", "🖼️ REPOSITORY: Found ${images.size} images")
            images
        } catch (e: Exception) {
            Log.e("REPO_DEBUG", "❌ REPOSITORY: Error fetching celebrity images - ${e.message}", e)
            emptyList()
        }
    }

    suspend fun searchMovies(query: String): MovieResponse {
        return apiService.searchMovies(BuildConfig.TMDB_API_KEY, query)
    }

    suspend fun searchCelebrities(query: String): CelebrityResponse {
        return apiService.searchCelebrities(BuildConfig.TMDB_API_KEY, query)
    }
    suspend fun getMovieCredits(movieId: Int): CreditsResponse {
        return apiService.getMovieCredits(movieId, BuildConfig.TMDB_API_KEY)
    }
    suspend fun getMovieDetails(movieId: Int): MovieDetails {
        return apiService.getMovieDetails(
            movieId = movieId,
            apiKey = BuildConfig.TMDB_API_KEY,
            appendToResponse = "videos" // <- this is the key fix
        )
    }
    suspend fun getMovieVideos(movieId: Int): List<Video> {
        return try {
            val response = RetrofitInstance.api.getMovieVideos(movieId, BuildConfig.TMDB_API_KEY)
            response.results
        } catch (e: Exception) {
            Log.e("MoviesCheck", "Error fetching movie videos: ${e.message}")
            emptyList()
        }
    }
    suspend fun getAgeRating(movieId: Int): String {
        val data = apiService.getMovieAgeRating(movieId, BuildConfig.TMDB_API_KEY)

        // Try to get US first, fallback to any country
        val rating = data.results
            .firstOrNull { it.countryCode == "US" }
            ?.releaseDates
            ?.firstOrNull { it.certification.isNotBlank() }
            ?.certification
            ?: data.results
                .firstNotNullOfOrNull { country ->
                    country.releaseDates.firstOrNull { it.certification.isNotBlank() }?.certification
                }

        return rating ?: "NR"
    }
    suspend fun getMovieDetailsWithAge(movieId: Int): MovieDetails {
        val details = getMovieDetails(movieId) // existing method
        val rating = getAgeRating(movieId)     // fetch age rating

        details.ageRating = rating             // assign it
        return details
    }

}