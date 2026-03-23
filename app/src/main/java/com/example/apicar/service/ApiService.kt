package com.example.apicar.service


import com.example.apicar.model.Car
import com.example.apicar.model.CarResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("car")
    suspend fun getCars(): List<Car>

    @GET("car/{id}")
    suspend fun getCar(@Path("id") id: String): List<Car>

    @POST("car")
    suspend fun addCar(@Body car: Car): Car

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Car

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String)
}