package ua.pasinfosc.data.repository

import android.util.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.pasinfosc.data.network.LogsService
import ua.pasinfosc.domain.repositories.LogsRepository
import java.net.URLEncoder
import java.util.Calendar

class LogsRepositoryImpl : LogsRepository, KoinComponent {

    private val logsService: LogsService by inject()

    override suspend fun log(message: String): String {
        val response = logsService.log(
            url = "https://hatebin.com/index.php",
            body = "text=${URLEncoder.encode(message)}"
        )
        val body = response.body()
        val errorBody = response.errorBody()?.string()

        Log.d("as", response.toString())
        Log.d("as", body.toString())
        Log.d("as", errorBody.toString())

        return body ?: errorBody.toString()
    }

    override suspend fun videoLog(message: String): String {
        val response = logsService.log(
            url = "https://pastebin.com/api/api_post.php",
            body = mapOf(
                "api_dev_key" to "0411723f73c3e2c410c452c791d2f447",
                "api_paste_private" to 1,
                "api_paste_name" to "logs (${
                    Calendar.getInstance().let {
                        "${it.get(Calendar.HOUR_OF_DAY)}:${it.get(Calendar.MINUTE)} ${it.get(Calendar.DAY_OF_MONTH)}.${it.get(Calendar.MONTH) + 1}"
                    }
                })",
                "api_paste_expire_date" to "N",
                "api_option" to "paste",
            ).map {
                "${it.key}=${it.value}"
            }.joinToString("&")
        )
        val body = response.body()
        val errorBody = response.errorBody()?.string()

        Log.d("as", response.toString())
        Log.d("as", body.toString())
        Log.d("as", errorBody.toString())

        return body ?: errorBody.toString()
    }
}