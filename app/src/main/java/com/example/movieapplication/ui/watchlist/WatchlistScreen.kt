package com.example.movieapp.ui.watchlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.movieapp.data.local.MovieEntity

@Composable
fun WatchlistScreen(
    watchlist: List<MovieEntity>,
    onBackClick: () -> Unit,
    onMovieClick: (MovieEntity) -> Unit,
    onRemoveClick: (MovieEntity) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // 🔙 زر الرجوع
        TextButton(onClick = onBackClick, modifier = Modifier.testTag("backButton")) {
            Text("← Back", color = Color.White, fontSize = 16.sp)
        }

        // 🧾 العنوان
        Text(
            text = "My Watchlist",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp).testTag("watchlistTitle")
        )

        // 🕳️ لو القائمة فاضية
        if (watchlist.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Your watchlist is empty!",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.testTag("emptyMessage")
                )
            }
        } else {
            // 📜 عرض الأفلام في قائمة
            LazyColumn {
                items(watchlist) { movie ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("movieItem_${movie.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMovieClick(movie) }
                                .testTag("movieRow_${movie.id}"),

                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 🖼️ صورة الفيلم
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = "https://image.tmdb.org/t/p/w500${movie.posterPath?.let { if (it.startsWith('/')) it else "/$it" } ?: ""}"
                                    ),
                                    contentDescription = movie.title,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                // Remove Icon
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove from Watchlist",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clickable { onRemoveClick(movie) }
                                        .testTag("removeButton_${movie.id}")
                                )
                            }

                            // 📜 بيانات الفيلم
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = movie.title,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                movie.voteAverage?.let {
                                    Text(
                                        text = "⭐ ${String.format("%.1f", it)}/10",
                                        color = Color.Yellow,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    Divider(color = Color.DarkGray)
                }

                item {
                    Spacer(modifier = Modifier.height(85.dp))
                }
            }
        }
    }
}