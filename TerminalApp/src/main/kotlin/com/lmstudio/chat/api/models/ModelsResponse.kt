package com.lmstudio.chat.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the /v1/models endpoint.
 */
@Serializable
data class ModelsResponse(
    val `object`: String,
    val data: List<ModelInfo>
)

@Serializable
data class ModelInfo(
    val id: String,
    val `object`: String,
    val created: Long? = null,
    @SerialName("owned_by")
    val ownedBy: String? = null
)
