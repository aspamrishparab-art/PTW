package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkSteelBlue
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SteelBlue
import java.io.ByteArrayOutputStream

@Composable
fun DigitalSignatureDialog(
    roleTitle: String,
    signerName: String,
    signerPhone: String,
    onDismiss: () -> Unit,
    onSignatureCaptured: (base64Signature: String, remarks: String) -> Unit
) {
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var remarks by remember { mutableStateOf("") }
    var signerInputName by remember { mutableStateOf(signerName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "Digital Acceptance & Signature",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSteelBlue
                )
                Text(
                    text = "Signing as: $roleTitle",
                    fontSize = 13.sp,
                    color = SteelBlue,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = signerInputName,
                    onValueChange = { signerInputName = it },
                    label = { Text("Signer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Approval Remarks / Clearance Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Draw Digital Signature below:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeutralMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .background(Color(0xFFFAFAFA))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = listOf(offset)
                                },
                                onDragEnd = {
                                    if (currentPath.isNotEmpty()) {
                                        paths.add(currentPath)
                                        currentPath = emptyList()
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPath = currentPath + change.position
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        for (stroke in paths) {
                            if (stroke.size > 1) {
                                val p = Path().apply {
                                    moveTo(stroke.first().x, stroke.first().y)
                                    for (i in 1 until stroke.size) {
                                        lineTo(stroke[i].x, stroke[i].y)
                                    }
                                }
                                drawPath(
                                    path = p,
                                    color = Color(0xFF002244),
                                    style = Stroke(
                                        width = 4.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        if (currentPath.size > 1) {
                            val p = Path().apply {
                                moveTo(currentPath.first().x, currentPath.first().y)
                                for (i in 1 until currentPath.size) {
                                    lineTo(currentPath[i].x, currentPath[i].y)
                                }
                            }
                            drawPath(
                                path = p,
                                color = Color(0xFF002244),
                                style = Stroke(
                                    width = 4.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (paths.isEmpty() && currentPath.isEmpty()) {
                        Text(
                            text = "Sign here with finger or stylus",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            paths.clear()
                            currentPath = emptyList()
                        },
                        modifier = Modifier.testTag("clear_signature_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Clear")
                    }

                    Row {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("cancel_signature_button")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val base64 = generateSignatureBitmapBase64(paths)
                                onSignatureCaptured(base64, remarks)
                            },
                            enabled = paths.isNotEmpty() || signerInputName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                            modifier = Modifier.testTag("confirm_signature_button")
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

private fun generateSignatureBitmapBase64(paths: List<List<Offset>>): String {
    return try {
        val width = 400
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = AndroidPaint().apply {
            color = android.graphics.Color.rgb(0, 34, 68)
            strokeWidth = 5f
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
        }

        for (stroke in paths) {
            if (stroke.size > 1) {
                val androidPath = android.graphics.Path()
                androidPath.moveTo(stroke.first().x, stroke.first().y)
                for (i in 1 until stroke.size) {
                    androidPath.lineTo(stroke[i].x, stroke[i].y)
                }
                canvas.drawPath(androidPath, paint)
            }
        }

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val bytes = baos.toByteArray()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        "SIGNATURE_CAPTURED"
    }
}
