package dev.hgokhale.lists

sealed interface Platform {
    data object Android: Platform
    data object iOS: Platform
    data object Desktop: Platform
    data object Web: Platform

    val isMobile: Boolean
        get() = this is Android || this is iOS
}

expect fun getPlatform(): Platform