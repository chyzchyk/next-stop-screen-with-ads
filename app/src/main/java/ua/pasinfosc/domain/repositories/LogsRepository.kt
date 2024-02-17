package ua.pasinfosc.domain.repositories

interface LogsRepository {

    suspend fun log(message: String): String

    suspend fun videoLog(message: String): String
}