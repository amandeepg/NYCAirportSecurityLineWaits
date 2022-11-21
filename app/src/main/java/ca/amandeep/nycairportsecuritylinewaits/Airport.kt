@file:OptIn(
    ExperimentalAnimationApi::class,
)

package ca.amandeep.nycairportsecuritylinewaits

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutBaseScope
import androidx.constraintlayout.compose.ConstraintSet
import ca.amandeep.nycairportsecuritylinewaits.MainViewModel.Terminal.*
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.Card3
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.NYCAirportSecurityLineWaitsTheme
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.Typography
import java.util.*

private const val MIN_OFFSET = 5
private const val COLUMNS_MARGIN = 8

private const val CLOSED_ALPHA = 0.3f

private val OFFWHITE_BORDER = Color(0xFFF6F6F6)

private val TERMINAL_RED = Color(200, 60, 43)
private val TERMINAL_BLUE = Color(18, 50, 154)
private val TERMINAL_GREEN = Color(79, 174, 79)
private val TERMINAL_YELLOW = Color(248, 207, 70)
private val TERMINAL_MUSTARD = Color(243, 167, 59)

private data class GatesId(val i: Int = -1)
private data class GeneralId(val i: Int = -1)
private data class PreId(val i: Int = -1)

@Composable
fun Airport(
    airport: MainViewModel.Airport,
    innerPadding: PaddingValues
) {
    Crossfade(targetState = airport.terminals.isEmpty()) { isLoading ->
        when (isLoading) {
            true -> Loading()
            false ->
                LazyColumn(
                    Modifier
                        .fillMaxHeight()
                        .padding(innerPadding)
                ) {
                    items(airport.terminals) { (terminal, gates) ->
                        Card3(
                            Modifier
                                .padding(horizontal = 15.dp, vertical = 9.dp),
                            elevation = 5.dp
                        ) {
                            LoadedAirportCard(gates, terminal)
                        }
                    }
                }
        }
    }
}

