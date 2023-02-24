package ca.amandeep.nycairportsecuritylinewaits.data

import ca.amandeep.nycairportsecuritylinewaits.data.model.Queue
import ca.amandeep.nycairportsecuritylinewaits.util.tickFlow
import com.github.ajalt.timberkt.Timber.d
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlin.time.Duration

class AirportRepository(
    private val airportRemoteDataSource: AirportRemoteDataSource,
    private val networkUpdateInterval: Duration,
    private val networkCacheTTL: Duration
) {
    private var refreshAirportFlow = MutableSharedFlow<AirportCode>()
    private val airportsCache = mutableMapOf<AirportCode, Result>()

    fun getWaitTimes(airportCode: AirportCode): Flow<Result> =
        merge(
            tickFlow(networkUpdateInterval).onEach { d { "tick for $airportCode" } },
            refreshAirportFlow.filter { it == airportCode }.onEach { d { "refresh $it" } }.map {}
        ).map {
            airportRemoteDataSource.getWaitTimes(airportCode.shortCode)
                .let {
                    Result(Result.Metadata(System.currentTimeMillis()), it).also {
                        d { "new wallTime: ${it.metadata.lastUpdated}" }
                    }
                }
                .also {
                    airportsCache[airportCode] = it
                }
        }.onStart {
            val cacheUpdateTime = airportsCache[airportCode]?.metadata?.lastUpdated ?: 0L
            if (cacheUpdateTime > System.currentTimeMillis() - networkCacheTTL.inWholeMilliseconds) {
                d { "using cached data for $airportCode" }
                emit(airportsCache[airportCode]!!)
            } else {
                d { "no cached data for $airportCode" }
            }
        }

    suspend fun refresh(airportCode: AirportCode) = refreshAirportFlow.emit(airportCode)

    data class Result(
        val metadata: Metadata,
        val queues: List<Queue>
    ) {
        data class Metadata(
            val lastUpdated: Long
        )
    }
}
