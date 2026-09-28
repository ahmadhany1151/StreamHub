package com.example.movieapp.ui.home

import app.cash.turbine.test
import com.example.movieapp.data.MovieRepository
import com.example.movieapp.data.local.MovieEntity
import com.example.movieapp.data.local.WatchlistRepository
import com.example.movieapp.model.Celebrity
import com.example.movieapp.model.CelebrityResponse
import com.example.movieapp.model.Movie
import com.example.movieapp.model.MovieResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val movieRepository: MovieRepository = mockk(relaxed = true)
    private val watchlistRepository: WatchlistRepository = mockk(relaxed = true)

    private val fakeMovie = Movie(
        id = 550,
        title = "Fight Club",
        posterPath = "/path.jpg",
        voteAverage = 8.8,
        overview = "Great movie",
        backdropPath = null,
        releaseDate = "1999-10-15"
    )

    private val fakeCelebrities = listOf(
        Celebrity(id = 1, name = "Brad Pitt", profilePath = "/brad.jpg", role = "Actor")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads trending movies and celebrities successfully`() = runTest(testDispatcher) {
        coEvery { movieRepository.getTrendingMovies() } returns MovieResponse(results = listOf(fakeMovie))
        coEvery { movieRepository.getTrendingCelebrities() } returns CelebrityResponse(results = fakeCelebrities)

        val viewModel = HomeViewModel(movieRepository, watchlistRepository)

        viewModel.trendingMovies.test {
            assertEquals(listOf(fakeMovie), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.trendingCelebrities.test {
            assertEquals(fakeCelebrities, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addToWatchlist adds movie when not in watchlist and shows success message`() = runTest(testDispatcher) {
        coEvery { watchlistRepository.isMovieInWatchlist(550) } returns false

        val viewModel = HomeViewModel(movieRepository, watchlistRepository)
        viewModel.addToWatchlist(fakeMovie)

        coVerify { watchlistRepository.addMovie(any()) }

        viewModel.snackbarMessage.test {
            assertEquals("✅ Added to Watchlist: Fight Club", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addToWatchlist shows warning when movie already exists`() = runTest(testDispatcher) {
        coEvery { watchlistRepository.isMovieInWatchlist(550) } returns true

        val viewModel = HomeViewModel(movieRepository, watchlistRepository)
        viewModel.addToWatchlist(fakeMovie)

        coVerify(exactly = 0) { watchlistRepository.addMovie(any()) }

        viewModel.snackbarMessage.test {
            assertEquals("⚠\uFE0F Movie already in Watchlist", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeFromWatchlist removes movie and shows message`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(movieRepository, watchlistRepository)
        viewModel.removeFromWatchlist(fakeMovie)

        coVerify { watchlistRepository.removeMovie(any()) }

        viewModel.snackbarMessage.test {
            assertEquals("❌ Removed from Watchlist: Fight Club", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSnackbarMessage resets message to null`() = runTest(testDispatcher) {
        coEvery { watchlistRepository.isMovieInWatchlist(550) } returns false

        val viewModel = HomeViewModel(movieRepository, watchlistRepository)
        viewModel.addToWatchlist(fakeMovie)

        viewModel.snackbarMessage.test {
            assertEquals("✅ Added to Watchlist: Fight Club", awaitItem())
            viewModel.clearSnackbarMessage()
            assertEquals(null, awaitItem()) // Only here we DO expect null because we called clear
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `watchlist flow is correctly exposed from repository`() = runTest(testDispatcher) {
        val expectedEntity = MovieEntity(
            id = 550,
            title = "Fight Club",
            posterPath = "/path.jpg",
            voteAverage = 8.8,
            overview = "Great movie",
        )

        coEvery { watchlistRepository.getAllMovies() } returns flowOf(listOf(expectedEntity))

        val viewModel = HomeViewModel(movieRepository, watchlistRepository)

        viewModel.watchlist.test {
            assertEquals(listOf(expectedEntity), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}