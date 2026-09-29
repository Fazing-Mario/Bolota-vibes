package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun BolotaPetView(
    mood: String, // "radiante", "feliz", "ok", "triste", "firme"
    level: Int = 1,
    wearAccessories: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    // Gentle bobbing animation
    val infiniteTransition = rememberInfiniteTransition(label = "bolota_bob")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob_offset"
    )

    // Blinking animation
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3700
                0.05f at 3850
                1f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink_progress"
    )

    // Resolve skin color based on equipped skin item
    val skinId = wearAccessories["SKIN"] ?: "laranja"
    val (primarySkin, tummySkin, pawColor) = when (skinId) {
        "pessego" -> Triple(Color(0xFFFFB27D), Color(0xFFFFE3CD), Color(0xFFE88A4F))
        "menta" -> Triple(Color(0xFF4DD0B8), Color(0xFFBFF6ED), Color(0xFF2FA68F))
        "lavanda" -> Triple(Color(0xFFB39DDB), Color(0xFFEDE7F6), Color(0xFF8E71C2))
        "noite" -> Triple(Color(0xFF3949AB), Color(0xFF7986CB), Color(0xFF263282))
        else -> Triple(BolotaPetOrange, BolotaPetOrangeLight, BolotaPetOrangeDark)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasW = this.size.width
            val scale = canvasW / 200f
            val dy = bobOffset * scale

            // Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.15f),
                topLeft = Offset(42f * scale, 166f * scale),
                size = Size(116f * scale, 12f * scale)
            )

            // Cape under body if equipped
            if (wearAccessories["NECK"] == "capa") {
                val cape = Path().apply {
                    moveTo(60f * scale, (110f * scale) + dy)
                    lineTo(20f * scale, (170f * scale) + dy)
                    quadraticTo(100f * scale, (180f * scale) + dy, 180f * scale, (170f * scale) + dy)
                    lineTo(140f * scale, (110f * scale) + dy)
                    close()
                }
                drawPath(cape, color = Color(0xFFD32F2F))
            }

            // Paws / Feet
            drawOval(
                color = pawColor,
                topLeft = Offset(56f * scale, (155f * scale) + dy),
                size = Size(32f * scale, 18f * scale)
            )
            drawOval(
                color = pawColor,
                topLeft = Offset(112f * scale, (155f * scale) + dy),
                size = Size(32f * scale, 18f * scale)
            )

            // Arms / Flippers
            drawOval(
                color = primarySkin,
                topLeft = Offset(20f * scale, (102f * scale) + dy),
                size = Size(20f * scale, 32f * scale)
            )
            drawOval(
                color = primarySkin,
                topLeft = Offset(160f * scale, (102f * scale) + dy),
                size = Size(20f * scale, 32f * scale)
            )

            // Body (Chubby rounded blob)
            val bodyPath = Path().apply {
                moveTo(100f * scale, (32f * scale) + dy)
                cubicTo(
                    152f * scale, (32f * scale) + dy,
                    178f * scale, (72f * scale) + dy,
                    176f * scale, (112f * scale) + dy
                )
                cubicTo(
                    174f * scale, (150f * scale) + dy,
                    142f * scale, (168f * scale) + dy,
                    100f * scale, (168f * scale) + dy
                )
                cubicTo(
                    58f * scale, (168f * scale) + dy,
                    26f * scale, (150f * scale) + dy,
                    24f * scale, (112f * scale) + dy
                )
                cubicTo(
                    22f * scale, (72f * scale) + dy,
                    48f * scale, (32f * scale) + dy,
                    100f * scale, (32f * scale) + dy
                )
                close()
            }
            drawPath(path = bodyPath, color = primarySkin)

            // Soft Tummy
            drawOval(
                color = tummySkin,
                topLeft = Offset(54f * scale, (102f * scale) + dy),
                size = Size(92f * scale, 60f * scale)
            )

            // Rosy Cheeks
            drawOval(
                color = BolotaCheek.copy(alpha = 0.5f),
                topLeft = Offset(53f * scale, (104f * scale) + dy),
                size = Size(18f * scale, 12f * scale)
            )
            drawOval(
                color = BolotaCheek.copy(alpha = 0.5f),
                topLeft = Offset(129f * scale, (104f * scale) + dy),
                size = Size(18f * scale, 12f * scale)
            )

            // Sprout for level >= 3 (if no head accessory)
            if (level >= 3 && wearAccessories["HEAD"].isNullOrEmpty()) {
                val sproutPath = Path().apply {
                    moveTo(100f * scale, (32f * scale) + dy)
                    cubicTo(
                        100f * scale, (22f * scale) + dy,
                        101f * scale, (16f * scale) + dy,
                        103f * scale, (10f * scale) + dy
                    )
                }
                drawPath(
                    sproutPath,
                    color = BolotaAccentDark,
                    style = Stroke(width = 4f * scale, cap = StrokeCap.Round)
                )
                drawOval(
                    color = BolotaAccentDark,
                    topLeft = Offset(82f * scale, (7f * scale) + dy),
                    size = Size(20f * scale, 11f * scale)
                )
                drawOval(
                    color = BolotaAccentDark,
                    topLeft = Offset(103f * scale, (4f * scale) + dy),
                    size = Size(20f * scale, 11f * scale)
                )
            }

            // Eyes and Mouth
            drawEyesAndMouth(
                mood = mood,
                scale = scale,
                dy = dy,
                blinkProgress = blinkProgress
            )

            // Accessories
            drawAccessories(
                wear = wearAccessories,
                scale = scale,
                dy = dy
            )
        }
    }
}

