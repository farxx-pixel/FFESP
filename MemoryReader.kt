package com.esp.ffindia

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MemoryReader {

    private var pid = -1
    private var mem: RandomAccessFile? = null

    fun attach(packageName: String): Boolean {
        pid = ShizukuHelper.getPid(packageName)
        if (pid <= 0) return false
        return try {
            // Shizuku gives us permission to open /proc/pid/mem without root
            mem = RandomAccessFile("/proc/$pid/mem", "r")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun readLong(address: Long): Long {
        return try {
            mem?.seek(address)
            val buf = ByteArray(8)
            mem?.readFully(buf)
            ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).long
        } catch (e: Exception) {
            0L
        }
    }

    fun readInt(address: Long): Int {
        return try {
            mem?.seek(address)
            val buf = ByteArray(4)
            mem?.readFully(buf)
            ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).int
        } catch (e: Exception) {
            0
        }
    }

    fun readFloat(address: Long): Float {
        return try {
            mem?.seek(address)
            val buf = ByteArray(4)
            mem?.readFully(buf)
            ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).float
        } catch (e: Exception) {
            0f
        }
    }

    fun close() {
        try { mem?.close() } catch (_: Exception) {}
        mem = null
        pid = -1
    }

    fun isAttached() = pid > 0 && mem != null
}
