package com.example.movieapp.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    init {
        loadUserProfile()
        observeFavoriteGenres()
        observeFavoriteCelebrities()
        observeUsernameChanges()
    }
    fun observeUsernameChanges() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val username = snapshot.get("username") as? String
                    if (!username.isNullOrEmpty()) {
                        _profileState.value = _profileState.value.copy(username = username)
                    }
                }
            }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userDoc = db.collection("users").document(currentUser.uid).get().await()
                    if (userDoc.exists()) {
                        val userData = userDoc.data
                        _profileState.value = ProfileState(
                            username = userData?.get("username") as? String ?: currentUser.displayName ?: "User",
                            email = currentUser.email ?: "",
                            profileImageUrl = userData?.get("profileImageUrl") as? String ?: "",
                            favoriteGenres = (userData?.get("favoriteGenres") as? List<String>) ?: emptyList(),
                            favoriteCelebrities = (userData?.get("favoriteCelebrities") as? List<Map<String, String>>)?.map {
                                FavoriteCelebrity(
                                    id = it["id"] ?: "",
                                    name = it["name"] ?: "",
                                    role = it["role"] ?: "",
                                    imageUrl = it["imageUrl"] ?: ""
                                )
                            } ?: emptyList(),
                            hasCompletedGenreSelection = userData?.get("hasCompletedGenreSelection") as? Boolean ?: false
                        )
                    } else {
                        createUserDocument(currentUser.uid)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _profileState.value = ProfileState(
                    username = "Movie Lover",
                    email = "user@example.com",
                    favoriteGenres = listOf("Action", "Drama", "Sci-Fi"),
                    favoriteCelebrities = listOf(
                        FavoriteCelebrity("1", "Tom Hanks", "Actor"),
                        FavoriteCelebrity("2", "Christopher Nolan", "Director")
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun createUserDocument(userId: String) {
        val currentUser = auth.currentUser
        val userData = hashMapOf(
            "username" to (currentUser?.displayName ?: "User"),
            "email" to (currentUser?.email ?: ""),
            "favoriteGenres" to emptyList<String>(),
            "favoriteCelebrities" to emptyList<Map<String, String>>(),
            "hasCompletedGenreSelection" to false,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("users").document(userId).set(userData).await()

        _profileState.value = ProfileState(
            username = currentUser?.displayName ?: "User",
            email = currentUser?.email ?: "",
            profileImageUrl = "",
            favoriteGenres = emptyList(),
            favoriteCelebrities = emptyList(),
            hasCompletedGenreSelection = false
        )
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                db.collection("users").document(userId).update("username", newUsername).await()
                _profileState.value = _profileState.value.copy(username = newUsername)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun observeFavoriteGenres() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val genres = (snapshot.get("favoriteGenres") as? List<String>) ?: emptyList()
                    _profileState.value = _profileState.value.copy(favoriteGenres = genres)
                }
            }
    }

    fun saveFavoriteGenre(genre: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                db.collection("users").document(userId)
                    .set(mapOf("favoriteGenres" to FieldValue.arrayUnion(genre)), SetOptions.merge())
                    .await()

                val updatedList = _profileState.value.favoriteGenres.toMutableList()
                if (!updatedList.contains(genre)) updatedList.add(genre)
                _profileState.value = _profileState.value.copy(favoriteGenres = updatedList)

            } catch (e: Exception) {
                Log.e("ProfileVM", "Error saving genre: ${e.message}")
            }
        }
    }
    fun removeFavoriteCelebrity(celebrity: FavoriteCelebrity) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val celebrityData = hashMapOf<String, String>(
                    "id" to celebrity.id,
                    "name" to celebrity.name,
                    "role" to celebrity.role,
                    "imageUrl" to celebrity.imageUrl
                )

                db.collection("users").document(userId)
                    .update("favoriteCelebrities", FieldValue.arrayRemove(celebrityData))
                    .await()

                Log.d("ProfileVM", "Celebrity removed from favorites")

            } catch (e: Exception) {
                Log.e("ProfileVM", "Error removing celebrity: ${e.message}")
            }
        }
    }

    fun removeFavoriteGenre(genre: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                db.collection("users").document(userId)
                    .update("favoriteGenres", FieldValue.arrayRemove(genre))
                    .await()

                val updatedList = _profileState.value.favoriteGenres.toMutableList()
                updatedList.remove(genre)
                _profileState.value = _profileState.value.copy(favoriteGenres = updatedList)

            } catch (e: Exception) {
                Log.e("ProfileVM", "Error removing genre: ${e.message}")
            }
        }
    }

    fun observeFavoriteCelebrities() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val favorites = (snapshot.get("favoriteCelebrities") as? List<Map<String, String>>)?.map {
                        FavoriteCelebrity(
                            id = it["id"] ?: "",
                            name = it["name"] ?: "",
                            role = it["role"] ?: "",
                            imageUrl = it["imageUrl"] ?: ""
                        )
                    } ?: emptyList()
                    _profileState.value = _profileState.value.copy(favoriteCelebrities = favorites)
                }
            }
    }




}


    data class ProfileState(
        val username: String = "",
        val email: String = "",
        val profileImageUrl: String = "",
        val favoriteGenres: List<String> = emptyList(),
        val favoriteCelebrities: List<FavoriteCelebrity> = emptyList(),
        var hasCompletedGenreSelection : Boolean = false
    )

    data class FavoriteCelebrity(
        val id: String,
        val name: String,
        val role: String,
        val imageUrl: String = ""
    )
