package com.example.rhythmspot

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.AuthError("Email and password must not be empty")
            return
        }
        _authState.value = AuthState.Authenticating
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null && currentUser.isEmailVerified) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Unauthenticated
                        Toast.makeText(Firebase.app.applicationContext, "Please verify your Email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    _authState.value = AuthState.AuthError(task.exception?.message ?: "Something went wrong")
                }
            }
    }
    fun signup(name: String, email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.AuthError("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Authenticating
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "profilePic" to "https://firebasestorage.googleapis.com/v0/b/rhythmspot-8d068.appspot.com/o/images%2Fblank-profile-picture-973460_1280.png?alt=media&token=fdeee537-9668-49a7-b6b2-500a9a85eea2",
                        "favorites" to ArrayList<String>()
                    )
                    user?.let {
                        db.collection("users").document(user.uid).set(userData)
                                .addOnSuccessListener {
                                    user.sendEmailVerification().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(Firebase.app.applicationContext, "Verification email sent", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(Firebase.app.applicationContext, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    _authState.value = AuthState.Unauthenticated
                                }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.AuthError(e.message ?: "Failed to save user data")
                            }
                    } ?: run {
                        _authState.value = AuthState.AuthError("Failed to get the current user")
                    }
                } else {
                    _authState.value = AuthState.AuthError(task.exception?.message ?: "Something went wrong")
                }
            }
    }
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Authenticating : AuthState()
    data class AuthError(val message: String) : AuthState()
}