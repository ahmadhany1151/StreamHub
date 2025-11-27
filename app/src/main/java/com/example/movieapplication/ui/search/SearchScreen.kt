package com.example.movieapp.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.movieapp.R
import com.example.movieapp.model.Celebrity
import com.example.movieapp.model.Movie
import com.example.movieapp.ui.home.HomeViewModel
import com.example.movieapplication.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = viewModel(),
    viewModel: HomeViewModel,
    onBackClick: () -> Unit = {},
    onSeeAllClick: (String) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onCelebrityClick: (Celebrity) -> Unit = {}
) {
    val query by searchViewModel.query
    val suggestions by searchViewModel.suggestions
    val showSuggestions by searchViewModel.showSuggestions
    val movieSearchResults by searchViewModel.movieSearchResults
    val celebSearchResults by searchViewModel.celebSearchResults
    val isLoading by searchViewModel.isLoading
    val errorMsg by searchViewModel.errorMsg
    val popularMovies by searchViewModel.popularMovies.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState(initial = emptyList())

    val celebs by viewModel.trendingCelebrities.collectAsState()

    val showDefaultSections =
        movieSearchResults.isEmpty()
                && celebSearchResults.isEmpty()
                && !isLoading && errorMsg == null

    var favoriteMovies by remember { mutableStateOf(setOf<Int>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 18.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                    )
            ) {



                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 24.dp,
                                bottomEnd = 24.dp
                            )
                        )
                        .background(Color(0xff1c1c1c))
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                        .height(80.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp,
                                    bottomStart = 24.dp,
                                    bottomEnd = 24.dp
                                )
                            )
                    ) {
                        //Search bar
                        OutlinedTextField(
                            value = query,
                            onValueChange = { searchViewModel.onQueryChanged(it) },
                            placeholder = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 30.dp),
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
                                        text = "Start searching",
                                        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                                    )
                                }
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { searchViewModel.clearAll() }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { searchViewModel.performSearch() }
                            ),
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .fillMaxWidth()
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
                                ),
                            shape = RoundedCornerShape(30.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                                unfocusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.Gray
                            ),
                            singleLine = true
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black,
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )





            }

            // Content based on search state
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                errorMsg != null -> {
                    Text(
                        text = errorMsg ?: "",
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        textAlign = TextAlign.Center
                    )
                }

                movieSearchResults.isNotEmpty() || celebSearchResults.isNotEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (movieSearchResults.isNotEmpty()) {
                            Text(
                                text = "Movies",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            movieSearchResults.forEach { movie ->
                                val inWatchlist = watchlist.any { it.id == movie.id }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable { onMovieClick(movie) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val posterUrl = movie.posterPath?.let {
                                        if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
                                        else "https://image.tmdb.org/t/p/w500/$it"
                                    }
                                    Image(
                                        painter = if (posterUrl != null) {
                                            rememberAsyncImagePainter(posterUrl)
                                        } else {
                                            painterResource(id = R.drawable.no_image)
                                        },
                                        contentDescription = movie.title ?: "Movie poster",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = movie.title ?: "Untitled",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "⭐ ${
                                                String.format(
                                                    "%.2f",
                                                    movie.voteAverage ?: 0.0
                                                )
                                            }",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            if (inWatchlist) viewModel.removeFromWatchlist(movie)
                                            else viewModel.addToWatchlist(movie)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (inWatchlist) Icons.Default.Delete else Icons.Default.Add,
                                            contentDescription = if (inWatchlist) "Remove" else "Add",
                                            tint = if (inWatchlist) Color.Red else Color.White
                                        )
                                    }
                                }
                                Divider(color = Color.DarkGray)
                            }
                        }
                        if (celebSearchResults.isNotEmpty()) {
                            Text(
                                text = "Celebrities",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            celebSearchResults.forEach { celebrity ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable { onCelebrityClick(celebrity) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val profileUrl = celebrity.profilePath?.let {
                                        if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
                                        else "https://image.tmdb.org/t/p/w500/$it"
                                    }
                                    Image(
                                        painter = if (profileUrl != null) {
                                            rememberAsyncImagePainter(profileUrl)
                                        } else {
                                            painterResource(id = R.drawable.no_image)
                                        },
                                        contentDescription = celebrity.name ?: "Celebrity",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = celebrity.name ?: "Unknown",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2
                                        )
                                        Text(
                                            text = celebrity.role ?: "Unknown",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Divider(color = Color.DarkGray)
                            }
                        }
                    }
                }

                showDefaultSections -> {

                    // Movies Sections

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C1E))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "Most watched",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "See all",
                                color = Color(0xFFcefc00),
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { onSeeAllClick("movies") }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val itemsToShow = minOf(2, popularMovies.size)
                                repeat(itemsToShow) { index ->
                                    val movie = popularMovies[index]
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(2f / 3f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onMovieClick(movie) }
                                    ) {
                                        val movieId = index + 1
                                        val isFavorite = favoriteMovies.contains(movieId)

                                        val posterUrl = movie.posterPath?.let {
                                            if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
                                            else "https://image.tmdb.org/t/p/w500/$it"
                                        } ?: "https://via.placeholder.com/300x450?text=No+Image"

                                        Image(
                                            painter = rememberAsyncImagePainter(posterUrl),
                                            contentDescription = "movie" ?: null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        IconButton(
                                            onClick = {
                                                favoriteMovies =
                                                    if (favoriteMovies.contains(movieId)) {
                                                        favoriteMovies - movieId
                                                    } else {
                                                        favoriteMovies + movieId
                                                    }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
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
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color.White.copy(alpha = 0.3f),
                                                            Color.White.copy(alpha = 0.1f)
                                                        )
                                                    ),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFavorite) Color.Red else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val itemsToShow = minOf(2, popularMovies.size - 3)
                                repeat(itemsToShow) { i ->
                                    val movie = popularMovies[i + 3]

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(2f / 3f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onMovieClick(movie) }
                                    ) {
                                        val movieId = i + 3
                                        val isFavorite = favoriteMovies.contains(movieId)

                                        val posterUrl = movie.posterPath?.let {
                                            if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
                                            else "https://image.tmdb.org/t/p/w500/$it"
                                        } ?: "https://via.placeholder.com/300x450?text=No+Image"

                                        Image(
                                            painter = rememberAsyncImagePainter(posterUrl),
                                            contentDescription = "Movie poster",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.2f),
                                                            Color.Black.copy(alpha = 0.8f)
                                                        ),
                                                        startY = 0f,
                                                        endY = Float.POSITIVE_INFINITY
                                                    )
                                                )
                                        )

                                        IconButton(
                                            onClick = {
                                                favoriteMovies =
                                                    if (favoriteMovies.contains(movieId)) {
                                                        favoriteMovies - movieId
                                                    } else {
                                                        favoriteMovies + movieId
                                                    }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(40.dp)
                                                .background(
                                                    color = Color.Black.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFavorite) Color.Red else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))


                    // Explore Genres Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C1E))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "✨",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "Explore Genres",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "See all",
                                color = Color(0xFFcefc00),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable { onSeeAllClick("genres") }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(2) { index ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(2f / 3f)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    if (index == 0) {
                                        Image(
                                            painter = rememberAsyncImagePainter(R.drawable.shawshank),
                                            contentDescription = "Movie poster",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onSeeAllClick("genre_18") },
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = rememberAsyncImagePainter(R.drawable.shutter),
                                            contentDescription = "Movie poster",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onSeeAllClick("genre_53") },
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.7f)
                                                    ),
                                                    startY = 200f
                                                )
                                            )
                                    )

                                    Text(
                                        text = if (index == 0) "Drama" else "Thriller",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(2) { index ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(2f / 3f)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    if (index == 0) {
                                        Image(
                                            painter = rememberAsyncImagePainter(R.drawable.oppenheimer),
                                            contentDescription = "Movie poster",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onSeeAllClick("genre_27") },
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = rememberAsyncImagePainter(R.drawable.wolf),
                                            contentDescription = "Movie poster",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onSeeAllClick("genre_35") },
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.7f)
                                                    ),
                                                    startY = 200f
                                                )
                                            )
                                    )

                                    Text(
                                        text = if (index == 0) "Horror" else "Comedy",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //Explore Celebrities Section

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C1E))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🎭",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "Explore Celebrities",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "See all",
                                color = Color(0xFFcefc00),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable { onSeeAllClick("celebrities") }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val itemsToShow = minOf(2, celebs.size)
                                repeat(itemsToShow) { index ->
                                    val celebrity = celebs[index]
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(2f / 3f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onCelebrityClick(celebrity) }
                                            .shadow(
                                                elevation = 4.dp,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        val profileImage = celebrity.profilePath?.let {
                                            if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
                                            else "https://image.tmdb.org/t/p/w500/$it"
                                        } ?: "https://via.placeholder.com/300x450?text=No+Image"

                                        Image(
                                            painter = rememberAsyncImagePainter(profileImage),
                                            contentDescription = celebrity.name ?: "Celebrity",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.7f)
                                                        ),
                                                        startY = 200f
                                                    )
                                                )
                                        )

                                        Text(
                                            text = celebrity.name ?: "Unknown",
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(12.dp),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                if (itemsToShow < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }

                            if (celebs.size > 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val startIndex = 2
                                    val itemsToShow = minOf(2, celebs.size - startIndex)
                                    repeat(itemsToShow) { index ->
                                        val celebrity = celebs[startIndex + index]
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(2f / 3f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onCelebrityClick(celebrity) }
                                                .shadow(
                                                    elevation = 4.dp,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                        ) {
                                            val profileImage = celebrity.profilePath?.let {
                                                if (it.startsWith("/")) "https://image.tmdb.org/t/p/w500$it"
                                                else "https://image.tmdb.org/t/p/w500/$it"
                                            } ?: "https://via.placeholder.com/300x450?text=No+Image"

                                            Image(
                                                painter = rememberAsyncImagePainter(profileImage),
                                                contentDescription = celebrity.name ?: "Celebrity",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color.Transparent,
                                                                Color.Black.copy(alpha = 0.7f)
                                                            ),
                                                            startY = 200f
                                                        )
                                                    )
                                            )

                                            Text(
                                                text = celebrity.name ?: "Unknown",
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(12.dp),
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    if (itemsToShow < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                query.isNotEmpty() && !isLoading && movieSearchResults.isEmpty() && celebSearchResults.isEmpty() && errorMsg == null -> {
                    Text(
                        text = "😔 No results found for \"$query\"",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp)
                    )
                }
            }

            // Spacer for bottom navigation bar
            Spacer(modifier = Modifier.height(85.dp))
        }

        if (showSuggestions && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 18.dp)
                    .padding(top = 90.dp)
                    .zIndex(10f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2C2C2E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchViewModel.onSuggestionClicked(suggestion)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = suggestion,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        if (suggestion != suggestions.last()) {
                            Divider(color = Color.DarkGray, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}