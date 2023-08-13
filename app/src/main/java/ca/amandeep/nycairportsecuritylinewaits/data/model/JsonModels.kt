package ca.amandeep.nycairportsecuritylinewaits.data.model

import ca.amandeep.nycairportsecuritylinewaits.data.AirportCode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class Queue(
    @field:Json(name = "timeInMinutes") val timeInMinutes: Int,
    @field:Json(name = "gate") val gate: String,
    @field:Json(name = "terminal") val terminal: String,
    @field:Json(name = "queueType") val queueType: QueueType,
    @field:Json(name = "queueOpen") val queueOpen: Boolean,
    @field:Json(name = "updateTime") val updateTime: Date,
    @field:Json(name = "isWaitTimeAvailable") val isWaitTimeAvailable: Boolean,
    @field:Json(name = "status") val status: String,
) {
    fun terminal(airportCode: AirportCode) = when (airportCode) {
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
            "1" -> Terminal.SWF_MAIN
            else -> throw IllegalStateException()
        }
    }
}

enum class QueueType {
    Reg, TSAPre
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
    SWF_MAIN(AirportCode.SWF, "Main"),
}
