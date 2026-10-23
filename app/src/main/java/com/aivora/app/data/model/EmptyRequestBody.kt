package com.aivora.app.data.model

import kotlinx.serialization.Serializable

/** Empty JSON object `{}` for POSTs that need a body for Retrofit/kotlinx.serialization. */
@Serializable
class EmptyRequestBody
