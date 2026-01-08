package com.mina.customerinsight

import kotlinx.serialization.Serializable

@Serializable
data class Feedback(val id: String, val content: String, val rating: Int)


