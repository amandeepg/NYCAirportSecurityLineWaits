package ca.amandeep.nycairportsecuritylinewaits.ui.main

import ca.amandeep.nycairportsecuritylinewaits.data.model.Queue
import ca.amandeep.nycairportsecuritylinewaits.data.model.QueueType
import ca.amandeep.nycairportsecuritylinewaits.data.model.Terminal

sealed interface MainUiState {
    data class Valid(
        val lastUpdated: Long,
        val airport: Airport,
        val hasError: Boolean = false
    ) : MainUiState

    object Error : MainUiState
    object Loading : MainUiState
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
