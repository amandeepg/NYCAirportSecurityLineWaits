@file:OptIn(
    ExperimentalAnimationApi::class,
)

package ca.amandeep.nycairportsecuritylinewaits.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutBaseScope
import androidx.constraintlayout.compose.ConstraintSet
import ca.amandeep.nycairportsecuritylinewaits.R
import ca.amandeep.nycairportsecuritylinewaits.data.model.Queue
import ca.amandeep.nycairportsecuritylinewaits.data.model.QueueType
import ca.amandeep.nycairportsecuritylinewaits.data.model.Terminal
import ca.amandeep.nycairportsecuritylinewaits.ui.main.Airport
import ca.amandeep.nycairportsecuritylinewaits.ui.main.MainUiState
import ca.amandeep.nycairportsecuritylinewaits.ui.main.Queues
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.NYCAirportSecurityLineWaitsTheme
import ca.amandeep.nycairportsecuritylinewaits.ui.theme.surfaceColorAtElevation
import ca.amandeep.nycairportsecuritylinewaits.util.ConnectionState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Date
import java.util.concurrent.TimeUnit

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
fun AirportScreen(
    uiState: MainUiState.Valid,
    connectivityState: ConnectionState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp,
        ),
    ) {
        item {
            AnimatedVisibility(
                visible = uiState.hasError,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                ErrorBar(
                    connectivityState = connectivityState,
                    minsAgo = TimeUnit.MILLISECONDS.toMinutes(
                        System.currentTimeMillis() - uiState.lastUpdated,
                    ),
                )
            }
        }
        items(uiState.airport.terminals) { (terminal, gates) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                LoadedAirportCard(gates, terminal)
            }
        }
    }
}

private const val SHOW_TERMINAL_ICONS = false

