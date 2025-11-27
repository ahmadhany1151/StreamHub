package com.example.movieapp.ui.navigation

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.movieapp.data.MovieRepository
import com.example.movieapp.model.Movie
import com.example.movieapp.model.Celebrity
import com.example.movieapp.data.local.MovieEntity
import com.example.movieapp.ui.details.MovieDetailsScreen
import com.example.movieapp.ui.celebrity.CelebrityDetailsScreen
import com.example.movieapp.ui.home.HomeScreen
import com.example.movieapp.ui.home.HomeViewModel
import com.example.movieapp.ui.profile.ProfileScreen
import com.example.movieapp.ui.search.SearchScreen
import com.example.movieapp.ui.settings.AccountSettingsScreen
import com.example.movieapp.ui.watchlist.WatchlistScreen
import com.example.movieapplication.ui.Login.LoginScreen
import com.example.movieapplication.ui.details.CelebrityListScreen
import com.example.movieapplication.ui.details.Genre
import com.example.movieapplication.ui.details.GenresScreen
import com.example.movieapplication.ui.details.MovieGridScreen
import com.example.movieapplication.ui.viewmodel.SearchViewModel
import com.google.gson.Gson
import com.example.movieapplication.ui.details.SeeAllScreen


val genreList = listOf(
    Genre(28, "Action"),
    Genre(12, "Adventure"),
    Genre(16, "Animation"),
    Genre(35, "Comedy"),
    Genre(80, "Crime"),
    Genre(99, "Documentary"),
    Genre(18, "Drama"),
    Genre(10751, "Family"),
    Genre(14, "Fantasy"),
    Genre(36, "History"),
    Genre(27, "Horror"),
    Genre(10402, "Music"),
    Genre(9648, "Mystery"),
    Genre(10749, "Romance"),
    Genre(878, "Science Fiction"),
    Genre(53, "Thriller"),
    Genre(10770, "TV Movie"),
    Genre(10752, "War"),
    Genre(37, "Western")
)

