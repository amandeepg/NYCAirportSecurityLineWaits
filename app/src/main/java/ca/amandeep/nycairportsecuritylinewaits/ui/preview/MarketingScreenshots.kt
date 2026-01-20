package ca.amandeep.nycairportsecuritylinewaits.ui.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import ca.amandeep.nycairportsecuritylinewaits.data.AirportCode
import ca.amandeep.nycairportsecuritylinewaits.data.model.Queue
import ca.amandeep.nycairportsecuritylinewaits.data.model.QueueType
import ca.amandeep.nycairportsecuritylinewaits.data.model.Terminal
import ca.amandeep.nycairportsecuritylinewaits.ui.AirportScreen
import ca.amandeep.nycairportsecuritylinewaits.ui.main.Airport
import ca.amandeep.nycairportsecuritylinewaits.ui.main.MainUiState
import ca.amandeep.nycairportsecuritylinewaits.ui.main.Queues
import ca.amandeep.nycairportsecuritylinewaits.ui.main.Selection
import ca.amandeep.nycairportsecuritylinewaits.ui.main.TitleAndBackBar
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.NYCAirportSecurityLineWaitsTheme
import ca.amandeep.nycairportsecuritylinewaits.util.ConnectionState
import kotlinx.collections.immutable.persistentListOf
import java.util.Date

@Composable
fun MarketingSelectionPreview() {
    DiagonalSplitDeviceScreenshot { darkTheme ->
        MarketingSelectionSingleThemePreview(darkTheme = darkTheme)
    }
}

@Composable
fun MarketingSelectionSingleThemePreview(
    darkTheme: Boolean,
) {
    MarketingScaffold(
        darkTheme = darkTheme,
        titleAirportCode = null,
    ) { padding ->
        Selection(
            modifier = Modifier.padding(padding),
            navigateTo = {},
        )
    }
}

@Composable
fun MarketingEwrPreview() = MarketingAirportPreview(AirportCode.EWR)

@Composable
fun MarketingJfkPreview() = MarketingAirportPreview(AirportCode.JFK)

@Composable
fun MarketingLgaPreview() = MarketingAirportPreview(AirportCode.LGA)

@Composable
private fun MarketingAirportPreview(
    airportCode: AirportCode,
) {
    MarketingScaffold(
        titleAirportCode = airportCode,
    ) { padding ->
        AirportScreen(
            modifier = Modifier.padding(padding),
            connectivityState = ConnectionState.Available,
            uiState = MainUiState.Valid(
                lastUpdated = FIXED_TIME_MILLIS,
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

                        else -> persistentListOf()
                    },
                ),
            ),
        )
    }
}

@Composable
private fun MarketingScaffold(
    darkTheme: Boolean = isSystemInDarkTheme(),
    titleAirportCode: AirportCode?,
    content: @Composable (PaddingValues) -> Unit,
) {
    NYCAirportSecurityLineWaitsTheme(
        darkTheme = darkTheme,
        dynamicColor = false,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
            ) {
                SyntheticStatusBar()
                Scaffold(
                    modifier = Modifier.weight(1f),
                    topBar = {
                        TitleAndBackBar(
                            titleAirportCode = titleAirportCode,
                            navController = rememberNavController(),
                        )
                    },
                ) { padding ->
                    content(padding)
                }
            }
        }
    }
}

@Composable
private fun DiagonalSplitDeviceScreenshot(
    content: @Composable (Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .diagonalClip(clipTopRight = true),
        ) {
            content(isSystemInDarkTheme())
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .diagonalClip(clipTopRight = false),
        ) {
            content(!isSystemInDarkTheme())
        }
    }
}

private fun Modifier.diagonalClip(clipTopRight: Boolean): Modifier = drawWithContent {
    val path = Path().apply {
        if (clipTopRight) {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
        } else {
            moveTo(0f, 0f)
            lineTo(0f, size.height)
            lineTo(size.width, size.height)
        }
        close()
    }
    clipPath(path) {
        this@drawWithContent.drawContent()
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
    updateTime = FIXED_DATE,
    isWaitTimeAvailable = true,
    status = "Open",
)

private const val FIXED_TIME_MILLIS = 1710000000000L
private val FIXED_DATE = Date(FIXED_TIME_MILLIS)
