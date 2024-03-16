package ca.amandeep.nycairportsecuritylinewaits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.InstantAnimationsRule
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.ThumbnailScale
import ca.amandeep.nycairportsecuritylinewaits.data.AirportCode
import ca.amandeep.nycairportsecuritylinewaits.data.model.Queue
import ca.amandeep.nycairportsecuritylinewaits.data.model.QueueType
import ca.amandeep.nycairportsecuritylinewaits.data.model.Terminal
import ca.amandeep.nycairportsecuritylinewaits.ui.main.Airport
import ca.amandeep.nycairportsecuritylinewaits.ui.main.MainUiState
import ca.amandeep.nycairportsecuritylinewaits.ui.main.Queues
import ca.amandeep.nycairportsecuritylinewaits.ui.main.TitleAndBackBar
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.NYCAirportSecurityLineWaitsTheme
import ca.amandeep.nycairportsecuritylinewaits.util.ConnectionState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.Instant
import java.util.Date

@Suppress("RemoveExplicitTypeArguments")
val DEVICE_DATA = listOfNotNull<List<Any>>(
//    listOf("Pixel 4", DeviceConfig.PIXEL_4),
//    listOf("Pixel 4a", DeviceConfig.PIXEL_4A),
//    listOf("Pixel 4 XL", DeviceConfig.PIXEL_4_XL),
//    listOf("Pixel 5", DeviceConfig.PIXEL_5),
//    listOf("Pixel 6", DeviceConfig.PIXEL_6),
//    listOf("Pixel 6 Pro", DeviceConfig.PIXEL_6_PRO),
//    listOf("Pixel 7", PIXEL_7),
//    listOf("Pixel 7a", PIXEL_7A),
//    listOf("Pixel 7 Pro", PIXEL_7_PRO),
//    listOf("Pixel 8", PIXEL_8),
//    listOf("Pixel 8a", PIXEL_8A),
    listOf("Pixel 8 Pro", PIXEL_8_PRO),
//    listOf("Pixel Fold", PIXEL_FOLD),
//    listOf("Pixel Fold Outer", PIXEL_FOLD_OUTER),
//    listOf("Galaxy Z Fold5", GALAXY_Z_FOLD_5),
//    listOf("Galaxy Z Fold5 Outer", GALAXY_Z_FOLD_5_OUTER),
//    listOf("Galaxy Z Flip5", GALAXY_Z_FLIP_5),
//    listOf("Galaxy Z Flip5 Outer", GALAXY_Z_FLIP_5_OUTER),
//    listOf("Galaxy S23", GALAXY_S_23),
//    listOf("Galaxy S23+", GALAXY_S_23_PLUS),
//    listOf("Galaxy S23 Ultra", GALAXY_S_23_ULTRA),
//    listOf("Galaxy S24", GALAXY_S_24),
//    listOf("Galaxy S24+", GALAXY_S_24_PLUS),
//    listOf("Galaxy S24 Ultra", GALAXY_S_24_ULTRA),
).andLandscape() + listOf<List<Any>>()

private val DATA: List<Any> = run {
    @Suppress("ktlint:standard:no-multi-spaces", "ktlint:standard:comment-wrapping")
    listOf(
        /* isDarkMode */            listOf(true, false),
        /* includeNotifs */         listOf(true, false),
        /* airport */               listOf(AirportCode.EWR, AirportCode.JFK, AirportCode.LGA),
        DEVICE_DATA,
    ).cartesianProduct().map {
        val device = it.last() as List<*>

        arrayOf(
            it[0] as Boolean,
            device[0],
            device[1],
            it[1] as Boolean,
            it[2] as List<*>,
        )
    }
}

