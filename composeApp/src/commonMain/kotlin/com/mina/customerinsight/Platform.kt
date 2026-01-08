package com.mina.customerinsight

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform