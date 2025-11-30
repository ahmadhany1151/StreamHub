package com.example.movieapplication.ui.Splash

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.movieapp.MainActivity
import com.example.movieapplication.ui.Login.LoginActivity
import com.example.movieapplication.ui.Splash.ui.theme.MovieApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {

    private val CHANNEL_ID = "welcome_channel"

    // --- Permission launcher for Android 13+
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Post notification slightly delayed after permission is granted
                Handler(Looper.getMainLooper()).postDelayed({
                    safeShowNotification()
                }, 500)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel (important for Android 8+)
        createNotificationChannel()

        // Check notification permission and show welcome notification safely
        checkAndShowNotification()

        setContent {
            MovieApplicationTheme {
                @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0)
                ) {
                    IntroScreen(
                        onGetInClick = { checkUserStatus() }
                    )
                }
            }
        }
    }

    // --- Check Firebase user and navigate
    private fun checkUserStatus() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // --- Create notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Welcome Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications that welcome the user"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // --- Safely show notification with user name if available
    private fun safeShowNotification() {
        try {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val userName = user?.displayName

            val title = if (!userName.isNullOrEmpty()) {
                "Welcome back, $userName!"
            } else {
                "Welcome to StreamHub!"
            }

            val message = if (!userName.isNullOrEmpty()) {
                "Glad to see you again. Enjoy your experience!"
            } else {
                "Glad to have you here. Let's get you started!"
            }

            // Slight delay ensures notification shows when app is foreground
            Handler(Looper.getMainLooper()).postDelayed({
                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL) // sound + vibration
                    .setAutoCancel(true)
                    .build()

                NotificationManagerCompat.from(this).notify(1001, notification)
            }, 500)

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // --- Check Android 13+ permission and post notification safely
    private fun checkAndShowNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                safeShowNotification()
            } else {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android < 13
            safeShowNotification()
        }
    }
}

// ---------------- Compose UI ----------------

@Composable
fun IntroScreen(onGetInClick: () -> Unit, testMode: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080808))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HeaderSection(onGetInClick = onGetInClick, testMode = testMode)
        }
    }
}

@Composable
fun HeaderSection(onGetInClick: () -> Unit, testMode: Boolean = false) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!testMode) delay(300)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(Color(0xFF080808))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 80.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) +
                        slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Welcome to",
                        modifier = Modifier.testTag("welcome_text"),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "StreamHub",
                        modifier = Modifier.testTag("app_name_text"),
                        style = TextStyle(
                            color = Color(0xFF92B300),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your gateway to endless entertainment.\nStream anywhere, anytime.",
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .testTag("splash_description"),
                        style = TextStyle(
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(45.dp))

                    Button(
                        onClick = onGetInClick,
                        modifier = Modifier
                            .testTag("next_button")
                            .fillMaxWidth(0.6f)
                            .height(50.dp),
                        colors = buttonColors(
                            containerColor = Color(0xFF92B300),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "NEXT",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun IntroScreenPreview() {
    IntroScreen(onGetInClick = {})
}