@Composable
private fun LoadedAirportCard(
    gates: List<Pair<String, MainViewModel.Queues>>,
    terminal: MainViewModel.Terminal
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (terminal != SWF_MAIN) {
                Text(
                    terminal.identifier,
                    modifier = Modifier
                        .let { m ->
                            val whiteBorder =
                                if (isSystemInDarkTheme()) LocalContentColor.current
                                else OFFWHITE_BORDER
                            when (terminal) {
                                EWR_A -> m.roundedTerminalHeader(TERMINAL_RED, whiteBorder)
                                EWR_B -> m.roundedTerminalHeader(
                                    TERMINAL_BLUE,
                                    whiteBorder,
                                    shiftedDown = true
                                )
                                EWR_C -> m.roundedTerminalHeader(TERMINAL_GREEN, whiteBorder)
                                JFK_1,
                                JFK_2,
                                LGA_B -> m.squareTerminalHeader(TERMINAL_GREEN, whiteBorder)
                                JFK_4 -> m.squareTerminalHeader(TERMINAL_BLUE, whiteBorder)
                                JFK_5 -> m.squareTerminalHeader(TERMINAL_YELLOW, Color.Black)
                                JFK_7,
                                LGA_D -> m.squareTerminalHeader(TERMINAL_MUSTARD, Color.Black)
                                JFK_8,
                                LGA_C -> m.squareTerminalHeader(TERMINAL_RED, whiteBorder)
                                LGA_A -> m.squareTerminalHeader(TERMINAL_BLUE, whiteBorder)
                                SWF_MAIN -> m
                            }
                        }
                        .badgeLayout(
                            offsetX = if (terminal == EWR_B) 3.dp.value.toInt() else 0
                        ),
                    fontSize = when (terminal) {
                        EWR_A, EWR_B, EWR_C -> 20.sp
                        else -> 25.sp
                    },
                    color = when (terminal) {
                        JFK_5,
                        JFK_7,
                        LGA_D -> Color.Black
                        else -> if (isSystemInDarkTheme()) LocalContentColor.current else Color.White
                    },
                    fontWeight = if (terminal == EWR_B) FontWeight.SemiBold else FontWeight.Black
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                when (terminal) {
                    SWF_MAIN -> "Main Terminal"
                    else -> "Terminal ${terminal.identifier}"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        val gateHeaderStyle = Typography.labelMedium.copy(
            color = Typography.labelMedium.color
        )
        val singleGateGroup =
            gates.firstOrNull()?.first?.startsWith("all", ignoreCase = true) == true
        val allPrecheckClosed = gates.all { it.second.preCheck?.queueOpen != true }

        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        if (false && singleGateGroup && allPrecheckClosed) {
            Box(Modifier.padding(10.dp)) {
                QueuesMins(0, gates.first().second, showEmptyPrecheckInGrid = false)
            }
        } else {
            val constraintSet = ConstraintSet {
                val generalLabel = createRefFor(GeneralId())
                val preLabel = createRefFor(PreId())
                val margin = COLUMNS_MARGIN.dp

                if (singleGateGroup) {
                    val generalTimeRef = createRefFor(GeneralId(0))
                    val preTimeRef = createRefFor(PreId(0))

                    val endGeneral = createEndBarrier(generalTimeRef, generalLabel, margin = margin)
                    val endPre = createEndBarrier(preTimeRef, preLabel, margin = margin)

                    constrain(generalTimeRef) {
                        start.linkTo(parent.start)
                        end.linkTo(endGeneral)
                        top.linkTo(generalLabel.bottom)
                        bottom.linkTo(preTimeRef.bottom)
                    }
                    constrain(preTimeRef) {
                        start.linkTo(endGeneral)
                        end.linkTo(endPre)
                        top.linkTo(generalLabel.bottom)
                        bottom.linkTo(generalTimeRef.bottom)
                    }

                    constrain(generalLabel) {
                        start.linkTo(parent.start)
                        top.linkTo(preLabel.top)
                        bottom.linkTo(preLabel.bottom)
                    }
                    constrain(preLabel) {
                        start.linkTo(endGeneral)
                        end.linkTo(endPre)
                        top.linkTo(parent.top)
                        bottom.linkTo(generalLabel.bottom)
                    }
                } else {
                    val gatesLabel = createRefFor(GatesId())

                    val gateRefs =
                        gates.indices.map { createRefFor(GatesId(it)) }
                    val generalRefs =
                        gates.indices.map { createRefFor(GeneralId(it)) }
                    val preRefs =
                        gates.indices.map { createRefFor(PreId(it)) }

                    val endGate =
                        createEndBarrier(gateRefs + gatesLabel, margin)
                    val endGeneral =
                        createEndBarrier(generalRefs + generalLabel, margin)
                    val endPre =
                        createEndBarrier(preRefs + preLabel, margin)

                    gates.indices.forEach { i ->
                        val bottomBarrier =
                            if (i > 0) createBottomBarrier(
                                gateRefs[i - 1],
                                generalRefs[i - 1],
                                preRefs[i - 1],
                                margin = (-MIN_OFFSET / 2).dp
                            ) else null

                        constrain(gateRefs[i]) {
                            top.linkTo(bottomBarrier ?: gatesLabel.bottom)
                            start.linkTo(parent.start)
                            bottom.linkTo(generalRefs[i].bottom)
                            bottom.linkTo(preRefs[i].bottom)
                        }
                        constrain(generalRefs[i]) {
                            top.linkTo(bottomBarrier ?: gatesLabel.bottom)
                            start.linkTo(endGate)
                            end.linkTo(endGeneral)
                            bottom.linkTo(gateRefs[i].bottom)
                            bottom.linkTo(preRefs[i].bottom)
                        }
                        constrain(preRefs[i]) {
                            top.linkTo(bottomBarrier ?: gatesLabel.bottom)
                            start.linkTo(endGeneral)
                            end.linkTo(endPre)
                            bottom.linkTo(gateRefs[i].bottom)
                            bottom.linkTo(generalRefs[i].bottom)
                        }
                    }

                    constrain(gatesLabel) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    constrain(generalLabel) {
                        top.linkTo(gatesLabel.top)
                        start.linkTo(endGate ?: parent.start)
                        end.linkTo(endGeneral)
                    }
                    constrain(preLabel) {
                        top.linkTo(gatesLabel.top)
                        bottom.linkTo(gatesLabel.bottom)
                        start.linkTo(endGeneral)
                        end.linkTo(endPre)
                    }
                }
            }
            ConstraintLayout(
                constraintSet = constraintSet,
                Modifier.padding(10.dp)
            ) {
                if (!singleGateGroup)
                    Text(
                        "Gates",
                        Modifier
                            .layoutId(GatesId())
                            .alpha(0.7f),
                        style = gateHeaderStyle
                    )
                GeneralLabel(gateHeaderStyle)
                if (!allPrecheckClosed)
                    PrecheckLabel()

                gates.forEachIndexed { i, (gate, queues) ->
                    if (!singleGateGroup)
                        Text(
                            if (gate.equals("All gates", ignoreCase = true)) "All" else gate,
                            Modifier
                                .layoutId(GatesId(i))
                                .alpha(if (queues.bothClosed) CLOSED_ALPHA else 1f),
                        )
                    QueuesMins(i, queues, showEmptyPrecheckInGrid = true)
                }
            }
        }
    }
}

@Composable
private fun PrecheckLabel() {
    Icon(
        modifier = Modifier
            .layoutId(PreId())
            .height(16.dp)
            .widthIn(min = 40.dp)
            .alpha(0.7f),
        painter = painterResource(id = R.drawable.ic_pre),
        contentDescription = "PreCheck",
    )
}

@Composable
private fun GeneralLabel(
    gateHeaderStyle: TextStyle
) {
    Text(
        "General Line",
        Modifier
            .layoutId(GeneralId())
            .alpha(0.7f),
        style = gateHeaderStyle
    )
}

private fun Modifier.roundedTerminalHeader(
    backgroundColor: Color,
    foregroundColor: Color,
    shiftedDown: Boolean = false,
) =
    drawBehind {
        drawStrokedRoundedTriangle(backgroundColor, foregroundColor, shiftedDown)
    }

private fun DrawScope.drawStrokedRoundedTriangle(
    backgroundColor: Color,
    foregroundColor: Color,
    shiftedDown: Boolean,
) {
    drawRoundedTriangle(
        backgroundColor = backgroundColor,
        shiftedDown = shiftedDown,
    )
    drawRoundedTriangle(
        factor = 0.97f,
        backgroundColor = foregroundColor,
        shiftedDown = shiftedDown,
    )
    drawRoundedTriangle(
        factor = 0.86f,
        backgroundColor = backgroundColor,
        shiftedDown = shiftedDown,
    )
}

private fun DrawScope.drawRoundedTriangle(
    factor: Float = 1f,
    backgroundColor: Color,
    shiftedDown: Boolean,
) {
    val factorX = 2.2f * factor
    val factorY = 2.0f * factor
    val offsetFactorY = (if (shiftedDown) 0.39f else 0.32f) * factor
    val rect = Rect(
        offset = Offset(
            x = -size.width * (factorX - 1f) / 2f,
            y = -size.height * (factorY - 1f) / 2f - size.height * offsetFactorY
        ),
        size = Size(size.width * factorX, size.height * factorY)
    )
    val trianglePath = Path().apply {
        moveTo(rect.topCenter)
        lineTo(rect.bottomRight)
        lineTo(rect.bottomLeft)
        // note that two more point repeats needed to round all corners
        lineTo(rect.topCenter)
        lineTo(rect.bottomRight)
    }

    drawIntoCanvas { canvas ->
        canvas.drawOutline(
            outline = Outline.Generic(trianglePath),
            paint = Paint().apply {
                color = backgroundColor
                pathEffect =
                    PathEffect.cornerPathEffect(rect.maxDimension / 1.5f)
            }
        )
    }
}

fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)
fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)

