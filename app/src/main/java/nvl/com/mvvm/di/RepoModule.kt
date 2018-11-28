package nvl.com.mvvm.di

import android.content.Context
import nvl.com.mvvm.di.DatasourceProperties.SERVER_URL
import nvl.com.mvvm.services.api.ApiService
import nvl.com.mvvm.services.api.ApiServiceImp
import nvl.com.mvvm.services.api.HttpRepository
import nvl.com.mvvm.services.local.LocalService
import nvl.com.mvvm.services.local.LocalServiceImp
import nvl.com.mvvm.usecase.UserUseCase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.applicationContext
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


val repoModule = module {
    single { createOkHttpClient() }
    single { createWebService<HttpRepository>(get(), SERVER_URL) }
    single { createApiService(get()) }
    single { createLocalService(androidContext()) }
    single { createUsercase(get(), get()) }

}


object DatasourceProperties {
    const val SERVER_URL = "https://api.stackexchange.com/2.2/"
}

fun createApiService(httpRepository: HttpRepository): ApiService = ApiServiceImp(httpRepository)
fun createLocalService(context: Context): LocalService = LocalServiceImp(context)

fun createOkHttpClient(): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
    return OkHttpClient.Builder()
            .connectTimeout(60L, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor).build()
}

inline fun <reified T> createWebService(okHttpClient: OkHttpClient, url: String): T {
    val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
    return retrofit.create(T::class.java)
}

fun createUsercase(apiService: ApiService, localService: LocalService) = UserUseCase(apiService, localService)