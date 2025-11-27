package com.example.movieapp.ui.home

import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.movieapp.data.local.MovieEntity
import com.example.movieapp.model.Celebrity
import com.example.movieapp.model.Movie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Movie) -> Unit = {},
    onCelebrityClick: (Celebrity) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {},
    onSeeAllClicked: (String) -> Unit = {},
    onCelebSeeAllClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val trendingCelebrities by viewModel.trendingCelebrities.collectAsState()
    val watchlistMovies by viewModel.watchlist.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {
            // 🎬 اللافتة الدعائية
            if (trendingMovies.isNotEmpty()) {
                val topMovie = trendingMovies.last()
                val backdropUrl = topMovie.backdropPath?.let {
                    if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
                    else "https://image.tmdb.org/t/p/w500/$it"
                } ?: "https://via.placeholder.com/500x300?text=No+Image"
                val posterUrl = topMovie.posterPath?.let {
                    if (it.startsWith("/")) "https://image.tmdb.org/t/p/w200$it"
                    else "https://image.tmdb.org/t/p/w200/$it"
                } ?: "https://via.placeholder.com/100x150?text=No+Image"
                val isInWatchlist = watchlistMovies.any { it.id == topMovie.id }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(410.dp)
                        .clip(RoundedCornerShape(bottomEnd = 10.dp, bottomStart = 10.dp))
                        .clickable { onMovieClick(topMovie) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(305.dp)
                            .clickable { onMovieClick(topMovie) }
                    ) {
                        // Trailer Image with top fade
                        Image(
                            painter = rememberAsyncImagePainter(backdropUrl),
                            contentDescription = topMovie.title ?: "Movie banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 1f),
                                            Color.Transparent
                                        ),
                                        startY = 40f,
                                        endY = 300f
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 1.5f))
                                    )
                                )
                        )
                        // Play Button Centered
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(65.dp)
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.15f),
                                                Color.White.copy(alpha = 0.05f)
                                            )
                                        ),
                                        radius = size.minDimension / 2
                                    )
                                }
                                .background(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.3f),
                                            Color.White.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Trailer",
                                tint = Color(0xFFcefc00),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        // Poster Card Layered In Front
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = 18.dp, y = 100.dp)
                                .zIndex(2f)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(posterUrl),
                                contentDescription = topMovie.title ?: "Movie Poster",
                                modifier = Modifier
                                    .size(width = 120.dp, height = 183.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 1.dp, top = 4.dp)
                                    .size(42.dp)
                                    .drawBehind {
                                        // Liquid glass effect - gradient overlay
                                        drawCircle(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.15f),
                                                    Color.White.copy(alpha = 0.05f)
                                                )
                                            ),
                                            radius = size.minDimension / 2
                                        )
                                    }
                                    .background(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.3f),
                                                Color.White.copy(alpha = 0.1f)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (isInWatchlist) {
                                            viewModel.removeFromWatchlist(topMovie)
                                        } else {
                                            viewModel.addToWatchlist(topMovie)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isInWatchlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Toggle Watchlist",
                                    tint = if (isInWatchlist) Color.Red else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        // Title and rating in front of trailer
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = 170.dp, y = 40.dp)
                        ) {
                            Row(modifier = Modifier.width(200.dp)) {
                                Text(
                                    text = topMovie.title ?: "Untitled",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    maxLines = 2
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%.1f", topMovie.voteAverage ?: 0.0),
                                    color = Color.Yellow,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔍 البحث
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Search for Movies, People.. ",
                            style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            color = Color.White
                        )
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .width(300.dp)
                    .align(alignment = CenterHorizontally)
                    .drawBehind {
                        // Liquid glass effect - blur simulation with layers
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            cornerRadius = CornerRadius(30.dp.toPx()),
                            style = Fill
                        )
                    }
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable { onSearchClick() },
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                    unfocusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                    disabledContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray
                ),
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // باقي المحتوى (Trending Movies, Trending Celebrities, My Watchlist) كما هو بدون أي تغيير


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1c1c1c))

            ) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("|", color = Color(0xFFd8fd33), fontSize = 27.sp)
                    Text(
                        "Trending Movies",
                        fontSize = 25.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 2.dp)
                    )
                    TextButton(onClick = { onSeeAllClicked("trendingMovies") }) {
                        Text(
                            "See all",
                            color = Color(0xFFcefc00),
                            fontSize = 15.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }


                LazyRow(modifier = Modifier.padding(top = 65.dp, bottom = 6.dp, start = 15.dp)) {
                    items(trendingMovies) { movie ->
                        val isInWatchlist = watchlistMovies.any { it.id == movie.id }
                        MovieItem(
                            movie = movie,
                            onClick = onMovieClick,
                            onToggleWatchlist = {
                                if (isInWatchlist) {
                                    viewModel.removeFromWatchlist(movie)
                                } else {
                                    viewModel.addToWatchlist(movie)
                                }
                            },
                            isInWatchlist = isInWatchlist
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1c1c1c))
            ) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("|", color = Color(0xFFd8fd33), fontSize = 27.sp)
                    Text(
                        "Trending Celebrities",
                        fontSize = 25.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 2.dp)
                    )
                    TextButton(onClick = { onCelebSeeAllClick("celebrityList") }) {
                        Text(
                            "See all",
                            color = Color(0xFFcefc00),
                            fontSize = 15.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                LazyRow(modifier = Modifier.padding(top = 65.dp, bottom = 6.dp, start = 15.dp)) {
                    items(trendingCelebrities) { celeb ->
                        CelebrityItem(celeb = celeb, onClick = onCelebrityClick)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1c1c1c))
            ) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("|", color = Color(0xFFd8fd33), fontSize = 27.sp)
                    Text(
                        "My Watchlist",
                        fontSize = 25.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 2.dp)
                    )
                    TextButton(onClick = onViewAllClick) {
                        Text("View All", color = Color(0xFFd8fd33), fontSize = 14.sp
                            , textDecoration = TextDecoration.Underline)
                    }
                }

                LazyRow(modifier = Modifier.padding(top = 65.dp, bottom = 6.dp, start = 15.dp)) {
                    items(watchlistMovies.take(3)) { movieEntity ->
                        WatchlistItem(
                            movieEntity = movieEntity,
                            onMovieClick = onMovieClick,
                            onRemoveClick = { movie ->
                                viewModel.removeFromWatchlist(movie)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(85.dp))

        }
    }
}


