package ca.amandeep.nycairportsecuritylinewaits.data

import ca.amandeep.nycairportsecuritylinewaits.data.model.Queue
import com.github.ajalt.timberkt.Timber.d
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AirportRemoteDataSource(
    private val airportApi: AirportApiService,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getWaitTimes(airport: String): List<Queue> =
        withContext(ioDispatcher) {
            d { "getWaitTimes" }
            airportApi.getWaitTimes(airport)
        }
}
