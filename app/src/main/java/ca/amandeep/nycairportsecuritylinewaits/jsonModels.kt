package ca.amandeep.nycairportsecuritylinewaits

import com.squareup.moshi.Json
import java.util.*

data class Queue(
    @field:Json(name = "timeInMinutes") val timeInMinutes: Int,
    @field:Json(name = "gate") val gate: String,
    @field:Json(name = "terminal") val terminal: String,
    @field:Json(name = "queueType") val queueType: QueueType,
    @field:Json(name = "queueOpen") val queueOpen: Boolean,
    @field:Json(name = "updateTime") val updateTime: Date,
    @field:Json(name = "isWaitTimeAvailable") val isWaitTimeAvailable: Boolean,
    @field:Json(name = "status") val status: String,
)

enum class QueueType {
    Reg, TSAPre
}