@Composable
private fun LoadedAirportCard(
    gates: ImmutableList<Pair<String, Queues>>,
    terminal: Terminal,
) {
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    )
    val titleStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
    val singleGateGroup =
        gates.firstOrNull()?.first?.startsWith("all", ignoreCase = true) == true
    val allPrecheckClosed = gates.all { it.second.preCheck?.queueOpen != true }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row {
            if (terminal != Terminal.SWF_MAIN && SHOW_TERMINAL_ICONS) {
                Text(
                    terminal.identifier,
                    modifier = Modifier
                        .let { m ->
                            val whiteBorder = if (isSystemInDarkTheme()) {
                                LocalContentColor.current
                            } else {
                                OFFWHITE_BORDER
                            }
                            when (terminal) {
                                Terminal.EWR_A -> m.roundedTerminalHeader(TERMINAL_RED, whiteBorder)
                                Terminal.EWR_B -> m.roundedTerminalHeader(
                                    TERMINAL_BLUE,
                                    whiteBorder,
                                    shiftedDown = true,
                                )

                                Terminal.EWR_C -> m.roundedTerminalHeader(
                                    TERMINAL_GREEN,
                                    whiteBorder,
                                )

                                Terminal.JFK_1, Terminal.JFK_2, Terminal.LGA_B -> m.squareTerminalHeader(
                                    TERMINAL_GREEN,
                                    whiteBorder,
                                )

                                Terminal.JFK_4 -> m.squareTerminalHeader(TERMINAL_BLUE, whiteBorder)
                                Terminal.JFK_5 -> m.squareTerminalHeader(
                                    TERMINAL_YELLOW,
                                    Color.Black,
                                )

                                Terminal.JFK_7, Terminal.LGA_D -> m.squareTerminalHeader(
                                    TERMINAL_MUSTARD,
                                    Color.Black,
                                )

                                Terminal.JFK_8, Terminal.LGA_C -> m.squareTerminalHeader(
                                    TERMINAL_RED,
                                    whiteBorder,
                                )

                                Terminal.LGA_A -> m.squareTerminalHeader(TERMINAL_BLUE, whiteBorder)
                                Terminal.SWF_MAIN -> m
                            }
                        }
                        .badgeLayout(
                            offsetX = if (terminal == Terminal.EWR_B) 3.dp.value.toInt() else 0,
                        ),
                    fontSize = when (terminal) {
                        Terminal.EWR_A, Terminal.EWR_B, Terminal.EWR_C -> 20.sp
                        else -> 25.sp
                    },
                    color = when (terminal) {
                        Terminal.JFK_5,
                        Terminal.JFK_7,
                        Terminal.LGA_D,
                        -> Color.Black

                        else -> if (isSystemInDarkTheme()) LocalContentColor.current else Color.White
                    },
                    fontWeight = if (terminal == Terminal.EWR_B) FontWeight.SemiBold else FontWeight.Black,
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = when (terminal) {
                    Terminal.SWF_MAIN -> stringResource(R.string.main_terminal)
                    else -> stringResource(R.string.terminal) + terminal.identifier
                },
                modifier = Modifier.alignByBaseline(),
                style = titleStyle,
            )
            terminalSubtitleRes(terminal)?.let { subtitleRes ->
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(subtitleRes),
                    modifier = Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (singleGateGroup) {
            val queues = gates.firstOrNull()?.second
            if (queues != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    QueueSummary(
                        label = stringResource(R.string.general_line).uppercase(),
                        queue = queues.general,
                        labelStyle = labelStyle,
                        modifier = Modifier.weight(1f),
                    )
                    if (!allPrecheckClosed && queues.preCheck != null) {
                        QueueSummary(
                            label = stringResource(R.string.pre_short),
                            queue = queues.preCheck,
                            labelStyle = labelStyle,
                            modifier = Modifier.weight(1f),
                            showCheck = true,
                        )
                    }
                }
            }
        } else {
            val constraintSet = ConstraintSet {
                val generalLabel = createRefFor(GeneralId())
                val preLabel = createRefFor(PreId())
                val margin = COLUMNS_MARGIN.dp

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
                        if (i > 0) {
                            createBottomBarrier(
                                gateRefs[i - 1],
                                generalRefs[i - 1],
                                preRefs[i - 1],
                                margin = (-MIN_OFFSET / 2).dp,
                            )
                        } else {
                            null
                        }

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
            ConstraintLayout(
                constraintSet = constraintSet,
                Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text(
                    stringResource(R.string.gates).uppercase(),
                    Modifier.layoutId(GatesId()),
                    style = labelStyle,
                )
                GeneralLabel(labelStyle)
                if (!allPrecheckClosed) {
                    PrecheckLabel(labelStyle)
                }

                gates.forEachIndexed { i, (gate, queues) ->
                    Text(
                        if (gate.equals("All gates", ignoreCase = true)) {
                            stringResource(R.string.all)
                        } else {
                            gate
                                .replace(" - ", "-")
                                .replace(" - ", "-")
                                .replace("-", " - ")
                        },
                        Modifier
                            .layoutId(GatesId(i))
                            .alpha(if (queues.bothClosed) CLOSED_ALPHA else 1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    QueuesMins(i, queues, showEmptyPrecheckInGrid = true)
                }
            }
        }
    }
}

@Composable
private fun PrecheckLabel(
    labelStyle: TextStyle,
) {
    Row(
        modifier = Modifier.layoutId(PreId()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.pre_short),
            style = labelStyle,
        )
        Spacer(Modifier.width(4.dp))
        PrecheckBadge(
            color = labelStyle.color,
            contentDescription = stringResource(R.string.precheck),
        )
    }
}

@Composable
private fun GeneralLabel(
    labelStyle: TextStyle,
) {
    Text(
        stringResource(R.string.general_line).uppercase(),
        Modifier.layoutId(GeneralId()),
        style = labelStyle,
    )
}

@Composable
private fun QueueSummary(
    label: String,
    queue: Queue,
    labelStyle: TextStyle,
    modifier: Modifier = Modifier,
    showCheck: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = labelStyle)
            if (showCheck) {
                Spacer(Modifier.width(4.dp))
                PrecheckBadge(
                    color = labelStyle.color,
                    contentDescription = null,
                )
            }
        }
        if (queue.queueOpen) {
            Time(
                targetTime = queue.timeInMinutes,
            )
        } else {
            ClosedLabel(
                alpha = 1f,
                modifier = Modifier,
            )
        }
    }
}

@Composable
private fun PrecheckBadge(
    color: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val fillColor = color.copy(alpha = 0.18f)
    Box(
        modifier = modifier
            .size(14.dp)
            .background(fillColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = contentDescription,
            modifier = Modifier.size(9.dp),
            tint = color,
        )
    }
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
            y = -size.height * (factorY - 1f) / 2f - size.height * offsetFactorY,
        ),
        size = Size(size.width * factorX, size.height * factorY),
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
            },
        )
    }
}

private fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)

private fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)

private fun Modifier.squareTerminalHeader(backgroundColor: Color, foregroundColor: Color) =
    this
        .background(
            shape = RectangleShape,
            color = backgroundColor,
        )
        .border(
            0.5.dp,
            shape = RectangleShape,
            color = foregroundColor,
        )

private fun Modifier.badgeLayout(offsetX: Int = 0, offsetY: Int = 0) =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        val width = maxOf(placeable.width, placeable.height) - 18.sp.value.toInt()
        layout(width, width) {
            placeable.place(
                x = (width - placeable.width) / 2 + offsetX,
                y = (-18.sp.value.toInt() / 1.5f).toInt() + offsetY,
            )
        }
    }

