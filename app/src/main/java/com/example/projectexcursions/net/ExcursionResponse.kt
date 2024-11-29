package com.example.projectexcursions.net

import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.PageInfo
import kotlinx.serialization.Serializable

@Serializable
data class ExcursionResponse(
    val content: List<Excursion>,
    val page: PageInfo
)

