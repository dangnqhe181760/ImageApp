package com.example.imageapp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitInstance {
    private const val BASE_URL = "https://gist.githubusercontent.com/chiraggshah/a7b3c85712ff2628b4d2ecfdddd4b352/raw/3aab247e4dd2fa8f371b44445332dd1e9c8db039/"

    val api: ApiService by lazy {
        val client = OkHttpClient()
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val clientBuilder: OkHttpClient.Builder =
            client.newBuilder()
                .addInterceptor(interceptor as HttpLoggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)  // Increase timeout
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}