fun Modifier.squareTerminalHeader(backgroundColor: Color, foregroundColor: Color) =
    background(
        shape = RectangleShape,
        color = backgroundColor
    ).border(
        0.5.dp,
        shape = RectangleShape,
        color = foregroundColor
    )

fun Modifier.badgeLayout(offsetX: Int = 0, offsetY: Int = 0) =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        val width = maxOf(placeable.width, placeable.height) - 18.sp.value.toInt()
        layout(width, width) {
            placeable.place(
                x = (width - placeable.width) / 2 + offsetX,
                y = (-18.sp.value.toInt() / 1.5f).toInt() + offsetY
            )
        }
    }

@Composable
private fun Loading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(10.dp))
        Text("Loading …", color = MaterialTheme.colorScheme.secondary)
    }
}

fun ConstraintLayoutBaseScope.createEndBarrier(
    elements: List<ConstrainedLayoutReference>,
    margin: Dp = 0.dp
) = createEndBarrier(*elements.toTypedArray(), margin = margin)

@Composable
private fun QueuesMins(
    i: Int,
    queues: MainViewModel.Queues,
    showEmptyPrecheckInGrid: Boolean,
) {
    if (queues.general.queueOpen)
        Time(GeneralId(i), queues.general.timeInMinutes)
    else
        Text(
            "Closed",
            Modifier
                .layoutId(GeneralId(i))
                .alpha(if (queues.bothClosed) CLOSED_ALPHA else 1f),
            fontSize = 18.sp,
            maxLines = 1,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold
        )
    if (queues.preCheck?.queueOpen == true)
        Time(PreId(i), queues.preCheck.timeInMinutes)
    else if (showEmptyPrecheckInGrid)
        Time(PreId(i), -1)
}

