package com.example.hito4.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.NotificationHelper
import com.example.hito4.data.repo.FriendRequest
import com.example.hito4.data.repo.UserProfile
import com.example.hito4.data.repo.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FeedItem(
    val nickname: String = "",
    val subjectName: String = "",
    val actualMinutes: Int = 0,
    val timestamp: Long = 0L
)

data class FriendsUiState(
    val friends: List<UserProfile> = emptyList(),
    val feed: List<FeedItem> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val searchQuery: String = "",
    val searchResult: UserProfile? = null,
    val searching: Boolean = false,
    val searchError: String? = null,
    val isLoading: Boolean = false,
    val alreadyFriend: Boolean = false,
    val requestAlreadySent: Boolean = false
)

class FriendsViewModel(
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {

    private val _ui = MutableStateFlow(FriendsUiState())
    val ui: StateFlow<FriendsUiState> = _ui.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var lastKnownRequestCount = -1

    init {
        loadAll()
    }

    fun onSearchQueryChange(query: String) {
        _ui.update {
            it.copy(
                searchQuery = query,
                searchResult = null,
                searchError = null,
                alreadyFriend = false,
                requestAlreadySent = false
            )
        }
    }

    fun searchUser() {
        val query = _ui.value.searchQuery.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            _ui.update { it.copy(searching = true, searchResult = null, searchError = null) }
            val result = userRepository.searchUserByNickname(query)
            when {
                result == null -> {
                    _ui.update { it.copy(searching = false, searchError = "Usuario no encontrado") }
                }
                result.uid == auth.currentUser?.uid -> {
                    _ui.update { it.copy(searching = false, searchError = "Ese eres tú 😄") }
                }
                else -> {
                    val alreadyFriend = _ui.value.friends.any { it.uid == result.uid }
                    val requestAlreadySent = userRepository.hasPendingRequestTo(result.uid)
                    _ui.update {
                        it.copy(
                            searching = false,
                            searchResult = result,
                            alreadyFriend = alreadyFriend,
                            requestAlreadySent = requestAlreadySent
                        )
                    }
                }
            }
        }
    }

    fun sendFriendRequest(toUid: String) {
        viewModelScope.launch {
            userRepository.sendFriendRequest(toUid)
            _ui.update { it.copy(requestAlreadySent = true) }
        }
    }

    fun acceptRequest(request: FriendRequest) {
        viewModelScope.launch {
            userRepository.acceptFriendRequest(request)
            loadAll()
        }
    }

    fun rejectRequest(request: FriendRequest) {
        viewModelScope.launch {
            userRepository.rejectFriendRequest(request)
            loadAll()
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            val friends = userRepository.getFriends()
            val pendingRequests = userRepository.getPendingRequests()

            // Mostramos notificación si hay solicitudes nuevas
            if (lastKnownRequestCount >= 0 &&
                pendingRequests.size > lastKnownRequestCount
            ) {
                val newRequest = pendingRequests.first()
                NotificationHelper.showFriendRequestNotification(
                    context,
                    newRequest.fromNickname
                )
            }
            lastKnownRequestCount = pendingRequests.size

            _ui.update {
                it.copy(
                    friends = friends,
                    pendingRequests = pendingRequests,
                    isLoading = false
                )
            }
            loadFeed(friends)
        }
    }

    private suspend fun loadFeed(friends: List<UserProfile>) {
        if (friends.isEmpty()) return
        val friendUids = friends.map { it.uid }
        try {
            val result = db.collection("sessions")
                .whereIn("uid", friendUids)
                .get()
                .await()

            val feedItems = result.documents.mapNotNull { doc ->
                val uid = doc.getString("uid") ?: return@mapNotNull null
                val friend = friends.find { it.uid == uid } ?: return@mapNotNull null
                FeedItem(
                    nickname = friend.nickname,
                    subjectName = doc.getString("subjectName") ?: "",
                    actualMinutes = (doc.getLong("actualMinutes") ?: 0L).toInt(),
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }.sortedByDescending { it.timestamp }

            _ui.update { it.copy(feed = feedItems) }
        } catch (e: Exception) {
            // Si falla el feed no bloqueamos la pantalla
        }
    }
}

class FriendsViewModelFactory(
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendsViewModel(userRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}