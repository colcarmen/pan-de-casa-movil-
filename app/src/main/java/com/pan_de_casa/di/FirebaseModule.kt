package com.pan_de_casa.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Mock classes for Local Demo
class MockFirebaseAuth {
    fun getInstance() = this
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    // This module is kept empty or with dummy provides for local demo 
    // to avoid compilation errors without google-services.json
}
