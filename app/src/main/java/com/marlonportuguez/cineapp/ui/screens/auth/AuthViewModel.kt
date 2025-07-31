package com.marlonportuguez.cineapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLogin = MutableStateFlow(true)
    val isLogin: StateFlow<Boolean> = _isLogin

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _authError.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _authError.value = null
    }

    fun toggleLoginState() {
        _isLogin.value = !_isLogin.value
        _authError.value = null
        _email.value = ""
        _password.value = ""
    }

    fun authenticateUser() {
        _isLoading.value = true
        _authError.value = null

        viewModelScope.launch {
            try {
                if (_isLogin.value) {
                    auth.signInWithEmailAndPassword(email.value, password.value).await()
                } else {
                    auth.createUserWithEmailAndPassword(email.value, password.value).await()
                    saveUserToFirestore(auth.currentUser?.uid, email.value)
                }
                _authSuccess.value = true
                addSessionLog(auth.currentUser?.uid, if (_isLogin.value) "login" else "register")
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Error desconocido."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Implementación Google Sign-In con Firebase
    fun signInWithGoogle(credential: AuthCredential) {
        _isLoading.value = true
        _authError.value = null

        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await() // Autenticar con la credencial de Google
                _authSuccess.value = true
                addSessionLog(auth.currentUser?.uid, "login_google")
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Error al iniciar sesión con Google."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveUserToFirestore(uid: String?, email: String) {
        uid ?: return

        val userMap = hashMapOf(
            "email" to email,
            "name" to email.substringBefore("@"),
            "registrationDate" to Timestamp.now(),
            "lastLogin" to Timestamp.now()
        )

        viewModelScope.launch {
            try {
                firestore.collection("users").document(uid).set(userMap).await()
            } catch (e: Exception) {
                // Manejo de errores de Firestore
            }
        }
    }

    fun addSessionLog(uid: String?, action: String) {
        uid ?: return

        val logMap = hashMapOf(
            "userId" to uid,
            "action" to action,
            "timestamp" to Timestamp.now()
        )

        viewModelScope.launch {
            try {
                firestore.collection("sessionLogs").add(logMap).await()
            } catch (e: Exception) {
                // Manejo de errores de log
            }
        }
    }

    fun signOut() {
        val currentUserId = auth.currentUser?.uid
        auth.signOut()
        addSessionLog(currentUserId, "logout")
    }

    fun resetAuthSuccess() {
        _authSuccess.value = false
    }

    fun resetAuthError() {
        _authError.value = null
    }

    fun clearInputs() {
        _email.value = ""
        _password.value = ""
    }
}