package com.example.projectexcursions.net

import hilt_aggregated_deps._com_example_projectexcursions_ui_favorite_excursions_FavFragment_GeneratedInjector
import kotlinx.serialization.Serializable

@Serializable
data class ExcursionResponse (
    val id: Long,
    val title: String,
    val userId: Long,
    val description: String,
    val username: String
    //val favorite: Boolean = false
)