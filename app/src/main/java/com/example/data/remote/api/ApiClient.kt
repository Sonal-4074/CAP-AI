package com.example.data.remote.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Default Android emulator loopback host for local backend on port 5000
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/"

    @Volatile
    private var instance: CapApiService? = null

    fun getService(context: Context, tokenManager: TokenManager): CapApiService {
        return instance ?: synchronized(this) {
            instance ?: buildRetrofit(context, tokenManager).create(CapApiService::class.java).also {
                instance = it
            }
        }
    }

    private fun buildRetrofit(context: Context, tokenManager: TokenManager): Retrofit {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            val token = tokenManager.getToken()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            requestBuilder.header("Accept", "application/json")
            requestBuilder.header("Content-Type", "application/json")

            chain.proceed(requestBuilder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
