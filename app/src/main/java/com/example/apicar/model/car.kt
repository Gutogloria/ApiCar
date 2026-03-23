package com.example.apicar.model

import com.google.gson.annotations.SerializedName

data class Car(
    val id: String,
    val name: String,
    val year: String,
    val licence: String,
    val imageUrl: String,
    val place: CarLocation?
)
data class CarResponse(
    val id: String,
    val value: Car // Aqui é onde o GSON vai achar os dados reais
)
data class CarLocation(
    val lat: Double,
    val long: Double // No seu JSON está "long", então mantenha assim
)