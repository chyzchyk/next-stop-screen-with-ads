package ua.pasinfosc.domain.usecases

import ua.pasinfosc.domain.repositories.ConfigRepository

class GetMarqueeSpeedUseCase(private val configRepository: ConfigRepository) {

    operator fun invoke() = configRepository.getMarqueeSpeed()
}