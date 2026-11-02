package com.esp.ffindia

data class PlayerEsp(
    val name: String,
    val health: Float,
    val distance: Float,
    val screenX: Float,
    val screenY: Float,
    val boxW: Float,
    val boxH: Float,
    val isEnemy: Boolean
)

data class EspFrame(
    val players: List<PlayerEsp>
)