@Composable
fun MovieItem(
    movie: Movie,
    onClick: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    isInWatchlist: Boolean
) {
    val posterUrl = movie.posterPath?.let {
        if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
        else "https://image.tmdb.org/t/p/w500/$it"
    } ?: "https://via.placeholder.com/300x450?text=No+Image"
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp, start = 5.dp)
            .width(140.dp)
    ) {
        Column(
            modifier = Modifier.clickable { onClick(movie) }
        ) {
            Image(
                painter = rememberAsyncImagePainter(posterUrl),
                contentDescription = movie.title ?: "Movie Poster",
                modifier = Modifier
                    .height(190.dp)
                    .width(130.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = movie.title ?: "Untitled",
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                text = "⭐ ${String.format("%.1f", movie.voteAverage ?: 0.0)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 15.dp, top = 3.dp, bottom = 55.dp)
                .size(40.dp)
                .drawBehind {
                    // Liquid glass effect - gradient overlay
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable { onToggleWatchlist(movie) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isInWatchlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Toggle Watchlist",
                tint = if (isInWatchlist) Color.Red else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun CelebrityItem(celeb: Celebrity, onClick: (Celebrity) -> Unit) {
    val profileUrl = celeb.profilePath?.let {
        if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
        else "https://image.tmdb.org/t/p/w500/$it"
    } ?: "https://via.placeholder.com/100x100?text=No+Image"
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(100.dp)
            .clickable { onClick(celeb) }
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(profileUrl),
            contentDescription = celeb.name ?: "Celebrity",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = celeb.name?.let {
                if (it.length > 12) {
                    it.substring(0, 12) + "..."
                } else {
                    it
                }
            } ?: "Unknown",
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1
        )

        Text(
            text = celeb.role ?: "N/A",
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
fun WatchlistItem(
    movieEntity: MovieEntity,
    onMovieClick: (Movie) -> Unit,
    onRemoveClick: (Movie) -> Unit
) {
    val posterUrl = movieEntity.posterPath?.let {
        if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
        else "https://image.tmdb.org/t/p/w500/$it"
    } ?: "https://via.placeholder.com/300x450?text=No+Image"

    val movieFromEntity = Movie(
        id = movieEntity.id,
        title = movieEntity.title ?: "Untitled",
        posterPath = movieEntity.posterPath,
        backdropPath = null,
        voteAverage = movieEntity.voteAverage ?: 0.0,
        overview = movieEntity.overview,
        releaseDate = null
    )
    Box(
        modifier = Modifier
            .padding(8.dp)
            .width(120.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(posterUrl),
            contentDescription = movieEntity.title ?: "Watchlist Movie",
            modifier = Modifier
                .height(190.dp)
                .width(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onMovieClick(movieFromEntity) },
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .size(40.dp)
                .drawBehind {
                    // Liquid glass effect - gradient overlay
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable { onRemoveClick(movieFromEntity) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Remove from Watchlist",
                tint = Color.Red,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