private fun ConstraintLayoutBaseScope.createEndBarrier(
    elements: List<ConstrainedLayoutReference>,
    margin: Dp = 0.dp,
) = createEndBarrier(*elements.toTypedArray(), margin = margin)

@Composable
private fun QueuesMins(
    i: Int,
    queues: Queues,
    showEmptyPrecheckInGrid: Boolean,
) {
    if (queues.general.queueOpen) {
        Time(
            targetTime = queues.general.timeInMinutes,
            modifier = Modifier.layoutId(GeneralId(i)),
        )
    } else {
        ClosedLabel(
            modifier = Modifier.layoutId(GeneralId(i)),
            alpha = if (queues.bothClosed) CLOSED_ALPHA else 1f,
        )
    }
    if (queues.preCheck?.queueOpen == true) {
        Time(
            targetTime = queues.preCheck.timeInMinutes,
            modifier = Modifier.layoutId(PreId(i)),
        )
    } else if (showEmptyPrecheckInGrid) {
        Time(
            targetTime = -1,
            modifier = Modifier.layoutId(PreId(i)),
        )
    }
}

@Composable
private fun ClosedLabel(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    Text(
        text = stringResource(R.string.closed),
        modifier = modifier.alpha(alpha),
        style = MaterialTheme.typography.bodySmall.copy(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

@Composable
private fun Time(
    targetTime: Int,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = targetTime,
        modifier,
        label = "time crossfade",
    ) { time ->
        Row(
            modifier = Modifier.alpha(if (time < 0) 0f else 1f),
            verticalAlignment = Alignment.Bottom,
        ) {
            val color = waitTimeColor(time)
            Text(
                time.toString(),
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                ),
                maxLines = 1,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                when (time) {
                    1 -> stringResource(R.string.min)
                    else -> stringResource(R.string.mins)
                },
                color = color,
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Composable
private fun waitTimeColor(
    time: Int,
): Color = when {
    time < 10 -> if (isSystemInDarkTheme()) {
        Color(129, 199, 132, 255)
    } else {
        Color(56, 142, 60, 255)
    }

    time < 25 -> if (isSystemInDarkTheme()) {
        Color(255, 241, 118, 255)
    } else {
        Color(251, 192, 45, 255)
    }

    else -> if (isSystemInDarkTheme()) {
        Color(229, 115, 115, 255)
    } else {
        Color(211, 47, 47, 255)
    }
}

private fun terminalSubtitleRes(
    terminal: Terminal,
): Int? = when (terminal) {
    Terminal.LGA_A -> R.string.terminal_subtitle_lga_a
    Terminal.LGA_B -> R.string.terminal_subtitle_lga_b
    Terminal.LGA_C -> R.string.terminal_subtitle_lga_c
    Terminal.EWR_A -> R.string.terminal_subtitle_ewr_a
    Terminal.EWR_B -> R.string.terminal_subtitle_ewr_b
    Terminal.EWR_C -> R.string.terminal_subtitle_ewr_c
    Terminal.JFK_1 -> R.string.terminal_subtitle_jfk_1
    Terminal.JFK_4 -> R.string.terminal_subtitle_jfk_4
    Terminal.JFK_5 -> R.string.terminal_subtitle_jfk_5
    Terminal.JFK_7 -> R.string.terminal_subtitle_jfk_7
    Terminal.JFK_8 -> R.string.terminal_subtitle_jfk_8
    else -> null
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AirportPreview() {
    NYCAirportSecurityLineWaitsTheme {
        Surface {
            AirportScreen(
                MainUiState.Valid(
                    lastUpdated = System.currentTimeMillis(),
                    airport = Airport(
                        persistentListOf(
                            Terminal.JFK_1 to persistentListOf(
                                "10-18" to Queues(
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
                                    ),
                                ),
                                "20-28" to Queues(
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
                                    ),
                                ),
                                "30-39" to Queues(
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
                                    ),
                                ),
                            ),
                            Terminal.EWR_A to persistentListOf(),
                            Terminal.EWR_B to persistentListOf(),
                            Terminal.EWR_C to persistentListOf(),
                            Terminal.JFK_4 to persistentListOf(
                                "All" to Queues(
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
                                    ),
                                ),
                            ),
                            Terminal.JFK_7 to persistentListOf(
                                "All" to Queues(
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
                                    ),
                                ),
                            ),
                            Terminal.LGA_C to persistentListOf(),
                            Terminal.LGA_D to persistentListOf(),
                        ),
                    ),
                ),
                ConnectionState.Available,
            )
        }
    }
}
