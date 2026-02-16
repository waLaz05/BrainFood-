package com.wlaz.brainfood.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wlaz.brainfood.ui.backpack.getIngredientEmoji

/**
 * Ingredient icon system:
 * - Custom Canvas drawings for ingredients that have poor/inaccurate emojis.
 * - Emoji fallback for items with good native emoji representations.
 */
@Composable
fun IngredientIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    emojiSize: TextUnit = 24.sp
) {
    val lowerName = name.lowercase()

    val drawFunction: (DrawScope.() -> Unit)? = when {
        // Cebollas
        lowerName == "cebolla roja" -> {{ drawOnion(Color(0xFF7B2D8E), Color(0xFFAB47BC)) }}
        lowerName.contains("cebolla") -> {{ drawOnion(Color(0xFFC88A2A), Color(0xFFF1C40F)) }}

        // Ajíes peruanos
        lowerName == "ají amarillo" || lowerName == "aji amarillo" -> {{ drawChiliPepper(Color(0xFFFFC107), Color(0xFFFF8F00)) }}
        lowerName == "ají panca" || lowerName == "aji panca" -> {{ drawChiliPepper(Color(0xFF6D4C41), Color(0xFF8D6E63)) }}
        lowerName == "rocoto" -> {{ drawRocoto() }}
        lowerName.contains("pimiento") -> {{ drawBellPepper() }}

        // Cítricos
        lowerName.contains("limón") || lowerName.contains("limon") -> {{ drawCitrus(Color(0xFF558B2F), Color(0xFF7CB342), Color(0xFFC5E1A5)) }}
        lowerName.contains("lima") -> {{ drawCitrus(Color(0xFFF9A825), Color(0xFFFFEB3B), Color(0xFFFFF9C4)) }}

        // Hojas verdes
        lowerName.contains("lechuga") -> {{ drawLeaf(Color(0xFF66BB6A), Color(0xFF81C784), Color(0xFFA5D6A7)) }}
        lowerName.contains("espinaca") -> {{ drawLeaf(Color(0xFF2E7D32), Color(0xFF388E3C), Color(0xFF4CAF50)) }}
        lowerName.contains("albahaca") -> {{ drawLeaf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C)) }}

        // Hierbas
        lowerName.contains("cilantro") || lowerName.contains("culantro") -> {{ drawHerbSprig(Color(0xFF43A047)) }}
        lowerName.contains("perejil") -> {{ drawHerbSprig(Color(0xFF2E7D32)) }}
        lowerName.contains("huacatay") -> {{ drawHerbSprig(Color(0xFF33691E)) }}
        lowerName.contains("orégano") || lowerName.contains("oregano") -> {{ drawDriedHerb(Color(0xFF8D6E63)) }}

        // Carnes
        lowerName.contains("pollo") -> {{ drawChickenLeg() }}
        lowerName.contains("lomo") || lowerName == "carne molida" -> {{ drawSteak() }}
        lowerName.contains("cerdo") -> {{ drawPorkCut() }}

        // Huevo
        lowerName.contains("huevo") -> {{ drawEgg() }}

        // Especias problemáticas
        lowerName.contains("comino") -> {{ drawSpicePowder(Color(0xFFD4A537), Color(0xFFBF8C2A)) }}
        lowerName.contains("pimienta") -> {{ drawPeppercorns() }}

        // Nuevos Iconos Solicitados
        lowerName.contains("aceite") -> {{ drawOilBottle() }}
        lowerName.contains("mayonesa") -> {{ drawMayoPacket() }}
        lowerName.contains("apio") -> {{ drawCelery() }}
        lowerName.contains("yuca") -> {{ drawYuca() }}

        else -> null
    }

    if (drawFunction != null) {
        Canvas(modifier = modifier.size(size)) {
            drawFunction()
        }
    } else {
        Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
            Text(text = getIngredientEmoji(name), fontSize = emojiSize)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  VEGETABLES
// ═══════════════════════════════════════════════════════════════════

private fun DrawScope.drawCelery() {
    val w = size.width; val h = size.height

    // Stalks (tallos)
    val stalk1 = Path().apply {
        moveTo(w * 0.45f, h * 0.9f)
        quadraticTo(w * 0.4f, h * 0.5f, w * 0.35f, h * 0.2f)
        lineTo(w * 0.45f, h * 0.2f)
        quadraticTo(w * 0.5f, h * 0.5f, w * 0.55f, h * 0.9f)
        close()
    }
    drawPath(stalk1, Color(0xFFAED581)) // Light green
    
    val stalk2 = Path().apply {
        moveTo(w * 0.55f, h * 0.95f)
        quadraticTo(w * 0.6f, h * 0.5f, w * 0.65f, h * 0.25f)
        lineTo(w * 0.75f, h * 0.25f)
        quadraticTo(w * 0.7f, h * 0.6f, w * 0.65f, h * 0.95f)
        close()
    }
    drawPath(stalk2, Color(0xFF9CCC65))

    // Leaves at top
    drawCircle(Color(0xFF558B2F), radius = w * 0.1f, center = Offset(w * 0.35f, h * 0.15f))
    drawCircle(Color(0xFF689F38), radius = w * 0.08f, center = Offset(w * 0.45f, h * 0.1f))
    drawCircle(Color(0xFF558B2F), radius = w * 0.1f, center = Offset(w * 0.7f, h * 0.2f))
    
    // Ribs on stalks
    drawLine(Color(0xFF33691E).copy(alpha = 0.3f), Offset(w * 0.48f, h * 0.8f), Offset(w * 0.42f, h * 0.3f), strokeWidth = 1.dp.toPx())
}

private fun DrawScope.drawYuca() {
    val w = size.width; val h = size.height
    
    // Long root shape
    val yuca = Path().apply {
        moveTo(w * 0.2f, h * 0.2f)
        quadraticTo(w * 0.8f, h * 0.3f, w * 0.85f, h * 0.8f) // Tip
        quadraticTo(w * 0.5f, h * 0.9f, w * 0.15f, h * 0.4f) // Thick end
        close()
    }
    drawPath(yuca, Color(0xFF5D4037)) // Dark brown skin

    // Texture (rough skin)
    drawLine(Color.Black.copy(alpha = 0.2f), Offset(w * 0.25f, h * 0.3f), Offset(w * 0.3f, h * 0.35f), strokeWidth = 1.dp.toPx())
    drawLine(Color.Black.copy(alpha = 0.2f), Offset(w * 0.4f, h * 0.5f), Offset(w * 0.45f, h * 0.55f), strokeWidth = 1.dp.toPx())
    
    // Cut end (showing white flesh)
    rotate(-15f) {
        drawOval(Color(0xFFFAFAFA), topLeft = Offset(w * 0.1f, h * 0.15f), size = Size(w * 0.2f, h * 0.25f))
        drawOval(Color(0xFF5D4037), topLeft = Offset(w * 0.1f, h * 0.15f), size = Size(w * 0.2f, h * 0.25f), style = Stroke(1.dp.toPx()))
    }
}

private fun DrawScope.drawOilBottle() {
    val w = size.width; val h = size.height
    val cx = w / 2

    // Bottle Body
    val body = Path().apply {
        moveTo(w * 0.3f, h * 0.35f)
        lineTo(w * 0.7f, h * 0.35f)
        quadraticTo(w * 0.8f, h * 0.4f, w * 0.8f, h * 0.9f)
        lineTo(w * 0.2f, h * 0.9f)
        quadraticTo(w * 0.2f, h * 0.4f, w * 0.3f, h * 0.35f)
    }
    drawPath(body, Color(0xFFFFEB3B).copy(alpha = 0.9f)) // Oil color

    // Bottle Neck
    drawRect(Color(0xFFFFEB3B).copy(alpha = 0.6f), topLeft = Offset(w * 0.4f, h * 0.15f), size = Size(w * 0.2f, h * 0.2f))
    
    // Cap
    drawRect(Color(0xFFD32F2F), topLeft = Offset(w * 0.38f, h * 0.1f), size = Size(w * 0.24f, h * 0.08f))

    // Highlights (Plastic sheen)
    val highlight = Path().apply {
        moveTo(w * 0.25f, h * 0.4f)
        lineTo(w * 0.25f, h * 0.8f)
    }
    drawPath(highlight, Color.White.copy(alpha = 0.4f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    
    // Label (generic)
    drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(w * 0.3f, h * 0.5f), size = Size(w * 0.4f, h * 0.2f))
}

private fun DrawScope.drawMayoPacket() {
    val w = size.width; val h = size.height
    val cx = w / 2

    // Doypack shape (like Alacena)
    val packet = Path().apply {
        moveTo(w * 0.2f, h * 0.9f) // Bottom left
        quadraticTo(w * 0.15f, h * 0.5f, w * 0.25f, h * 0.3f) // Left side
        lineTo(w * 0.4f, h * 0.2f) // Shoulder left
        lineTo(w * 0.42f, h * 0.1f) // Spout base
        lineTo(w * 0.58f, h * 0.1f) // Spout base
        lineTo(w * 0.6f, h * 0.2f) // Shoulder right
        lineTo(w * 0.75f, h * 0.3f) // Right side
        quadraticTo(w * 0.85f, h * 0.5f, w * 0.8f, h * 0.9f) // Right curve
        lineTo(w * 0.2f, h * 0.9f) // Bottom
    }
    drawPath(packet, Color(0xFFFFF9C4)) // Creamy yellow/white

    // Spout Cap
    drawRoundRect(Color(0xFFD32F2F), topLeft = Offset(w * 0.42f, h * 0.05f), size = Size(w * 0.16f, h * 0.08f), cornerRadius = CornerRadius(2f))

    // Label branding area
    drawOval(Color(0xFFFBC02D), topLeft = Offset(w * 0.35f, h * 0.45f), size = Size(w * 0.3f, h * 0.2f))
    
    // Fold/crease lines implies packet
    drawLine(Color.Black.copy(alpha = 0.1f), Offset(w * 0.2f, h * 0.9f), Offset(w * 0.25f, h * 0.8f), strokeWidth = 1.dp.toPx())
    drawLine(Color.Black.copy(alpha = 0.1f), Offset(w * 0.8f, h * 0.9f), Offset(w * 0.75f, h * 0.8f), strokeWidth = 1.dp.toPx())
}

private fun DrawScope.drawOnion(darkColor: Color, lightColor: Color) {
    val cx = size.width / 2f
    val cy = size.height * 0.55f
    val rx = size.width * 0.42f
    val ry = size.height * 0.38f

    // Body — oval
    drawOval(lightColor, topLeft = Offset(cx - rx, cy - ry), size = Size(rx * 2, ry * 2))

    // Onion rings (3 concentric)
    drawOval(darkColor.copy(alpha = 0.3f), topLeft = Offset(cx - rx * 0.75f, cy - ry * 0.75f),
        size = Size(rx * 1.5f, ry * 1.5f), style = Stroke(1.5.dp.toPx()))
    drawOval(darkColor.copy(alpha = 0.2f), topLeft = Offset(cx - rx * 0.45f, cy - ry * 0.45f),
        size = Size(rx * 0.9f, ry * 0.9f), style = Stroke(1.dp.toPx()))

    // Root tuft at bottom
    val rootY = cy + ry * 0.85f
    drawLine(darkColor.copy(alpha = 0.4f), Offset(cx, rootY), Offset(cx - rx * 0.15f, rootY + ry * 0.3f), strokeWidth = 1.dp.toPx())
    drawLine(darkColor.copy(alpha = 0.4f), Offset(cx, rootY), Offset(cx + rx * 0.15f, rootY + ry * 0.3f), strokeWidth = 1.dp.toPx())
    drawLine(darkColor.copy(alpha = 0.4f), Offset(cx, rootY), Offset(cx, rootY + ry * 0.35f), strokeWidth = 1.dp.toPx())

    // Sprout on top
    val sproutBase = cy - ry
    val sproutPath = Path().apply {
        moveTo(cx - rx * 0.08f, sproutBase)
        quadraticTo(cx - rx * 0.25f, sproutBase - ry * 0.6f, cx - rx * 0.05f, sproutBase - ry * 0.8f)
        lineTo(cx + rx * 0.05f, sproutBase - ry * 0.8f)
        quadraticTo(cx + rx * 0.25f, sproutBase - ry * 0.6f, cx + rx * 0.08f, sproutBase)
        close()
    }
    drawPath(sproutPath, Color(0xFF66BB6A))
}

private fun DrawScope.drawBellPepper() {
    val w = size.width; val h = size.height
    val cx = w / 2f; val cy = h * 0.55f

    // Three lobes of the pepper
    val lobeR = w * 0.2f
    drawCircle(Color(0xFFE53935), radius = lobeR, center = Offset(cx - lobeR * 0.8f, cy + lobeR * 0.2f))
    drawCircle(Color(0xFFE53935), radius = lobeR, center = Offset(cx + lobeR * 0.8f, cy + lobeR * 0.2f))
    drawCircle(Color(0xFFF44336), radius = lobeR * 0.9f, center = Offset(cx, cy - lobeR * 0.1f))

    // Connecting body
    drawRoundRect(Color(0xFFEF5350), topLeft = Offset(cx - lobeR * 1.2f, cy - lobeR * 0.5f),
        size = Size(lobeR * 2.4f, lobeR * 1.5f), cornerRadius = CornerRadius(lobeR * 0.3f))

    // Highlight
    drawCircle(Color.White.copy(alpha = 0.3f), radius = lobeR * 0.35f, center = Offset(cx - lobeR * 0.5f, cy - lobeR * 0.3f))

    // Stem
    val stemW = w * 0.08f
    drawRoundRect(Color(0xFF388E3C), topLeft = Offset(cx - stemW / 2, cy - lobeR * 1.3f),
        size = Size(stemW, lobeR * 0.8f), cornerRadius = CornerRadius(stemW / 2))
}

private fun DrawScope.drawRocoto() {
    val w = size.width; val h = size.height
    val cx = w / 2f; val cy = h * 0.55f
    val r = w * 0.38f

    // Main round body (slightly flattened)
    drawOval(Color(0xFFD32F2F), topLeft = Offset(cx - r, cy - r * 0.85f), size = Size(r * 2, r * 1.7f))
    // Darker shadow at bottom
    drawOval(Color(0xFFB71C1C).copy(alpha = 0.4f), topLeft = Offset(cx - r * 0.8f, cy + r * 0.2f), size = Size(r * 1.6f, r * 0.6f))
    // Highlight
    drawCircle(Color.White.copy(alpha = 0.35f), radius = r * 0.2f, center = Offset(cx - r * 0.35f, cy - r * 0.4f))
    // Stem
    drawRoundRect(Color(0xFF2E7D32), topLeft = Offset(cx - w * 0.05f, cy - r * 1.2f),
        size = Size(w * 0.1f, r * 0.5f), cornerRadius = CornerRadius(w * 0.04f))
    // Star-shaped calyx around stem
    val calyx = Path().apply {
        moveTo(cx - r * 0.35f, cy - r * 0.75f)
        lineTo(cx - r * 0.15f, cy - r * 0.95f)
        lineTo(cx, cy - r * 0.7f)
        lineTo(cx + r * 0.15f, cy - r * 0.95f)
        lineTo(cx + r * 0.35f, cy - r * 0.75f)
        lineTo(cx, cy - r * 0.6f)
        close()
    }
    drawPath(calyx, Color(0xFF388E3C))
}

// ═══════════════════════════════════════════════════════════════════
//  CITRUS
// ═══════════════════════════════════════════════════════════════════

private fun DrawScope.drawCitrus(skinDark: Color, skinLight: Color, flesh: Color) {
    val cx = size.width / 2f; val cy = size.height / 2f
    val r = size.width * 0.4f

    // Outer skin
    drawCircle(skinLight, radius = r, center = Offset(cx, cy))
    // Cut face (inner flesh)
    drawCircle(flesh, radius = r * 0.75f, center = Offset(cx, cy))
    // Segment lines (like a real sliced lime/lemon)
    val segments = 6
    for (i in 0 until segments) {
        val angle = (i * 360f / segments) * (Math.PI / 180f)
        val endX = cx + r * 0.7f * kotlin.math.cos(angle).toFloat()
        val endY = cy + r * 0.7f * kotlin.math.sin(angle).toFloat()
        drawLine(skinDark.copy(alpha = 0.5f), Offset(cx, cy), Offset(endX, endY), strokeWidth = 1.dp.toPx())
    }
    // Center pip
    drawCircle(skinDark.copy(alpha = 0.3f), radius = r * 0.1f, center = Offset(cx, cy))
    // Skin border
    drawCircle(skinDark, radius = r, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
}

// ═══════════════════════════════════════════════════════════════════
//  LEAFY GREENS & HERBS
// ═══════════════════════════════════════════════════════════════════

private fun DrawScope.drawLeaf(darkColor: Color, midColor: Color, lightColor: Color) {
    val w = size.width; val h = size.height

    // Main leaf body
    val leaf = Path().apply {
        moveTo(w * 0.5f, h * 0.92f) // base (stem)
        cubicTo(w * 0.05f, h * 0.7f, w * 0.05f, h * 0.25f, w * 0.5f, h * 0.08f) // left edge
        cubicTo(w * 0.95f, h * 0.25f, w * 0.95f, h * 0.7f, w * 0.5f, h * 0.92f) // right edge
    }
    drawPath(leaf, midColor)

    // Central vein
    drawLine(darkColor, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.5f, h * 0.88f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    // Side veins
    for (i in 1..4) {
        val y = h * (0.2f + i * 0.14f)
        val spread = w * (0.12f + i * 0.03f)
        drawLine(darkColor.copy(alpha = 0.4f), Offset(w * 0.5f, y), Offset(w * 0.5f - spread, y - h * 0.06f), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
        drawLine(darkColor.copy(alpha = 0.4f), Offset(w * 0.5f, y), Offset(w * 0.5f + spread, y - h * 0.06f), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
    }

    // Highlight
    drawCircle(lightColor.copy(alpha = 0.4f), radius = w * 0.12f, center = Offset(w * 0.38f, h * 0.3f))
}

private fun DrawScope.drawHerbSprig(color: Color) {
    val w = size.width; val h = size.height

    // Main stem
    val stem = Path().apply {
        moveTo(w * 0.5f, h * 0.95f)
        quadraticTo(w * 0.45f, h * 0.6f, w * 0.5f, h * 0.15f)
    }
    drawPath(stem, color.copy(alpha = 0.8f), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))

    // Small oval leaves along the stem (3 pairs)
    val leafPositions = listOf(0.25f, 0.45f, 0.65f)
    for (pos in leafPositions) {
        val cy = h * pos
        // Left leaf
        val leftLeaf = Path().apply {
            moveTo(w * 0.48f, cy)
            quadraticTo(w * 0.2f, cy - h * 0.08f, w * 0.25f, cy - h * 0.02f)
            quadraticTo(w * 0.22f, cy + h * 0.04f, w * 0.48f, cy)
        }
        drawPath(leftLeaf, color)
        // Right leaf
        val rightLeaf = Path().apply {
            moveTo(w * 0.52f, cy + h * 0.03f)
            quadraticTo(w * 0.8f, cy - h * 0.05f, w * 0.75f, cy + h * 0.01f)
            quadraticTo(w * 0.78f, cy + h * 0.07f, w * 0.52f, cy + h * 0.03f)
        }
        drawPath(rightLeaf, color)
    }
    // Top leaf (single)
    val topLeaf = Path().apply {
        moveTo(w * 0.5f, h * 0.15f)
        quadraticTo(w * 0.35f, h * 0.02f, w * 0.5f, h * 0.05f)
        quadraticTo(w * 0.65f, h * 0.02f, w * 0.5f, h * 0.15f)
    }
    drawPath(topLeaf, color)
}

private fun DrawScope.drawDriedHerb(color: Color) {
    val w = size.width; val h = size.height
    val cx = w / 2f; val cy = h / 2f

    // Small bowl / pile of dried herbs
    val bowl = Path().apply {
        moveTo(cx - w * 0.35f, cy)
        quadraticTo(cx, cy + h * 0.35f, cx + w * 0.35f, cy)
    }
    drawPath(bowl, Color(0xFFBCAAA4), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

    // Pile of herb flakes
    for (i in 0..4) {
        val x = cx + (i - 2) * w * 0.1f
        val y = cy + h * 0.05f
        drawCircle(color, radius = w * 0.06f, center = Offset(x, y))
    }
    for (i in 0..2) {
        val x = cx + (i - 1) * w * 0.12f
        val y = cy - h * 0.04f
        drawCircle(color.copy(alpha = 0.7f), radius = w * 0.05f, center = Offset(x, y))
    }
}

// ═══════════════════════════════════════════════════════════════════
//  PROTEINS
// ═══════════════════════════════════════════════════════════════════

private fun DrawScope.drawChickenLeg() {
    val w = size.width; val h = size.height

    // Drumstick bone
    val boneW = w * 0.1f
    drawRoundRect(Color(0xFFFFF8E1), topLeft = Offset(w * 0.42f, h * 0.05f),
        size = Size(boneW, h * 0.45f), cornerRadius = CornerRadius(boneW / 2))
    // Bone knob at top
    drawCircle(Color(0xFFFFF8E1), radius = boneW * 0.8f, center = Offset(w * 0.47f, h * 0.08f))

    // Meat (thigh)
    val meatPath = Path().apply {
        moveTo(w * 0.25f, h * 0.35f)
        cubicTo(w * 0.15f, h * 0.5f, w * 0.15f, h * 0.85f, w * 0.45f, h * 0.9f)
        cubicTo(w * 0.7f, h * 0.92f, w * 0.85f, h * 0.7f, w * 0.8f, h * 0.45f)
        cubicTo(w * 0.75f, h * 0.3f, w * 0.35f, h * 0.3f, w * 0.25f, h * 0.35f)
    }
    drawPath(meatPath, Color(0xFFE8A87C))
    // Golden crispy skin highlight
    drawCircle(Color(0xFFD4915D).copy(alpha = 0.5f), radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.6f))
    drawCircle(Color(0xFFFFF8E1).copy(alpha = 0.3f), radius = w * 0.08f, center = Offset(w * 0.35f, h * 0.5f))
}

private fun DrawScope.drawSteak() {
    val w = size.width; val h = size.height

    // Main cut shape (irregular organic shape)
    val steakPath = Path().apply {
        moveTo(w * 0.15f, h * 0.35f)
        cubicTo(w * 0.25f, h * 0.2f, w * 0.7f, h * 0.18f, w * 0.85f, h * 0.3f)
        cubicTo(w * 0.92f, h * 0.45f, w * 0.88f, h * 0.7f, w * 0.75f, h * 0.8f)
        cubicTo(w * 0.55f, h * 0.88f, w * 0.2f, h * 0.82f, w * 0.12f, h * 0.6f)
        cubicTo(w * 0.08f, h * 0.48f, w * 0.1f, h * 0.4f, w * 0.15f, h * 0.35f)
    }
    drawPath(steakPath, Color(0xFFB71C1C)) // Deep red meat

    // Fat marbling streaks
    val marbling1 = Path().apply {
        moveTo(w * 0.2f, h * 0.45f)
        quadraticTo(w * 0.4f, h * 0.38f, w * 0.6f, h * 0.42f)
    }
    drawPath(marbling1, Color(0xFFFFCDD2).copy(alpha = 0.6f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    val marbling2 = Path().apply {
        moveTo(w * 0.25f, h * 0.6f)
        quadraticTo(w * 0.5f, h * 0.53f, w * 0.7f, h * 0.58f)
    }
    drawPath(marbling2, Color(0xFFFFCDD2).copy(alpha = 0.5f), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))

    // Fat cap on top edge
    val fatCap = Path().apply {
        moveTo(w * 0.15f, h * 0.35f)
        cubicTo(w * 0.25f, h * 0.2f, w * 0.7f, h * 0.18f, w * 0.85f, h * 0.3f)
        lineTo(w * 0.8f, h * 0.36f)
        cubicTo(w * 0.6f, h * 0.28f, w * 0.3f, h * 0.28f, w * 0.18f, h * 0.4f)
        close()
    }
    drawPath(fatCap, Color(0xFFFFF8E1).copy(alpha = 0.7f))
}

private fun DrawScope.drawPorkCut() {
    val w = size.width; val h = size.height

    // Pork chop shape (rounded with a flat side)
    val porkPath = Path().apply {
        moveTo(w * 0.2f, h * 0.25f)
        cubicTo(w * 0.35f, h * 0.15f, w * 0.7f, h * 0.15f, w * 0.8f, h * 0.3f)
        cubicTo(w * 0.88f, h * 0.5f, w * 0.82f, h * 0.75f, w * 0.65f, h * 0.85f)
        cubicTo(w * 0.45f, h * 0.9f, w * 0.2f, h * 0.8f, w * 0.15f, h * 0.55f)
        cubicTo(w * 0.12f, h * 0.4f, w * 0.14f, h * 0.3f, w * 0.2f, h * 0.25f)
    }
    drawPath(porkPath, Color(0xFFE8A0A0)) // Pink pork color

    // Fat layer on one side
    val fatLayer = Path().apply {
        moveTo(w * 0.65f, h * 0.85f)
        cubicTo(w * 0.82f, h * 0.75f, w * 0.88f, h * 0.5f, w * 0.8f, h * 0.3f)
        lineTo(w * 0.85f, h * 0.32f)
        cubicTo(w * 0.92f, h * 0.52f, w * 0.87f, h * 0.78f, w * 0.68f, h * 0.88f)
        close()
    }
    drawPath(fatLayer, Color(0xFFFFF3E0).copy(alpha = 0.8f))

    // Bone (small circle)
    drawCircle(Color(0xFFFFF8E1), radius = w * 0.08f, center = Offset(w * 0.28f, h * 0.35f))
    drawCircle(Color(0xFFD7CCC8), radius = w * 0.05f, center = Offset(w * 0.28f, h * 0.35f))
}

private fun DrawScope.drawEgg() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val w = size.width

    // Egg white (fried egg look)
    val whitePath = Path().apply {
        moveTo(cx, cy - w * 0.42f)
        cubicTo(cx + w * 0.45f, cy - w * 0.35f, cx + w * 0.48f, cy + w * 0.1f, cx + w * 0.35f, cy + w * 0.35f)
        cubicTo(cx + w * 0.15f, cy + w * 0.48f, cx - w * 0.2f, cy + w * 0.45f, cx - w * 0.38f, cy + w * 0.3f)
        cubicTo(cx - w * 0.48f, cy + w * 0.1f, cx - w * 0.45f, cy - w * 0.25f, cx, cy - w * 0.42f)
    }
    drawPath(whitePath, Color(0xFFF5F5F5))
    // Border for the white
    drawPath(whitePath, Color(0xFFE0E0E0), style = Stroke(1.dp.toPx()))

    // Yolk
    drawCircle(Color(0xFFFF8F00), radius = w * 0.18f, center = Offset(cx - w * 0.02f, cy + w * 0.02f))
    // Yolk highlight
    drawCircle(Color(0xFFFFB300).copy(alpha = 0.6f), radius = w * 0.07f, center = Offset(cx - w * 0.06f, cy - w * 0.02f))
}

// ═══════════════════════════════════════════════════════════════════
//  SPICES
// ═══════════════════════════════════════════════════════════════════

private fun DrawScope.drawSpicePowder(color: Color, darkColor: Color) {
    val w = size.width; val h = size.height
    val cx = w / 2f; val cy = h * 0.55f

    // Small wooden spoon bowl
    val spoonBowl = Path().apply {
        moveTo(cx - w * 0.3f, cy - h * 0.02f)
        quadraticTo(cx, cy + h * 0.25f, cx + w * 0.3f, cy - h * 0.02f)
        quadraticTo(cx, cy + h * 0.08f, cx - w * 0.3f, cy - h * 0.02f)
    }
    drawPath(spoonBowl, Color(0xFF8D6E63)) // Wooden spoon

    // Pile of powder
    val pile = Path().apply {
        moveTo(cx - w * 0.25f, cy)
        quadraticTo(cx - w * 0.15f, cy - h * 0.2f, cx, cy - h * 0.22f)
        quadraticTo(cx + w * 0.15f, cy - h * 0.2f, cx + w * 0.25f, cy)
        quadraticTo(cx, cy + h * 0.05f, cx - w * 0.25f, cy)
    }
    drawPath(pile, color)

    // Texture dots
    drawCircle(darkColor.copy(alpha = 0.3f), radius = w * 0.02f, center = Offset(cx - w * 0.08f, cy - h * 0.1f))
    drawCircle(darkColor.copy(alpha = 0.3f), radius = w * 0.02f, center = Offset(cx + w * 0.1f, cy - h * 0.08f))
    drawCircle(darkColor.copy(alpha = 0.3f), radius = w * 0.015f, center = Offset(cx, cy - h * 0.15f))

    // Spoon handle
    drawRoundRect(Color(0xFFA1887F), topLeft = Offset(cx - w * 0.04f, cy + h * 0.12f),
        size = Size(w * 0.08f, h * 0.32f), cornerRadius = CornerRadius(w * 0.03f))
}

private fun DrawScope.drawPeppercorns() {
    val w = size.width; val h = size.height

    // Scatter of peppercorns
    val positions = listOf(
        Offset(w * 0.3f, h * 0.3f), Offset(w * 0.6f, h * 0.25f), Offset(w * 0.75f, h * 0.45f),
        Offset(w * 0.5f, h * 0.5f), Offset(w * 0.25f, h * 0.55f), Offset(w * 0.45f, h * 0.7f),
        Offset(w * 0.7f, h * 0.7f), Offset(w * 0.35f, h * 0.45f)
    )
    val pepperSize = w * 0.08f

    positions.forEachIndexed { idx, pos ->
        val shade = if (idx % 3 == 0) Color(0xFF3E2723) else if (idx % 3 == 1) Color(0xFF4E342E) else Color(0xFF5D4037)
        drawCircle(shade, radius = pepperSize, center = pos)
        // Small highlight
        drawCircle(Color.White.copy(alpha = 0.2f), radius = pepperSize * 0.4f,
            center = Offset(pos.x - pepperSize * 0.2f, pos.y - pepperSize * 0.2f))
    }
}

private fun DrawScope.drawChiliPepper(mainColor: Color, darkColor: Color) {
    val w = size.width; val h = size.height

    // Long curved chili body
    val chili = Path().apply {
        moveTo(w * 0.3f, h * 0.15f)
        cubicTo(w * 0.55f, h * 0.12f, w * 0.75f, h * 0.25f, w * 0.8f, h * 0.5f)
        cubicTo(w * 0.82f, h * 0.7f, w * 0.7f, h * 0.88f, w * 0.6f, h * 0.92f) // tip
        cubicTo(w * 0.55f, h * 0.85f, w * 0.5f, h * 0.65f, w * 0.45f, h * 0.5f)
        cubicTo(w * 0.38f, h * 0.3f, w * 0.2f, h * 0.2f, w * 0.3f, h * 0.15f)
    }
    drawPath(chili, mainColor)

    // Shadow/depth line
    val shadow = Path().apply {
        moveTo(w * 0.38f, h * 0.22f)
        cubicTo(w * 0.5f, h * 0.3f, w * 0.6f, h * 0.55f, w * 0.62f, h * 0.85f)
    }
    drawPath(shadow, darkColor.copy(alpha = 0.3f), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))

    // Highlight
    drawCircle(Color.White.copy(alpha = 0.25f), radius = w * 0.06f, center = Offset(w * 0.4f, h * 0.25f))

    // Green stem at top
    val stem = Path().apply {
        moveTo(w * 0.25f, h * 0.18f)
        lineTo(w * 0.3f, h * 0.08f)
        lineTo(w * 0.38f, h * 0.12f)
        lineTo(w * 0.35f, h * 0.18f)
        close()
    }
    drawPath(stem, Color(0xFF388E3C))
    drawCircle(Color(0xFF2E7D32), radius = w * 0.04f, center = Offset(w * 0.32f, h * 0.1f))
}
