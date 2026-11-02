package com.esp.ffindia

import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuHelper {

    fun isReady(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun exec(cmd: String): String? {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", cmd), null, null)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText().trim()
            process.waitFor()
            output
        } catch (e: Exception) {
            null
        }
    }

    fun getPid(packageName: String): Int {
        val out = exec("pidof $packageName") ?: return -1
        return out.split("\\s+".toRegex()).firstOrNull()?.toIntOrNull() ?: -1
    }

    fun getModuleBase(pid: Int, module: String): Long {
        val maps = exec("cat /proc/$pid/maps") ?: return 0L
        val line = maps.lines().firstOrNull { it.contains(module) && it.contains("r-xp") } ?: return 0L
        val addr = line.substringBefore("-")
        return addr.toLongOrNull(16) ?: 0L
    }
}
