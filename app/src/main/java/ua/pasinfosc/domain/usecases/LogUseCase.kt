package ua.pasinfosc.domain.usecases

import ua.pasinfosc.domain.repositories.LogsRepository

class LogUseCase(private val repository: LogsRepository) {

    suspend operator fun invoke(message: String) = repository.log(message)
}