@Composable
fun AppNavigation(
    viewModel: HomeViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {} // This parameter receives the callback from MainActivity
) {
    val gson = Gson()

    val trendingMovies: List<Movie> =
        viewModel.trendingMovies.collectAsState(initial = emptyList()).value ?: emptyList()

    val trendingCelebrities: List<Celebrity> =
        viewModel.trendingCelebrities.collectAsState(initial = emptyList()).value ?: emptyList()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {

        // ---------------- LOGIN ----------------
        composable("login") {
            LoginScreen(
                onLoginClick = { _, _ ->
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate("signup") },
                onForgotPasswordClick = { }
            )
        }

        // ---------------- HOME ----------------
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onMovieClick = { movie ->
                    val encoded = Uri.encode(gson.toJson(movie))
                    navController.navigate("details/$encoded")
                },
                onCelebrityClick = { celebrity ->
                    val encodedName = Uri.encode(celebrity.name)
                    val encodedProfilePath = Uri.encode(celebrity.profilePath ?: "")
                    navController.navigate("celebrityDetails/${celebrity.id}?name=$encodedName&profilePath=$encodedProfilePath")
                },
                onSearchClick = { navController.navigate("search") },
                onViewAllClick = { navController.navigate("watchlist") },
                onSeeAllClicked = {
                    val encoded = Uri.encode(gson.toJson(trendingMovies))
                    navController.navigate("SeeAllScreen?movieList=$encoded")
                },
                onCelebSeeAllClick = {
                    val encoded = Uri.encode(gson.toJson(trendingCelebrities))
                    navController.navigate("allCelebrities?celebrityList=$encoded")
                },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // ---------------- SEE ALL MOVIES ----------------
        composable(
            "SeeAllScreen?movieList={movieList}",
            arguments = listOf(navArgument("movieList") {
                type = NavType.StringType
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            val json = Uri.decode(backStackEntry.arguments?.getString("movieList") ?: "[]")
            val movies: List<Movie> = runCatching {
                gson.fromJson(json, Array<Movie>::class.java)?.toList() ?: emptyList()
            }.getOrDefault(emptyList())

            SeeAllScreen(
                movies = movies,
                onMovieClick = { movie ->
                    navController.navigate("details/${Uri.encode(gson.toJson(movie))}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ---------------- SEE ALL CELEBRITIES ----------------
        composable(
            "allCelebrities?celebrityList={celebrityList}",
            arguments = listOf(navArgument("celebrityList") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val json = Uri.decode(backStackEntry.arguments?.getString("celebrityList") ?: "[]")
            val celebrities: List<Celebrity> = runCatching {
                gson.fromJson(json, Array<Celebrity>::class.java)?.toList() ?: emptyList()
            }.getOrDefault(emptyList())

            CelebrityListScreen(
                celebrities = celebrities,
                onCelebrityClick = { celebrity ->
                    val encodedName = Uri.encode(celebrity.name)
                    val encodedProfilePath = Uri.encode(celebrity.profilePath ?: "")
                    navController.navigate("celebrityDetails/${celebrity.id}?name=$encodedName&profilePath=$encodedProfilePath")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ---------------- MOVIE DETAILS ----------------
        composable(
            "details/{movieJson}",
            arguments = listOf(navArgument("movieJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val decoded = Uri.decode(backStackEntry.arguments?.getString("movieJson") ?: "")
            val movie = decodeMovieJson(decoded, gson) ?: return@composable

            MovieDetailsScreen(
                movie = movie,
                navController = navController,      // must pass navController here
                onBackClick = { navController.popBackStack() }
            )
        }


        // ---------------- SEARCH ----------------
        composable("search") {
            SearchScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie ->
                    navController.navigate("details/${Uri.encode(gson.toJson(movie))}")
                },
                onCelebrityClick = { cel ->
                    val encodedName = Uri.encode(cel.name)
                    val encodedProfilePath = Uri.encode(cel.profilePath ?: "")
                    navController.navigate("celebrityDetails/${cel.id}?name=$encodedName&profilePath=$encodedProfilePath")
                },
                onSeeAllClick = { type ->
                    if (type.startsWith("genre_")) {
                        val genreId = type.removePrefix("genre_").toInt()
                        navController.navigate("moviesByGenre/$genreId")
                    } else {
                        navController.navigate("SeeAllScreen?contentType=$type")
                    }
                }
            )
        }

        // ---------------- WATCHLIST ----------------
        composable("watchlist") {
            val wl = viewModel.watchlist.collectAsState(initial = emptyList()).value

            WatchlistScreen(
                watchlist = wl,
                onBackClick = { navController.popBackStack() },
                onMovieClick = { entity ->
                    navController.navigate("details/${Uri.encode(gson.toJson(entity))}")
                },
                onRemoveClick = { entity ->
                    val movie = Movie(
                        id = entity.id,
                        title = entity.title,
                        overview = entity.overview,
                        posterPath = entity.posterPath,
                        voteAverage = entity.voteAverage,
                        backdropPath = null,
                        releaseDate = null
                    )
                    viewModel.removeFromWatchlist(movie)
                }
            )
        }

        // ---------------- CONTENT TYPE (MOVIES / GENRES) ----------------
        composable(
            "SeeAllScreen?contentType={contentType}",
            arguments = listOf(navArgument("contentType") {
                type = NavType.StringType
                defaultValue = "movies"
            })
        ) { backStackEntry ->
            val contentType = backStackEntry.arguments?.getString("contentType") ?: "movies"
            val searchVM: SearchViewModel = viewModel()

            when (contentType) {
                "movies" -> {
                    MovieGridScreen(
                        genreId = 0,
                        searchViewModel = searchVM,
                        onMovieClick = { movie ->
                            navController.navigate("details/${Uri.encode(gson.toJson(movie))}")
                        }
                    )
                }

                "genres" -> {
                    GenresScreen(
                        genres = genreList,
                        onGenreSelected = { g ->
                            navController.navigate("moviesByGenre/${g.id}")
                        }
                    )
                }

                "celebrities" -> {
                    CelebrityListScreen(
                        celebrities = trendingCelebrities,
                        onCelebrityClick = { celebrity ->
                            val encodedName = Uri.encode(celebrity.name)
                            val encodedProfilePath = Uri.encode(celebrity.profilePath ?: "")
                            navController.navigate("celebrityDetails/${celebrity.id}?name=$encodedName&profilePath=$encodedProfilePath")
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        // ---------------- MOVIES BY GENRE ----------------
        composable(
            "moviesByGenre/{genreId}",
            arguments = listOf(navArgument("genreId") { type = NavType.IntType })
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getInt("genreId") ?: 0
            val searchVM: SearchViewModel = viewModel()

            MovieGridScreen(
                genreId = genreId,
                searchViewModel = searchVM,
                onMovieClick = { movie ->
                    navController.navigate("details/${Uri.encode(gson.toJson(movie))}")
                }
            )
        }

        // ---------------- CELEBRITY DETAILS ----------------
        composable(
            "celebrityDetails/{celebrityId}?name={name}&profilePath={profilePath}",
            arguments = listOf(
                navArgument("celebrityId") { type = NavType.IntType },
                navArgument("name") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("profilePath") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val celebrityId = backStackEntry.arguments?.getInt("celebrityId") ?: 0
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "Loading...")
            val profilePath = backStackEntry.arguments?.getString("profilePath")?.let {
                if (it.isNotBlank()) Uri.decode(it) else null
            }

            println("🎯 NAVIGATION: Creating celebrity with ID: $celebrityId, Name: '$name', ProfilePath: '$profilePath'")

            if (celebrityId == 0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load celebrity", color = Color.White)
                }
                return@composable
            }

            val repository = viewModel.movieRepository

            val basicCelebrity = Celebrity(
                id = celebrityId,
                name = name,
                profilePath = profilePath
            )

            CelebrityDetailsScreen(
                basicCelebrity = basicCelebrity,
                repository = repository,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ---------------- PROFILE ----------------
        composable("profile") {
            val context = LocalContext.current

            ProfileScreen(
                onNavigateToSettings = { navController.navigate("account_settings") },
                onNavigateToCelebrity = { celebrityId ->
                    try {
                        Log.d("NAVIGATION", "Navigating to celebrity details with ID: $celebrityId")

                        if (celebrityId.isNotEmpty()) {
                            navController.navigate("celebrityDetails/$celebrityId")
                        } else {
                            Log.e("NAVIGATION", "Empty celebrity ID")
                            Toast.makeText(context, "Invalid celebrity", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("NAVIGATION", "Error navigating to celebrity details: ${e.message}")
                        Toast.makeText(
                            context,
                            "Navigation error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onNavigateToGenre = { genre ->
                    Toast.makeText(context, "Clicked genre: $genre", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable(
            "celebrityDetails/{celebrityId}?name={name}&profilePath={profilePath}",
            arguments = listOf(
                navArgument("celebrityId") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType; nullable = true },
                navArgument("profilePath") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val celebrityId = backStackEntry.arguments?.getInt("celebrityId") ?: 0
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "Loading...")
            val profilePath = backStackEntry.arguments?.getString("profilePath")?.let {
                if (it.isNotBlank()) Uri.decode(it) else null
            }

            val repository = viewModel.movieRepository
            val basicCelebrity = Celebrity(
                id = celebrityId,
                name = name,
                profilePath = profilePath
            )

            CelebrityDetailsScreen(
                basicCelebrity = basicCelebrity,
                repository = repository,
                onBackClick = { navController.popBackStack() }
            )
        }


        // ---------------- ACCOUNT SETTINGS ----------------
        composable("account_settings") {
            AccountSettingsScreen(
                onLogout = onLogout ,// FIXED: Now calls the MainActivity's handleLogout() function
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

// ---------------- JSON DECODER ----------------
private fun decodeMovieJson(json: String, gson: Gson): Movie? {
    return try {
        gson.fromJson(json, Movie::class.java)
    } catch (e: Exception) {
        try {
            val entity = gson.fromJson(json, MovieEntity::class.java)
            Movie(
                id = entity.id,
                title = entity.title,
                overview = entity.overview,
                posterPath = entity.posterPath,
                voteAverage = entity.voteAverage,
                backdropPath = null,
                releaseDate = null
            )
        } catch (e: Exception) {
            null
        }
    }
}