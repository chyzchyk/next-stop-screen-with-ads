package ua.pasinfosc.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import ua.pasinfosc.MainActivity
import java.io.File
import java.io.FileWriter
import java.io.IOException

fun outputFile(name: String): File {
    val mediaStorageDir = File("${Environment.getExternalStoragePublicDirectory("/pasinfosc")}")
    return File(mediaStorageDir.path + File.separator + name)
}

fun hasPermission(context: Context, permission: String): Boolean {
    return checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
}

var pasinfoscLogs = ""
var pasinfoscVideoLogs = ""

fun pasinfoscLog(obj: Any) {
    Log.wtf("PasinfoscLog", "$obj")
    pasinfoscLogs += "\n$obj\n"

    if (!hasPermission(MainActivity.context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        && (Build.VERSION.SDK_INT <= 30 || (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()))
    ) return

    val appDirectory = File("${Environment.getExternalStoragePublicDirectory("/pasinfosc")}")
    val logDirectory = File("$appDirectory/logs")
    val file = File("$logDirectory/logs.txt")

    if (!logDirectory.exists()) logDirectory.mkdirs()
    if (!file.exists()) file.createNewFile()

    try {
        FileWriter(file, true).run {
            write("\n$obj\n")
            flush()
            close()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}