@Composable
fun Time(
    id: Any,
    targetTime: Int
) {
    Crossfade(
        targetState = targetTime,
        Modifier.layoutId(id)
    ) { time ->
        Column(
            modifier = Modifier.alpha(if (time < 0) 0f else 1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val color = when {
                time < 10 -> if (isSystemInDarkTheme())
                    Color(129, 199, 132, 255)
                else
                    Color(56, 142, 60, 255)
                time < 25 -> if (isSystemInDarkTheme())
                    Color(255, 241, 118, 255)
                else
                    Color(251, 192, 45, 255)
                else -> if (isSystemInDarkTheme())
                    Color(229, 115, 115, 255)
                else
                    Color(211, 47, 47, 255)
            }
            Text(
                time.toString(),
                fontSize = 30.sp,
                color = color,
                maxLines = 1,
                fontWeight = FontWeight.Black,
            )
            Text(
                when (time) {
                    1 -> "min"
                    else -> "mins"
                },
                color = color,
                modifier = Modifier
                    .offset(y = (-MIN_OFFSET).dp)
                    .animateContentSize(),
                style = Typography.labelSmall.copy(
                    fontWeight = FontWeight.Light
                )
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AirportPreviewDark() = AirportPreview()

@Preview
@Composable
fun AirportPreviewLight() = AirportPreview()

@Composable
fun AirportPreview() {
    NYCAirportSecurityLineWaitsTheme {
        Airport(
            MainViewModel.Airport(
                listOf(
                    JFK_1 to listOf(
                        "10-18" to MainViewModel.Queues(
                            general = Queue(
                                timeInMinutes = 50,
                                gate = "10-18",
                                terminal = "1",
                                queueType = QueueType.Reg,
                                queueOpen = false,
                                updateTime = Date(),
                                isWaitTimeAvailable = false,
                                status = "Open",
                            ),
                            preCheck = Queue(
                                timeInMinutes = 2,
                                gate = "10-18",
                                terminal = "1",
                                queueType = QueueType.TSAPre,
                                queueOpen = false,
                                updateTime = Date(),
                                isWaitTimeAvailable = false,
                                status = "Open",
                            )
                        ),
                        "20-28" to MainViewModel.Queues(
                            general = Queue(
                                timeInMinutes = 50,
                                gate = "10-18",
                                terminal = "1",
                                queueType = QueueType.Reg,
                                queueOpen = true,
                                updateTime = Date(),
                                isWaitTimeAvailable = true,
                                status = "Open",
                            ),
                            preCheck = Queue(
                                timeInMinutes = 20,
                                gate = "10-18",
                                terminal = "1",
                                queueType = QueueType.TSAPre,
                                queueOpen = true,
                                updateTime = Date(),
                                isWaitTimeAvailable = true,
                                status = "Open",
                            )
                        ),
                        "30-39" to MainViewModel.Queues(
                            general = Queue(
                                timeInMinutes = 50,
                                gate = "10-18",
                                terminal = "1",
                                queueType = QueueType.Reg,
                                queueOpen = true,
                                updateTime = Date(),
                                isWaitTimeAvailable = true,
                                status = "Open",
                            ),
                            preCheck = Queue(
                                timeInMinutes = 2,
                                gate = "10-18",
                                terminal = "1",
                                queueType = QueueType.TSAPre,
                                queueOpen = true,
                                updateTime = Date(),
                                isWaitTimeAvailable = true,
                                status = "Open",
                            )
                        ),
                    ),
                    EWR_A to listOf(
                    ),
                    EWR_B to listOf(
                    ),
                    EWR_C to listOf(
                    ),
                    JFK_4 to listOf(
                        "All" to MainViewModel.Queues(
                            general = Queue(
                                timeInMinutes = 50,
                                gate = "All",
                                terminal = "1",
                                queueType = QueueType.Reg,
                                queueOpen = true,
                                updateTime = Date(),
                                isWaitTimeAvailable = false,
                                status = "Open",
                            ),
                            preCheck = Queue(
                                timeInMinutes = 2,
                                gate = "All",
                                terminal = "1",
                                queueType = QueueType.TSAPre,
                                queueOpen = false,
                                updateTime = Date(),
                                isWaitTimeAvailable = false,
                                status = "Open",
                            )
                        ),
                    ),
                    JFK_7 to listOf(

                        "All" to MainViewModel.Queues(
                            general = Queue(
                                timeInMinutes = 50,
                                gate = "All",
                                terminal = "1",
                                queueType = QueueType.Reg,
                                queueOpen = true,
                                updateTime = Date(),
                                isWaitTimeAvailable = false,
                                status = "Open",
                            ),
                            preCheck = Queue(
                                timeInMinutes = 2,
                                gate = "All",
                                terminal = "1",
                                queueType = QueueType.TSAPre,
                                queueOpen = true,
                                updateTime = Date(),
                                isWaitTimeAvailable = false,
                                status = "Open",
                            )
                        ),
                    ),
                    LGA_C to listOf(
                    ),
                    LGA_D to listOf(
                    ),
                )
            ),
            innerPadding = PaddingValues(5.dp)
        )
    }
}