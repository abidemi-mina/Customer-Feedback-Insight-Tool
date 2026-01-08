package com.mina.customerinsight

import kotlinx.serialization.Serializable

@Serializable
data class AIResult(val sentiment: String, val keyPoints: List<String>)
