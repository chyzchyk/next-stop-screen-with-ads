package ua.pasinfosc.data.di

import org.koin.dsl.module
import ua.pasinfosc.data.repository.StopsRepositoryImpl
import ua.pasinfosc.data.repository.BusesRepositoryImpl
import ua.pasinfosc.data.repository.ConfigRepositoryImpl
import ua.pasinfosc.data.repository.LogsRepositoryImpl
import ua.pasinfosc.domain.repositories.StopsRepository
import ua.pasinfosc.domain.repositories.BusesRepository
import ua.pasinfosc.domain.repositories.ConfigRepository
import ua.pasinfosc.domain.repositories.LogsRepository

val repositoriesModule = module {

    single<BusesRepository> { BusesRepositoryImpl() }

    single<StopsRepository> { StopsRepositoryImpl() }

    single<ConfigRepository> { ConfigRepositoryImpl() }

    single<LogsRepository> { LogsRepositoryImpl() }
}