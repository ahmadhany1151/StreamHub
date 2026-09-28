package com.example.movieapplication.data

import com.example.movieapp.data.MovieRepository
import com.example.movieapp.model.Movie
import com.example.movieapp.model.MovieResponse
import com.example.movieapp.network.ApiService
import com.example.movieapplication.model.MovieDetails
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MovieRepositoryTest {

    private lateinit var repository: MovieRepository
    @RelaxedMockK
    lateinit var apiService: ApiService

    init {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Before
    fun setup() {
        repository = MovieRepository().apply {
            val field = MovieRepository::class.java.getDeclaredField("apiService")
            field.isAccessible = true
            field.set(this, apiService)
        }
    }

    @Test
    fun `getTrendingMovies returns data when API succeeds`() = runTest {
        val fakeMovies = listOf(
            Movie(
                id = 550,
                title = "Fight Club",
                posterPath = "/path.jpg",
                overview = "",
                voteAverage = 8.8,
                backdropPath = null,
                releaseDate = "1999-10-15",
            )
        )
        val fakeResponse = MovieResponse(results = fakeMovies)

        coEvery { apiService.getTrendingMovies(any()) } returns fakeResponse

        val result = repository.getTrendingMovies()

        assertNotNull(result)
        assertEquals(fakeMovies, result?.results)
        coVerify { apiService.getTrendingMovies(any()) }
    }

    @Test
    fun `getTrendingMovies returns null when API throws exception`() = runTest {
        coEvery { apiService.getTrendingMovies(any()) } throws Exception("Network error")

        val result = repository.getTrendingMovies()

        assertNull(result)
        coVerify { apiService.getTrendingMovies(any()) }
    }

    @Test
    fun `getPopularMovies returns data successfully`() = runTest {
        val fakeResponse = MovieResponse(
            results = listOf(
                Movie(
                    id = 278,
                    title = "The Shawshank Redemption",
                    posterPath = null,
                    overview = "",
                    voteAverage = 9.3,
                    backdropPath = null,
                    releaseDate = "1994-09-23",
                )
            )
        )
        coEvery { apiService.getPopularMovies(any()) } returns fakeResponse

        val result = repository.getPopularMovies()

        assertNotNull(result)
        assertEquals(1, result?.results?.size)
    }

    @Test
    fun `searchMovies returns correct response`() = runTest {
        val query = "inception"
        val expected = MovieResponse(
            results = listOf(
                Movie(
                    id = 27205,
                    title = "Inception",
                    posterPath = null,
                    overview = "",
                    voteAverage = 8.8,
                    backdropPath = null,
                    releaseDate = "2010-07-16",
                )
            )
        )
        coEvery { apiService.searchMovies(any(), query) } returns expected

        val result = repository.searchMovies(query)

        assertEquals(expected, result)
    }

    @Test
    fun `getMovieDetails returns correct MovieDetails`() = runTest {
        val movieId = 550
        val fakeDetails = MovieDetails(
            id = movieId,
            title = "Fight Club",
            overview = "An insomniac office worker...",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            videos = null,
            voteAverage = 8.8,
            backdropPath = null,
            releaseDate = "1999-10-15",
            genres = emptyList(),
            runtime = 139,
            cast = emptyList(),
            crew = emptyList(),
            voteCount = 12345,
            ageRating = "R"
        )

        coEvery { apiService.getMovieDetails(movieId, any(), "videos") } returns fakeDetails

        val result = repository.getMovieDetails(movieId)

        assertEquals(fakeDetails, result)
    }
}