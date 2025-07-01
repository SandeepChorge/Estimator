package com.madtitan.estimator.feature_auth.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.madtitan.estimator.core.data.FirestoreDataSource
import com.madtitan.estimator.feature_auth.data.AuthRepository
import com.madtitan.estimator.feature_auth.domain.SignInUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideCredentialManager(context: Context): CredentialManager =
        CredentialManager.create(context)

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, credentialManager: CredentialManager, firestoreDataSource: FirestoreDataSource): AuthRepository =
        AuthRepository(auth, credentialManager, firestoreDataSource)

    @Provides
    @Singleton
    fun provideSignInUseCase(authRepository: AuthRepository): SignInUseCase =
        SignInUseCase(authRepository)
}