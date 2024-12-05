package com.example.projectexcursions.net

import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.models.PageInfo
import kotlinx.serialization.Serializable

@Serializable
data class ExcursionsResponse(
    val content: List<ExcursionsList>,
    val page: PageInfo
)

