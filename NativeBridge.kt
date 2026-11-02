package com.esp.ffindia

object NativeBridge {
    init {
        System.loadLibrary("native-lib")
    }

    external fun worldToScreen(
        matrix: FloatArray,
        x: Float, y: Float, z: Float,
        width: Int, height: Int
    ): FloatArray?
}
