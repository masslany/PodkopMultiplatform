package pl.masslany.podkop.features.entrydetails

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Which thread lines pass through a row. Level `k` is the
 * column under the avatar of the row's ancestor at depth `k`.
 */
data class ThreadConnectors(
    /** For levels `0 until depth - 1`: whether that ancestor's line runs on past this row. */
    val ancestorLines: List<Boolean> = emptyList(),
    /** Whether the parent's line runs on below this row's elbow, to a later sibling. */
    val continuesBelow: Boolean = false,
    /** Whether a line hangs from this row's own avatar down to its replies. */
    val hasReplies: Boolean = false,
)

/** Where a thread row's content starts: the list gutter plus one step per reply level. */
internal val ThreadStartPadding = 16.dp
internal val ThreadIndent = 28.dp

// Avatars are 36 dp with a 2 dp gender bar 4 dp below, placed after the row's 16 dp top padding.
private val AvatarHalfWidth = 18.dp
private val AvatarBottom = 16.dp + 42.dp
private val ElbowRadius = 8.dp
private val ElbowGap = 2.dp
private val LineWidth = 1.5.dp

internal fun threadContentStart(depth: Int): Dp = ThreadStartPadding + ThreadIndent * depth

/**
 * Draws reply connectors behind a full-width thread row: lines under ancestor
 * avatars that run on to later siblings, a rounded elbow from the parent's line into this row at
 * [elbowY], and a line down from this row's own avatar when it has replies.
 */
internal fun Modifier.threadConnectors(
    depth: Int,
    connectors: ThreadConnectors,
    elbowY: Dp,
    color: Color,
): Modifier = drawBehind {
    val stroke = LineWidth.toPx()
    fun columnX(level: Int): Float = (threadContentStart(level) + AvatarHalfWidth).toPx()
    fun verticalLine(x: Float, fromY: Float) {
        drawLine(color = color, start = Offset(x, fromY), end = Offset(x, size.height), strokeWidth = stroke)
    }

    connectors.ancestorLines.forEachIndexed { level, runsOn ->
        if (runsOn) verticalLine(columnX(level), fromY = 0f)
    }

    if (depth > 0) {
        val x = columnX(depth - 1)
        val y = elbowY.toPx()
        val radius = ElbowRadius.toPx()
        val elbow = Path().apply {
            moveTo(x, 0f)
            lineTo(x, y - radius)
            quadraticTo(x, y, x + radius, y)
            lineTo((threadContentStart(depth) - ElbowGap).toPx(), y)
        }
        drawPath(path = elbow, color = color, style = Stroke(width = stroke))
        if (connectors.continuesBelow) verticalLine(x, fromY = y - radius)
    }

    if (connectors.hasReplies) verticalLine(columnX(depth), fromY = AvatarBottom.toPx())
}
