package com.example.apicar.service
import com.example.meuprimeiroapp.database.dao.UserLocationDao
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class GetLocationInterceptor(private val userLocationDao: UserLocationDao) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val userLastLocation = runBlocking {
            userLocationDao.getLastLocation()
        }

        val originalRequest = chain.request()

        return if (userLastLocation != null) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("x-data-latitude", userLastLocation.latitude.toString())
                .addHeader("x-data-longitude", userLastLocation.longitude.toString())
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}