@RunWith(Parameterized::class)
class AirportScreenshotTest(
    private val isDarkMode: Boolean,
    @Suppress("unused") private val deviceName: String,
    private val device: DeviceConfig,
    private val includeNotifs: Boolean,
    private val airportCode: AirportCode,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(
            name = "darkMode={0}," +
                " device={1}," +
                " showNotifs={3}," +
                " airport={4}",
        )
        fun data() = DATA
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = device,
        theme = when (isDarkMode) {
            true -> "android:ThemeOverlay.Material.Dark"
            false -> "android:Theme.Material.Light.NoActionBar"
        },
        thumbnailScale = ThumbnailScale.NoScale,
    )

    @get:Rule
    val instantAnimationsRule = InstantAnimationsRule()

    @Test
    fun screenshotMain() {
        paparazzi.snapshot {
            NYCAirportSecurityLineWaitsTheme(
                darkTheme = isDarkMode,
                dynamicColor = false,
            ) {
                Box(
                    Modifier
                        .padding(5.dp)
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    Column {
                        StatusBar(includeNotifs = includeNotifs)
                        Scaffold(
                            topBar = {
                                TitleAndBackBar(
                                    titleAirportCode = airportCode,
                                    NavHostController(paparazzi.context),
                                )
                            },
                        ) {
                            AirportScreen(
                                modifier = Modifier.padding(it),
                                connectivityState = ConnectionState.Available,
                                uiState = MainUiState.Valid(
                                    lastUpdated = System.currentTimeMillis(),
                                    airport = Airport(
                                        terminals = when (airportCode) {
                                            AirportCode.EWR -> persistentListOf(
                                                Terminal.EWR_A to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 7,
                                                        timePrecheck = 2,
                                                    ),
                                                ),
                                                Terminal.EWR_B to persistentListOf(
                                                    "40-47" to fakeQueues(
                                                        timeReg = 16,
                                                        timePrecheck = 2,
                                                    ),
                                                    "51-57" to fakeQueues(
                                                        timeReg = 11,
                                                    ),
                                                    "60-68" to fakeQueues(
                                                        timeReg = 5,
                                                    ),
                                                ),
                                                Terminal.EWR_C to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 14,
                                                        timePrecheck = 6,
                                                    ),
                                                ),
                                            )

                                            AirportCode.JFK -> persistentListOf(
                                                Terminal.JFK_1 to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 42,
                                                        timePrecheck = 8,
                                                    ),
                                                ),
                                                Terminal.JFK_4 to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 15,
                                                        timePrecheck = 5,
                                                    ),
                                                ),
                                                Terminal.JFK_5 to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 31,
                                                        timePrecheck = 10,
                                                    ),
                                                ),
                                                Terminal.JFK_7 to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 2,
                                                        timePrecheck = 8,
                                                    ),
                                                ),
                                                Terminal.JFK_8 to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 19,
                                                        timePrecheck = 14,
                                                    ),
                                                ),
                                            )

                                            AirportCode.LGA -> persistentListOf(
                                                Terminal.LGA_A to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 9,
                                                        timePrecheck = 6,
                                                    ),
                                                ),
                                                Terminal.LGA_B to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 15,
                                                        timePrecheck = 1,
                                                    ),
                                                ),
                                                Terminal.LGA_C to persistentListOf(
                                                    "All gates" to fakeQueues(
                                                        timeReg = 3,
                                                        timePrecheck = 0,
                                                    ),
                                                ),
                                            )

                                            else -> TODO()
                                        },
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fakeQueues(
        timeReg: Int = 7,
        timePrecheck: Int? = null,
    ): Queues = Queues(
        general = fakeQueue(
            queueType = QueueType.Reg,
            time = timeReg,
        ),
        preCheck = timePrecheck?.let {
            fakeQueue(
                queueType = QueueType.TSAPre,
                time = timePrecheck,
            )
        },
    )

    private fun fakeQueue(
        queueType: QueueType = QueueType.Reg,
        time: Int = 5,
    ): Queue = Queue(
        timeInMinutes = time,
        gate = "All gates",
        terminal = "A",
        queueType = queueType,
        queueOpen = true,
        updateTime = Date.from(Instant.now()),
        isWaitTimeAvailable = true,
        status = "Open",
    )
}
