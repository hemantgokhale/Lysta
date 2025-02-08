package dev.hgokhale.lysta

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform