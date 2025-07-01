package com.madtitan.estimator.feature_auth.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.madtitan.estimator.core.data.FirestoreDataSource
import com.madtitan.estimator.core.domain.User
import com.madtitan.estimator.feature_auth.utils.AuthConstants.Companion.AUTH_CLIENT_ID
import com.madtitan.estimator.feature_auth.utils.AuthConstants.Companion.WEB_AUTH_CLIENT_ID
import kotlinx.coroutines.tasks.await


class AuthRepository(private val auth: FirebaseAuth,
                     private val credentialManager: CredentialManager,
                     private val firestoreDataSource: FirestoreDataSource
) {
    suspend fun signInWithGoogle(context: Context): Boolean{
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setServerClientId(WEB_AUTH_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()

        return try {
            val credentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )
            Log.d("Auth", "Credential type: ${credentialResponse.credential.type}")
            Log.d("Auth", "Credential raw: ${credentialResponse.credential}")

            if (credentialResponse.credential is CustomCredential && credentialResponse.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                //val googleIdToken = GoogleIdTokenCredential.createFrom(credentialResponse.credential.data)
                val googleCredential =
                    GoogleIdTokenCredential.createFrom(credentialResponse.credential.data)
                val googleIdToken = googleCredential.idToken
                Log.d("Auth", "Google ID Token: ${googleCredential.idToken.take(20)}") // avoid logging full token

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                val authResult = auth.signInWithCredential(firebaseCredential).await()


                Log.d("Auth", "Sign-in successful: ${authResult.user?.uid}")
                val firebaseUser = authResult.user ?: return false
                // Save user in Firestore if not already present
                ensureUserInFirestore(firebaseUser)
                /*authResult.user?.let {
                    firebaseUser ->
                saveUserToFirestore(firebaseUser)
            }*/
            }else{
                Log.e("Auth", "Credential is not of type Google ID!")
                return false
            }
            true
        }  catch (e: Exception) {
            e.printStackTrace()
            Log.e("Auth", "Google Sign-in failed: ${e.message}")
            false
        }
    }

    private fun saveUserToFirestore(user: FirebaseUser) {
        val userData = User(
            id = user.uid,
            name = user.displayName ?: "",
            email = user.email ?: "",
            role = "user",
            createdAt = Timestamp.now()
        )

        firestoreDataSource.setDocument("users", user.uid, userData)
    }

    fun getCurrentUser() = auth.currentUser

    private suspend fun ensureUserInFirestore(firebaseUser: FirebaseUser) {
        val userRef = firestoreDataSource.getDocument("users", firebaseUser.uid)
        val userSnapshot = userRef.get().await()

        if (!userSnapshot.exists()) {
            val newUser = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                role = "pending", // Default role, needs admin approval
                createdAt = Timestamp.now()
            )
            userRef.set(newUser).await()
        }
    }

}