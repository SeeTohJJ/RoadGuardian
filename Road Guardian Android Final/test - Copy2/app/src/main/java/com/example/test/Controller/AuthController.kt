package com.example.test.Controller

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth


// Ping Firebase to get user current authentication status
class AuthViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    println("Login successful test" + authState.value)
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun register(email: String, password: String) {

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        if(!validatePassword(password)){
            _authState.value =
                AuthState.Error("Password must contain at least 8 characters, including uppercase, lowercase, number, and special character")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Unauthenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun logout() {
        auth.signOut()
        println("signout")
        _authState.value = AuthState.Unauthenticated
    }

    //fun resetPassword(email: String) {
    //    auth.sendPasswordResetEmail(email)
    //}

    fun resetPassword(newPassword: String, context: Context) {
        // Validate the new password using regex
        if (!validatePassword(newPassword)) {
            Toast.makeText(context, "Password does not meet security requirements. Use 8+ characters, uppercase, lowercase, number, and special character.", Toast.LENGTH_LONG).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        println("user: " + FirebaseAuth.getInstance().currentUser)
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_LONG).show()
                        _authState.value = AuthState.Unauthenticated
                    } else {
                        Toast.makeText(context, "Error updating password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(context, "User is not logged in. Please log in again.", Toast.LENGTH_LONG).show()
        }
    }

    fun forgotPassword(email: String, context: Context) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // No sign-in methods mean the user does not exist
                        Toast.makeText(context, "User does not exist", Toast.LENGTH_SHORT).show()
                    } else {
                        // User exists, proceed with password reset
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { resetTask ->
                                if (resetTask.isSuccessful) {
                                    Toast.makeText(context, "Check your email for the password reset link", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Handle error
                    Toast.makeText(context, "Error checking email existence", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun validatePassword(password: String): Boolean {
        // Regular expression for password validation
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"
        return password.matches(passwordRegex.toRegex())
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

