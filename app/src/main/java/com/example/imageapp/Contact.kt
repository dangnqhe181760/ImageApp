package com.example.imageapp

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Contact(
    @SerializedName("location")
    val address: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("cell")
    val phone: String = "",

    @SerializedName("picture")
    val pictureUrl: String = "",

    @SerializedName("id")
    val id: Int = 0, // Use Int for numerical ID

    @SerializedName("first_name")
    val firstName: String = "",

    @SerializedName("last_name")
    val lastName: String = "",

    ) : Serializable