private fun DrawScope.drawEyesAndMouth(
    mood: String,
    scale: Float,
    dy: Float,
    blinkProgress: Float
) {
    val ink = BolotaPetInk

    when (mood) {
        "radiante" -> {
            val leftEye = Path().apply {
                moveTo(69f * scale, (95f * scale) + dy)
                quadraticTo(78f * scale, (83f * scale) + dy, 87f * scale, (95f * scale) + dy)
            }
            val rightEye = Path().apply {
                moveTo(113f * scale, (95f * scale) + dy)
                quadraticTo(122f * scale, (83f * scale) + dy, 131f * scale, (95f * scale) + dy)
            }
            drawPath(leftEye, color = ink, style = Stroke(width = 5f * scale, cap = StrokeCap.Round))
            drawPath(rightEye, color = ink, style = Stroke(width = 5f * scale, cap = StrokeCap.Round))

            val mouthPath = Path().apply {
                moveTo(84f * scale, (110f * scale) + dy)
                quadraticTo(100f * scale, (134f * scale) + dy, 116f * scale, (110f * scale) + dy)
                close()
            }
            drawPath(mouthPath, color = ink)
            val tonguePath = Path().apply {
                moveTo(92f * scale, (121f * scale) + dy)
                quadraticTo(100f * scale, (128f * scale) + dy, 108f * scale, (121f * scale) + dy)
                quadraticTo(100f * scale, (117f * scale) + dy, 92f * scale, (121f * scale) + dy)
                close()
            }
            drawPath(tonguePath, color = BolotaCheek)
        }
        "triste" -> {
            val eyeH = 18f * scale * blinkProgress
            drawOval(
                color = ink,
                topLeft = Offset(69f * scale, (86f * scale) + dy + ((18f * scale - eyeH) / 2)),
                size = Size(18f * scale, eyeH)
            )
            drawOval(
                color = ink,
                topLeft = Offset(113f * scale, (86f * scale) + dy + ((18f * scale - eyeH) / 2)),
                size = Size(18f * scale, eyeH)
            )
            drawLine(
                color = ink,
                start = Offset(68f * scale, (80f * scale) + dy),
                end = Offset(86f * scale, (76f * scale) + dy),
                strokeWidth = 4f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = ink,
                start = Offset(132f * scale, (80f * scale) + dy),
                end = Offset(114f * scale, (76f * scale) + dy),
                strokeWidth = 4f * scale,
                cap = StrokeCap.Round
            )
            val mouth = Path().apply {
                moveTo(87f * scale, (122f * scale) + dy)
                quadraticTo(100f * scale, (111f * scale) + dy, 113f * scale, (122f * scale) + dy)
            }
            drawPath(mouth, color = ink, style = Stroke(width = 4.5f * scale, cap = StrokeCap.Round))
            drawOval(
                color = Color(0xFF6FB7FF),
                topLeft = Offset(131f * scale, (104f * scale) + dy),
                size = Size(6f * scale, 10f * scale)
            )
        }
        "firme" -> {
            val eyeH = 18f * scale * blinkProgress
            drawOval(
                color = ink,
                topLeft = Offset(69f * scale, (85f * scale) + dy + ((18f * scale - eyeH) / 2)),
                size = Size(18f * scale, eyeH)
            )
            drawOval(
                color = ink,
                topLeft = Offset(113f * scale, (85f * scale) + dy + ((18f * scale - eyeH) / 2)),
                size = Size(18f * scale, eyeH)
            )
            drawLine(
                color = ink,
                start = Offset(66f * scale, (74f * scale) + dy),
                end = Offset(88f * scale, (83f * scale) + dy),
                strokeWidth = 5f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = ink,
                start = Offset(134f * scale, (74f * scale) + dy),
                end = Offset(112f * scale, (83f * scale) + dy),
                strokeWidth = 5f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = ink,
                start = Offset(88f * scale, (118f * scale) + dy),
                end = Offset(112f * scale, (118f * scale) + dy),
                strokeWidth = 5f * scale,
                cap = StrokeCap.Round
            )
        }
        else -> {
            val eyeH = 18f * scale * blinkProgress
            drawOval(
                color = ink,
                topLeft = Offset(69f * scale, (83f * scale) + dy + ((18f * scale - eyeH) / 2)),
                size = Size(18f * scale, eyeH)
            )
            drawOval(
                color = ink,
                topLeft = Offset(113f * scale, (83f * scale) + dy + ((18f * scale - eyeH) / 2)),
                size = Size(18f * scale, eyeH)
            )
            if (blinkProgress > 0.4f) {
                drawCircle(
                    color = Color.White,
                    radius = 3f * scale,
                    center = Offset(81f * scale, (89f * scale) + dy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f * scale,
                    center = Offset(125f * scale, (89f * scale) + dy)
                )
            }
            val mouth = Path().apply {
                val mouthCurve = if (mood == "feliz") 125f else 120f
                moveTo(86f * scale, (112f * scale) + dy)
                quadraticTo(100f * scale, (mouthCurve * scale) + dy, 114f * scale, (112f * scale) + dy)
            }
            drawPath(mouth, color = ink, style = Stroke(width = 4.5f * scale, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawAccessories(
    wear: Map<String, String>,
    scale: Float,
    dy: Float
) {
    val ink = BolotaPetInk

    // HEAD ACCESSORIES
    when (wear["HEAD"]) {
        "faixa" -> {
            val headband = Path().apply {
                moveTo(38f * scale, (74f * scale) + dy)
                quadraticTo(100f * scale, (44f * scale) + dy, 162f * scale, (74f * scale) + dy)
                lineTo(160f * scale, (86f * scale) + dy)
                quadraticTo(100f * scale, (56f * scale) + dy, 40f * scale, (86f * scale) + dy)
                close()
            }
            drawPath(headband, color = Color(0xFFE5484D))
        }
        "capelo" -> {
            val capBase = Path().apply {
                moveTo(78f * scale, (34f * scale) + dy)
                lineTo(78f * scale, (48f * scale) + dy)
                quadraticTo(100f * scale, (58f * scale) + dy, 122f * scale, (48f * scale) + dy)
                lineTo(122f * scale, (34f * scale) + dy)
                close()
            }
            drawPath(capBase, color = ink)
            val capRhombus = Path().apply {
                moveTo(52f * scale, (32f * scale) + dy)
                lineTo(100f * scale, (12f * scale) + dy)
                lineTo(148f * scale, (32f * scale) + dy)
                lineTo(100f * scale, (50f * scale) + dy)
                close()
            }
            drawPath(capRhombus, color = ink)
            drawLine(
                color = Color(0xFFFFC53D),
                start = Offset(146f * scale, (33f * scale) + dy),
                end = Offset(146f * scale, (53f * scale) + dy),
                strokeWidth = 2.5f * scale
            )
            drawCircle(color = Color(0xFFFFC53D), radius = 4f * scale, center = Offset(146f * scale, (56f * scale) + dy))
        }
        "gorro" -> {
            val cap = Path().apply {
                moveTo(60f * scale, (46f * scale) + dy)
                quadraticTo(92f * scale, (-6f * scale) + dy, 162f * scale, (20f * scale) + dy)
                quadraticTo(132f * scale, (26f * scale) + dy, 140f * scale, (46f * scale) + dy)
                close()
            }
            drawPath(cap, color = Color(0xFF5967D8))
            drawRoundRect(
                color = Color(0xFFF4F6FF),
                topLeft = Offset(56f * scale, (38f * scale) + dy),
                size = Size(90f * scale, 13f * scale),
                cornerRadius = CornerRadius(6.5f * scale, 6.5f * scale)
            )
            drawCircle(color = Color(0xFFF4F6FF), radius = 8f * scale, center = Offset(164f * scale, (20f * scale) + dy))
        }
        "coroa" -> {
            val crown = Path().apply {
                moveTo(70f * scale, (40f * scale) + dy)
                lineTo(76f * scale, (12f * scale) + dy)
                lineTo(90f * scale, (28f * scale) + dy)
                lineTo(100f * scale, (6f * scale) + dy)
                lineTo(110f * scale, (28f * scale) + dy)
                lineTo(124f * scale, (12f * scale) + dy)
                lineTo(130f * scale, (40f * scale) + dy)
                close()
            }
            drawPath(crown, color = Color(0xFFFFC53D))
            drawPath(crown, color = ink, style = Stroke(width = 1.5f * scale, join = StrokeJoin.Round))
            drawCircle(color = Color(0xFFE5484D), radius = 3.5f * scale, center = Offset(100f * scale, (30f * scale) + dy))
            drawCircle(color = Color(0xFF3E8BFF), radius = 2.5f * scale, center = Offset(84f * scale, (33f * scale) + dy))
            drawCircle(color = Color(0xFF3E8BFF), radius = 2.5f * scale, center = Offset(116f * scale, (33f * scale) + dy))
        }
        "fone" -> {
            // DJ focus headphones
            val headband = Path().apply {
                moveTo(44f * scale, (72f * scale) + dy)
                quadraticTo(100f * scale, (10f * scale) + dy, 156f * scale, (72f * scale) + dy)
            }
            drawPath(headband, color = Color(0xFF37474F), style = Stroke(width = 6f * scale, cap = StrokeCap.Round))
            drawRoundRect(
                color = Color(0xFF263238),
                topLeft = Offset(34f * scale, (68f * scale) + dy),
                size = Size(18f * scale, 30f * scale),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            drawRoundRect(
                color = Color(0xFF263238),
                topLeft = Offset(148f * scale, (68f * scale) + dy),
                size = Size(18f * scale, 30f * scale),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
        }
    }

    // FACE ACCESSORIES
    when (wear["FACE"]) {
        "oculos" -> {
            drawRoundRect(
                color = ink,
                topLeft = Offset(61f * scale, (81f * scale) + dy),
                size = Size(33f * scale, 21f * scale),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            drawRoundRect(
                color = ink,
                topLeft = Offset(106f * scale, (81f * scale) + dy),
                size = Size(33f * scale, 21f * scale),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            val bridge = Path().apply {
                moveTo(94f * scale, (88f * scale) + dy)
                quadraticTo(100f * scale, (84f * scale) + dy, 106f * scale, (88f * scale) + dy)
            }
            drawPath(bridge, color = ink, style = Stroke(width = 3f * scale))
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(67f * scale, (86f * scale) + dy),
                end = Offset(76f * scale, (86f * scale) + dy),
                strokeWidth = 2f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(112f * scale, (86f * scale) + dy),
                end = Offset(121f * scale, (86f * scale) + dy),
                strokeWidth = 2f * scale,
                cap = StrokeCap.Round
            )
        }
        "oculos_leitura" -> {
            drawCircle(
                color = Color(0xFF6D4C41),
                radius = 14f * scale,
                center = Offset(78f * scale, (92f * scale) + dy),
                style = Stroke(width = 2.5f * scale)
            )
            drawCircle(
                color = Color(0xFF6D4C41),
                radius = 14f * scale,
                center = Offset(122f * scale, (92f * scale) + dy),
                style = Stroke(width = 2.5f * scale)
            )
            drawLine(
                color = Color(0xFF6D4C41),
                start = Offset(92f * scale, (92f * scale) + dy),
                end = Offset(108f * scale, (92f * scale) + dy),
                strokeWidth = 2.5f * scale
            )
        }
    }

    // NECK ACCESSORIES
    when (wear["NECK"]) {
        "laco" -> {
            val leftBow = Path().apply {
                moveTo(100f * scale, (146f * scale) + dy)
                lineTo(82f * scale, (137f * scale) + dy)
                lineTo(82f * scale, (155f * scale) + dy)
                close()
            }
            val rightBow = Path().apply {
                moveTo(100f * scale, (146f * scale) + dy)
                lineTo(118f * scale, (137f * scale) + dy)
                lineTo(118f * scale, (155f * scale) + dy)
                close()
            }
            drawPath(leftBow, color = Color(0xFFE4407A))
            drawPath(rightBow, color = Color(0xFFE4407A))
            drawCircle(color = Color(0xFFE4407A), radius = 4.5f * scale, center = Offset(100f * scale, (146f * scale) + dy))
        }
        "cachecol" -> {
            val scarf = Path().apply {
                moveTo(38f * scale, (134f * scale) + dy)
                quadraticTo(100f * scale, (156f * scale) + dy, 162f * scale, (134f * scale) + dy)
                lineTo(164f * scale, (147f * scale) + dy)
                quadraticTo(100f * scale, (170f * scale) + dy, 36f * scale, (147f * scale) + dy)
                close()
            }
            drawPath(scarf, color = Color(0xFF2F7DE1))
            val tail = Path().apply {
                moveTo(126f * scale, (152f * scale) + dy)
                lineTo(132f * scale, (174f * scale) + dy)
                lineTo(145f * scale, (170f * scale) + dy)
                lineTo(140f * scale, (149f * scale) + dy)
                close()
            }
            drawPath(tail, color = Color(0xFF2F7DE1))
        }
        "medalha" -> {
            val ribbon = Path().apply {
                moveTo(86f * scale, (126f * scale) + dy)
                lineTo(100f * scale, (145f * scale) + dy)
                lineTo(114f * scale, (126f * scale) + dy)
            }
            drawPath(ribbon, color = Color(0xFFE4407A), style = Stroke(width = 5f * scale, join = StrokeJoin.Round))
            drawCircle(color = Color(0xFFFFC53D), radius = 10f * scale, center = Offset(100f * scale, (152f * scale) + dy))
            drawCircle(color = ink, radius = 10f * scale, center = Offset(100f * scale, (152f * scale) + dy), style = Stroke(width = 1.5f * scale))
        }
    }

    // HAND ACCESSORIES
    when (wear["HAND"]) {
        "escudo" -> {
            val shield = Path().apply {
                moveTo(158f * scale, (102f * scale) + dy)
                lineTo(186f * scale, (102f * scale) + dy)
                lineTo(186f * scale, (117f * scale) + dy)
                quadraticTo(186f * scale, (132f * scale) + dy, 172f * scale, (139f * scale) + dy)
                quadraticTo(158f * scale, (132f * scale) + dy, 158f * scale, (117f * scale) + dy)
                close()
            }
            drawPath(shield, color = BolotaAccentDark)
            drawPath(shield, color = ink, style = Stroke(width = 1.5f * scale))
            val check = Path().apply {
                moveTo(165f * scale, (118f * scale) + dy)
                lineTo(170f * scale, (123f * scale) + dy)
                lineTo(179f * scale, (113f * scale) + dy)
            }
            drawPath(check, color = Color.White, style = Stroke(width = 2.6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        "haltere" -> {
            drawLine(
                color = Color(0xFF546E7A),
                start = Offset(158f * scale, (118f * scale) + dy),
                end = Offset(186f * scale, (118f * scale) + dy),
                strokeWidth = 4f * scale,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = Color(0xFF37474F),
                topLeft = Offset(154f * scale, (108f * scale) + dy),
                size = Size(8f * scale, 20f * scale),
                cornerRadius = CornerRadius(3f * scale, 3f * scale)
            )
            drawRoundRect(
                color = Color(0xFF37474F),
                topLeft = Offset(182f * scale, (108f * scale) + dy),
                size = Size(8f * scale, 20f * scale),
                cornerRadius = CornerRadius(3f * scale, 3f * scale)
            )
        }
        "livro" -> {
            drawRoundRect(
                color = Color(0xFF1565C0),
                topLeft = Offset(158f * scale, (106f * scale) + dy),
                size = Size(24f * scale, 30f * scale),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            drawRoundRect(
                color = Color(0xFFFFF8E1),
                topLeft = Offset(161f * scale, (109f * scale) + dy),
                size = Size(18f * scale, 24f * scale),
                cornerRadius = CornerRadius(2f * scale, 2f * scale)
            )
        }
        "cafe" -> {
            drawRoundRect(
                color = Color(0xFFFF7043),
                topLeft = Offset(160f * scale, (112f * scale) + dy),
                size = Size(20f * scale, 22f * scale),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            val steam = Path().apply {
                moveTo(166f * scale, (108f * scale) + dy)
                quadraticTo(168f * scale, (102f * scale) + dy, 166f * scale, (98f * scale) + dy)
            }
            drawPath(steam, color = Color.White.copy(alpha = 0.7f), style = Stroke(width = 2f * scale, cap = StrokeCap.Round))
        }
        "varinha" -> {
            drawLine(
                color = Color(0xFFFFB300),
                start = Offset(160f * scale, (132f * scale) + dy),
                end = Offset(182f * scale, (102f * scale) + dy),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color(0xFFFFD54F), radius = 5f * scale, center = Offset(184f * scale, (100f * scale) + dy))
        }
    }
}
