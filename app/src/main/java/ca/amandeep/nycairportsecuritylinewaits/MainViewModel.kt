package ca.amandeep.nycairportsecuritylinewaits

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val airports = mutableStateMapOf<AirportCode, Airport>()

    fun load(airportCode: AirportCode) = viewModelScope.launch(Dispatchers.IO) {
        val terminals = AirportService.INSTANCE
            .getWaitTimes(airportCode)
            .groupBy {
                val terminal = it.terminal
                when (airportCode) {
                    AirportCode.EWR -> when (terminal) {
                        "A" -> Terminal.EWR_A
                        "B" -> Terminal.EWR_B
                        "C" -> Terminal.EWR_C
                        else -> throw IllegalStateException()
                    }
                    AirportCode.JFK -> when (terminal) {
                        "1" -> Terminal.JFK_1
                        "2" -> Terminal.JFK_2
                        "4" -> Terminal.JFK_4
                        "5" -> Terminal.JFK_5
                        "7" -> Terminal.JFK_7
                        "8" -> Terminal.JFK_8
                        else -> throw IllegalStateException()
                    }
                    AirportCode.LGA -> when (terminal) {
                        "A" -> Terminal.LGA_A
                        "B" -> Terminal.LGA_B
                        "C" -> Terminal.LGA_C
                        "D" -> Terminal.LGA_D
                        else -> throw IllegalStateException()
                    }
                    AirportCode.SWF -> when (terminal) {
                        "1" -> Terminal.SWF_1
                        else -> throw IllegalStateException()
                    }
                }
            }
            .mapValues {
                it.value
                    .groupBy { it.gate }
                    .mapValues { Queues(it.value.sortedBy { it.queueType }) }
                    .toList()
                    .sortedBy { it.first }
            }
            .toList()
            .sortedBy { it.first }
        airports[airportCode] = Airport(terminals)
    }

    data class Airport(
        val terminals: List<Pair<Terminal, List<Pair<String, Queues>>>> = emptyList()
    )

    data class Queues(
        val general: Queue,
        val preCheck: Queue?
    ) {
        constructor(queues: List<Queue>) : this(
            general = queues.find { it.queueType == QueueType.Reg }!!,
            preCheck = queues.find { it.queueType == QueueType.TSAPre }
        )

        val bothClosed = !general.queueOpen && preCheck?.queueOpen != true
    }

    enum class Terminal(
        val airportCode: AirportCode,
        val identifier: String,
    ) {
        EWR_A(AirportCode.EWR, "A"),
        EWR_B(AirportCode.EWR, "B"),
        EWR_C(AirportCode.EWR, "C"),
        JFK_1(AirportCode.JFK, "1"),
        JFK_2(AirportCode.JFK, "2"),
        JFK_4(AirportCode.JFK, "4"),
        JFK_5(AirportCode.JFK, "5"),
        JFK_7(AirportCode.JFK, "7"),
        JFK_8(AirportCode.JFK, "8"),
        LGA_A(AirportCode.LGA, "A"),
        LGA_B(AirportCode.LGA, "B"),
        LGA_C(AirportCode.LGA, "C"),
        LGA_D(AirportCode.LGA, "D"),
        SWF_1(AirportCode.SWF, "1"),
    }
}
