package com.example.scenabl.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Manual DI container holding singleton SDK/client instances.
 * Repository singletons are added here in the Data layer phase.
 */
class AppContainer {

    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val imgBbHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()
    }

    val imgBbRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .client(imgBbHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
