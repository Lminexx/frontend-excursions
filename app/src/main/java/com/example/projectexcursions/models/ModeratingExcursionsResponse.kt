package com.example.projectexcursions.models

import kotlinx.serialization.Serializable

@Serializable
data class ModeratingExcursionsResponse(
    val content: List<ExcursionsList>,
    val page: PageInfo
)