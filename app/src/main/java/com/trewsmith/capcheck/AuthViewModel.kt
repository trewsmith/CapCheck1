package com.trewsmith.capcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        checkUser()
    }

    private fun checkUser() {
        _user.value = auth.currentUser
        _isInitializing.value = false
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _user.value = auth.currentUser
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _user.value = auth.currentUser
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }
}
