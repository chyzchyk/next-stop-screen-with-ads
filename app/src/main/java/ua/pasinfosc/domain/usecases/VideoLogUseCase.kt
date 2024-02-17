package ua.pasinfosc.domain.usecases

import ua.pasinfosc.domain.repositories.LogsRepository

class VideoLogUseCase(private val repository: LogsRepository) {

    suspend operator fun invoke(message: String) = repository.videoLog(message)
}