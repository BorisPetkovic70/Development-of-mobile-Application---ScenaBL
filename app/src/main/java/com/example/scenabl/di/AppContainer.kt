package com.example.scenabl.di

import com.example.scenabl.BuildConfig
import com.example.scenabl.data.remote.AuthRemoteDataSource
import com.example.scenabl.data.remote.ImgBbApi
import com.example.scenabl.data.remote.ImgBbRemoteDataSource
import com.example.scenabl.data.remote.InstitutionRemoteDataSource
import com.example.scenabl.data.remote.PerformanceRemoteDataSource
import com.example.scenabl.data.remote.ReservationRemoteDataSource
import com.example.scenabl.data.remote.ReviewRemoteDataSource
import com.example.scenabl.data.remote.TitleRemoteDataSource
import com.example.scenabl.data.remote.UserListRemoteDataSource
import com.example.scenabl.data.remote.UserRemoteDataSource
import com.example.scenabl.data.repository.AuthRepository
import com.example.scenabl.data.repository.PerformanceRepository
import com.example.scenabl.data.repository.ReservationRepository
import com.example.scenabl.data.repository.ReviewRepository
import com.example.scenabl.data.repository.TitleRepository
import com.example.scenabl.data.repository.UserListRepository
import com.example.scenabl.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Manual DI container holding singleton repository instances. UI and
 * ViewModel code depends only on the `repository` properties below —
 * the raw Firebase/Retrofit clients stay private (NFR-MAINT-002).
 */
class AppContainer {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val imgBbHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()
    }

    private val imgBbRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .client(imgBbHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val imgBbApi: ImgBbApi by lazy { imgBbRetrofit.create(ImgBbApi::class.java) }
    private val imgBbRemoteDataSource: ImgBbRemoteDataSource by lazy {
        ImgBbRemoteDataSource(imgBbApi, BuildConfig.IMGBB_API_KEY)
    }

    private val authRemoteDataSource by lazy { AuthRemoteDataSource(firebaseAuth) }
    private val userRemoteDataSource by lazy { UserRemoteDataSource(firestore) }
    private val institutionRemoteDataSource by lazy { InstitutionRemoteDataSource(firestore) }
    private val titleRemoteDataSource by lazy { TitleRemoteDataSource(firestore) }
    private val performanceRemoteDataSource by lazy { PerformanceRemoteDataSource(firestore) }
    private val reservationRemoteDataSource by lazy { ReservationRemoteDataSource(firestore) }
    private val reviewRemoteDataSource by lazy { ReviewRemoteDataSource(firestore) }
    private val userListRemoteDataSource by lazy { UserListRemoteDataSource(firestore) }

    val authRepository: AuthRepository by lazy { AuthRepository(authRemoteDataSource) }

    val userRepository: UserRepository by lazy {
        UserRepository(userRemoteDataSource, institutionRemoteDataSource, imgBbRemoteDataSource)
    }

    val titleRepository: TitleRepository by lazy {
        TitleRepository(titleRemoteDataSource, imgBbRemoteDataSource)
    }

    val performanceRepository: PerformanceRepository by lazy {
        PerformanceRepository(performanceRemoteDataSource)
    }

    val reservationRepository: ReservationRepository by lazy {
        ReservationRepository(reservationRemoteDataSource)
    }

    val reviewRepository: ReviewRepository by lazy {
        ReviewRepository(reviewRemoteDataSource)
    }

    val userListRepository: UserListRepository by lazy {
        UserListRepository(userListRemoteDataSource)
    }
}
