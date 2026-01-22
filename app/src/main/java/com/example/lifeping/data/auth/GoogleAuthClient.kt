package com.example.lifeping.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.lifeping.R
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class GoogleAuthClient(
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): Result<AuthResult> {
        return try {
            val webClientId = context.getString(R.string.default_web_client_id)
            
            // Build the Google ID option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            // Build the credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Request the credential
            // Note: This needs to be called from an Activity context or handled via ActivityResultRegistry if possible,
            // but CredentialManager.getCredential(context, request) works if context is Activity.
            // If context is Application, it will fail. We rely on the caller passing Activity context.
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                Result.success(authResult)
            } else {
                Result.failure(Exception("Received unknown credential type."))
            }
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthClient", "GetCredentialException", e)
             // Check if it's a cancellation to avoid noise, or return failure
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("GoogleAuthClient", "Unexpected exception", e)
            Result.failure(e)
        }
    }

    fun getSignedInUser(): com.google.firebase.auth.FirebaseUser? {
        return auth.currentUser
    }

    fun signOut() {
        auth.signOut()
    }
}
