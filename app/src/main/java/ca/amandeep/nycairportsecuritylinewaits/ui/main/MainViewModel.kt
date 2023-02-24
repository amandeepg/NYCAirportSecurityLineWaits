package ca.amandeep.nycairportsecuritylinewaits.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ca.amandeep.nycairportsecuritylinewaits.data.AirportApiService
import ca.amandeep.nycairportsecuritylinewaits.data.AirportCode
import ca.amandeep.nycairportsecuritylinewaits.data.AirportRemoteDataSource
import ca.amandeep.nycairportsecuritylinewaits.data.AirportRepository
import com.github.ajalt.timberkt.d
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val airportRepository = AirportRepository(
        airportRemoteDataSource = AirportRemoteDataSource(
            airportApi = AirportApiService.INSTANCE,
            ioDispatcher = Dispatchers.IO
        ),
        networkUpdateInterval = 5.minutes,
        networkCacheTTL = 5.minutes
    )

    fun getWaitTimes(airportCode: AirportCode): Flow<MainUiState> =
        airportRepository.getWaitTimes(airportCode)
            .map { result ->
                val queues = result.queues
                    .groupBy { it.terminal(airportCode) }
                    .mapValues { queuesByTerminal ->
                        queuesByTerminal.value
                            .groupBy { it.gate }
                            .mapValues { Queues(it.value.sortedBy { it.queueType }) }
                            .toList()
                            .sortedBy { it.first }
                            .toImmutableList()
                    }
                    .toList()
                    .sortedBy { it.first }
                    .toImmutableList()

                d { "Got ${queues.size} UI terminals" }

                if (queues.isEmpty()) {
                    MainUiState.Error
                } else {
                    MainUiState.Valid(
                        lastUpdated = result.metadata.lastUpdated,
                        airport = Airport(queues)
                    )
                }
            }
            .retryWhen { cause, attempt ->
                // Retry all errors with a 1 second delay
                emit(MainUiState.Error)
                delay(1.seconds)
                d { "Retrying after error: $cause (attempt $attempt)" }
                true
            }

    suspend fun refreshAirportFromNetwork(airportCode: AirportCode) =
        airportRepository.refresh(airportCode)
}
