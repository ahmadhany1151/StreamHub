package com.example.movieapp.ui.details

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.movieapp.R
import com.example.movieapp.data.MovieRepository
import com.example.movieapp.data.local.AppDatabase
import com.example.movieapp.data.local.MovieEntity
import com.example.movieapp.model.CastMember
import com.example.movieapp.model.Movie
import com.example.movieapplication.model.CrewMember
import com.example.movieapplication.model.Genre
import com.example.movieapplication.model.MovieDetails
import kotlinx.coroutines.launch

@Composable
fun MovieDetailsScreen(
    movie: Any,
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    onPlayTrailer: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }


    val title = when (movie) {
        is Movie -> movie.title ?: "Unknown Title"
        is MovieEntity -> movie.title
        else -> "Unknown Title"
    }
    val posterPathProp = when (movie) {
        is Movie -> movie.posterPath
        is MovieEntity -> movie.posterPath
        else -> null
    }
    val voteAverageProp = when (movie) {
        is Movie -> movie.voteAverage
        is MovieEntity -> movie.voteAverage
        else -> null
    }
    val overviewProp = when (movie) {
        is Movie -> movie.overview
        is MovieEntity -> movie.overview
        else -> null
    }
    val id = when (movie) {
        is Movie -> movie.id
        is MovieEntity -> movie.id
        else -> 0
    }

    var movieDetails by remember { mutableStateOf<MovieDetails?>(null) }
    var castList by remember { mutableStateOf<List<CastMember>>(emptyList()) }
    var crewList by remember { mutableStateOf<List<CrewMember>>(emptyList()) }
    var addedToPlaylist by remember { mutableStateOf(movie is MovieEntity) }
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        try {
            val repo = MovieRepository()
            movieDetails = repo.getMovieDetails(id)
            val detailsWithAge = repo.getMovieDetailsWithAge(id)
            movieDetails = detailsWithAge
            val credits = repo.getMovieCredits(id)
            castList = credits.cast
            crewList = credits.crew
        } catch (_: Exception) {
        }

        try {
            addedToPlaylist = db.watchlistDao().isMovieInWatchlist(id)
        } catch (_: Exception) {
        }
    }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        color = Color(0xFF080808)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            DetailsBackdropSection(
                details = movieDetails,
                navController = navController,
                posterPathFallback = posterPathProp,

                onPlayTrailer = { trailerUrl ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl))
                    context.startActivity(intent)
                },
                onBackClick = onBackClick
            )


            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------
            // POSTER + OVERVIEW ROW
            // -------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Poster
                val posterUrl =
                    movieDetails?.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
                        ?: posterPathProp?.let { "https://image.tmdb.org/t/p/w500$it" }
                        ?: "https://via.placeholder.com/120x180?text=No+Poster"
                Image(
                    painter = rememberAsyncImagePainter(posterUrl),
                    contentDescription = "Movie Poster",
                    modifier = Modifier
                        .size(width = 120.dp, height = 180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Overview and rating
                var expanded by remember { mutableStateOf(false) }
                val overviewText = overviewProp ?: movieDetails?.overview ?: "No overview available."
                val previewLimit = 150 // number of characters before showing "Read more"

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!expanded && overviewText.length > previewLimit)
                            overviewText.take(previewLimit) + "..."
                        else
                            overviewText,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )

                    // Show read more / read less only if text is longer than limit
                    if (overviewText.length > previewLimit) {
                        Text(
                            text = if (expanded) "Read less" else "Read more",
                            color = Color(0xFFFFD54F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { expanded = !expanded }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))


                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------
            // GENRES ROW
            // -------------------------------
            val genres = movieDetails?.genres?.take(3) ?: listOf(
                Genre(0, "Action"),
                Genre(1, "Drama"),
                Genre(2, "Comedy")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(genres) { genre ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2c2c2c), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = genre.name ?: "Unknown",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (!addedToPlaylist) {
                            try {
                                db.watchlistDao().addMovie(
                                    MovieEntity(
                                        id = id,
                                        title = title,
                                        posterPath = posterPathProp,
                                        voteAverage = voteAverageProp,
                                        overview = overviewProp
                                    )
                                )
                                addedToPlaylist = true
                                showSnackbar = true
                            } catch (_: Exception) {
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCFF00)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (addedToPlaylist) "✔ In Playlist" else "+ Add to playlist",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }



            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------
            // CAST & CREW SECTION
            // -------------------------------
            if (castList.isNotEmpty() || crewList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1c1c1c))
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Title row with yellow bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        ) {
                            Text("|", color = Color(0xFFd8fd33), fontSize = 27.sp)
                            Text(
                                text = "Cast & Crew",
                                fontSize = 25.sp,
                                color = Color.White,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        // Cast LazyRow
                        if (castList.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(castList.take(10)) { castMember ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(100.dp)
                                            .clickable {
                                                val encodedName =
                                                    Uri.encode(castMember.name ?: "Unknown")
                                                val encodedProfile =
                                                    Uri.encode(castMember.profile_path ?: "")
                                                navController.navigate(
                                                    "celebrityDetails/${castMember.id}?name=$encodedName&profilePath=$encodedProfile"
                                                )
                                            }
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = castMember.profile_path?.let { "https://image.tmdb.org/t/p/w200$it" }
                                                    ?: R.drawable.no_image
                                            ),
                                            contentDescription = castMember.name,
                                            modifier = Modifier
                                                .size(width = 100.dp, height = 150.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(Modifier.height(6.dp))

                                        Text(
                                            text = castMember.name ?: "Unknown",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = castMember.character ?: "",
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                        }

                        Divider(thickness = .3.dp, color =  Color(0xFF2D2D2D))

// Crew summary
                        val ageRating = movieDetails?.ageRating ?: "NR"

                        val directors = crewList.filter { it.job.equals("Director", true) }
                        val writers = crewList.filter { jobIsWriter(it.job) }.take(1)

                        if (directors.isNotEmpty() || writers.isNotEmpty()) {

                            // --- DIRECTOR ROW (inside padding) ---
                            if (directors.isNotEmpty()) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Director: ",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = directors.joinToString(", ") { it.name ?: "" },
                                            color = Color.LightGray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            // --- FULL WIDTH DIVIDER (NOT inside padding) ---
                            if (directors.isNotEmpty() && writers.isNotEmpty()) {
                                Divider(thickness = .3.dp, color =  Color(0xFF2D2D2D))
                            }

                            // --- WRITER ROW (inside padding) ---
                            if (writers.isNotEmpty()) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Writer: ",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = writers.joinToString(", ") { it.name ?: "" },
                                            color = Color.LightGray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            // bottom divider full width
                            Divider(thickness = .3.dp, color =  Color(0xFF2D2D2D))
                        }
                        // Bottom row: Age Rating • Rate this • Rating box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 🔹 Column 1 — Age Rating
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Icon(

                                        Icons.Default.Shield,
                                        contentDescription = "Back",
                                        tint = Color(0xFF00D17B),
                                        modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = "Age",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = ageRating ?: "NR",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 🔹 Column 2 — Rate This Button
                            var showRatingDialog by remember { mutableStateOf(false) }
                            var userRating by remember { mutableStateOf(0) }
                            Button(
                                onClick = { showRatingDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCEFC00)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                Text(text = "Rate This", color = Color(0xFF080808), fontSize = 18.sp)
                            }

                            if (showRatingDialog) {
                                Dialog(onDismissRequest = { showRatingDialog = false }) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        // 1️⃣ Full-screen blurred background
                                        Image(
                                            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movieDetails?.posterPath}"),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .blur(10.dp)
                                        )

                                        // Semi-transparent overlay on top of blur
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFF2D2D2D).copy(alpha = 0.4f))
                                        )

                                        // Foreground UI (poster, stars, X button) remains sharp
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            // X Button
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .align(Alignment.End)
                                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                                    .clickable { showRatingDialog = false },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Big poster
                                            val posterUrl = movieDetails?.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
                                            Image(
                                                painter = rememberAsyncImagePainter(posterUrl),
                                                contentDescription = "Poster",
                                                modifier = Modifier
                                                    .size(200.dp, 300.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )

                                            Spacer(modifier = Modifier.height(24.dp))

                                            // Stars Row
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                (1..5).forEach { index ->
                                                    Icon(
                                                        imageVector = Icons.Default.Star,               // <-- Material icon
                                                        contentDescription = "Star $index",
                                                        tint = if (index <= userRating) Color(
                                                            0xFFFFEB3B
                                                        ) else Color.Gray,
                                                        modifier = Modifier
                                                            .size(28.dp)                                // adjust size here
                                                            .clickable { userRating = index }
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Optional: display rating text
                                            Text(
                                                text = "Your Rating: $userRating/5",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // 🔹 Column 3 — Rating + Votes
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                // ⭐ Star icon
                                Image(
                                    painter = painterResource(id = R.drawable.star_icon),
                                    contentDescription = "Rating Icon",
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(bottom = 4.dp)
                                )

                                // ⭐ Rating text
                                val displayRating = movieDetails?.voteAverage ?: voteAverageProp
                                val voteColor =  Color.LightGray// your voting color
                                displayRating?.let { r ->
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            ) {
                                                append(String.format("%.1f",displayRating)) // the rating number in white
                                            }
                                            withStyle(style = SpanStyle(color = voteColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)) {
                                                append("/10") // the "/10" in voting color
                                            }
                                        },
                                        fontSize = 14.sp
                                    )
                                }

                                // ⭐ Votes
                                Text(
                                    text = "${String.format("%,d", movieDetails?.voteCount ?: 0)} votes",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                    }
                }


                Spacer(modifier = Modifier.height(20.dp))
            }

            // -------------------------------
            // ADD TO PLAYLIST BUTTON
            // -------------------------------


            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp),
                    containerColor = Color.DarkGray
                ) {
                    Text("Added to playlist", color = Color.White)
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1800)
                    showSnackbar = false
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DetailsBackdropSection(
    details: MovieDetails?,
    navController: NavHostController,
    posterPathFallback: String?,
    onPlayTrailer: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val backdropUrl = details?.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
        ?: "https://via.placeholder.com/780x350?text=No+Backdrop"

    // Get official trailer key from TMDb data
    val trailerKey = details?.videos?.results
        ?.firstOrNull { it.type == "Trailer" && it.site == "YouTube" }
        ?.key

    val trailerUrl = trailerKey?.let { "https://www.youtube.com/watch?v=$it" }
        ?: "https://www.youtube.com/results?search_query=${Uri.encode("${details?.title} trailer")}"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .systemBarsPadding() // handles status bar
    ) {
        // BACKDROP IMAGE
        Image(
            painter = rememberAsyncImagePainter(backdropUrl),
            contentDescription = "Backdrop",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // TOP GRADIENT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        // TITLE + RELEASE DATE + RUNTIME
        details?.let { movie ->
            val year = movie.releaseDate?.take(4) ?: "----"
            val runtimeText = movie.runtime?.let { formatRuntime(it) } ?: ""

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .widthIn(max = 220.dp)   // ⭐ Prevent overlap with icons
                    .drawBehind {
                        // Liquid glass effect - gradient overlay
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            cornerRadius = CornerRadius(12.dp.toPx()),
                            style = Fill
                        )
                    }
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = movie.title ?: "Unknown Title",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,              // ⭐ wrap instead of pushing sideways
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$year • $runtimeText",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }


        // TOP BAR: Back + Share
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // top + horizontal padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BACK BUTTON
            Box(
                modifier = Modifier
                    .size(44.dp)
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
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

// SHARE BUTTON
            Box(
                modifier = Modifier
                    .size(44.dp)
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
                    .clickable {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Watch the trailer: $trailerUrl")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share via"))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Trailer",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
// PLAY TRAILER BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
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
                .clickable { onPlayTrailer(trailerUrl) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play Trailer",
                tint = Color(0xFFcefc00),
                modifier = Modifier.size(44.dp)
            )
        }
    }
}


private fun jobIsWriter(job: String?): Boolean {
    if (job == null) return false
    val j = job.lowercase()
    return j.contains("writer") || j.contains("screenplay") || j.contains("story")
}

private fun formatRuntime(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
