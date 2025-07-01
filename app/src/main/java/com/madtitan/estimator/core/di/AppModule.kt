package com.madtitan.estimator.core.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madtitan.estimator.core.data.FirestoreDataSource
import com.madtitan.estimator.core.data.repository.EventRepository
import com.madtitan.estimator.core.data.repository.PaymentRepository
import com.madtitan.estimator.core.data.repository.RecipientRepository
import com.madtitan.estimator.core.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context


    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirestoreDataSource(firestore: FirebaseFirestore): FirestoreDataSource =
        FirestoreDataSource(firestore)

    @Provides
    @Singleton
    fun provideUserRepository(dataSource: FirestoreDataSource, auth: FirebaseAuth): UserRepository =
        UserRepository(dataSource, auth)

    @Provides
    @Singleton
    fun provideEventRepository(dataSource: FirestoreDataSource): EventRepository =
        EventRepository(dataSource)

    @Provides
    @Singleton
    fun provideRecipientRepository(dataSource: FirestoreDataSource): RecipientRepository =
        RecipientRepository(dataSource)

    @Provides
    @Singleton
    fun providePaymentRepository(dataSource: FirestoreDataSource): PaymentRepository =
        PaymentRepository(dataSource)
}