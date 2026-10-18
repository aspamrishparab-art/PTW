package com.example.ui.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.local.entity.PermitEntity
import com.example.data.model.ChecklistItem
import com.example.data.model.DigitalSignature
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PermitPdfGenerator {

    fun generateAndSharePdf(context: Context, permit: PermitEntity) {
        val file = generatePdf(context, permit)
        if (file != null && file.exists()) {
            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Permit-To-Work: ${permit.permitNumber} - ${permit.title}")
                putExtra(Intent.EXTRA_TEXT, "Official Permit-To-Work Document (${permit.permitNumber}) for Himgiri Castings Pvt. Ltd.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share or Save Permit PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    fun generatePdf(context: Context, permit: PermitEntity): File? {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val textPaint = Paint().apply { isAntiAlias = true }
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())

        // Header background banner
        paint.color = Color.rgb(11, 87, 208) // Steel Blue
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Accent strip
        paint.color = Color.rgb(232, 113, 10) // Himcast Orange
        canvas.drawRect(0f, 75f, 595f, 80f, paint)

        // Company Logo drawing
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.himcast_logo)
            if (logoBitmap != null) {
                val destRect = Rect(20, 12, 70, 62)
                canvas.drawBitmap(logoBitmap, null, destRect, paint)
            }
        } catch (e: Exception) {
            // fallback logo circle
            paint.color = Color.WHITE
            canvas.drawCircle(45f, 37f, 22f, paint)
        }

        // Header Text
        textPaint.color = Color.WHITE
        textPaint.textSize = 17f
        textPaint.isFakeBoldText = true
        canvas.drawText("HIMGIRI CASTINGS PVT. LTD.", 85f, 34f, textPaint)

        textPaint.textSize = 11f
        textPaint.isFakeBoldText = false
        canvas.drawText("Plant Safety & Environmental Health Management System", 85f, 50f, textPaint)
        canvas.drawText("PERMIT-TO-WORK (PTW) AUTHORIZATION CERTIFICATE", 85f, 65f, textPaint)

        // Permit Meta Box
        paint.color = Color.rgb(245, 247, 250)
        canvas.drawRect(20f, 95f, 575f, 185f, paint)
        paint.color = Color.rgb(200, 205, 215)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(20f, 95f, 575f, 185f, paint)
        paint.style = Paint.Style.FILL

        // Column 1
        textPaint.color = Color.rgb(30, 30, 30)
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = true
        canvas.drawText("Permit No:", 30f, 115f, textPaint)
        textPaint.isFakeBoldText = false
        canvas.drawText(permit.permitNumber, 100f, 115f, textPaint)

        textPaint.isFakeBoldText = true
        canvas.drawText("Category:", 30f, 135f, textPaint)
        textPaint.isFakeBoldText = false
        canvas.drawText(permit.category.displayName, 100f, 135f, textPaint)

        textPaint.isFakeBoldText = true
        canvas.drawText("Status:", 30f, 155f, textPaint)
        textPaint.isFakeBoldText = false
        canvas.drawText(permit.status.displayName, 100f, 155f, textPaint)

        textPaint.isFakeBoldText = true
        canvas.drawText("Location:", 30f, 175f, textPaint)
        textPaint.isFakeBoldText = false
        val locText = if (permit.location.length > 32) permit.location.substring(0, 32) + "..." else permit.location
        canvas.drawText(locText, 100f, 175f, textPaint)

        // Column 2
        textPaint.isFakeBoldText = true
        canvas.drawText("Contractor:", 310f, 115f, textPaint)
        textPaint.isFakeBoldText = false
        canvas.drawText(permit.contractorName, 395f, 115f, textPaint)

        textPaint.isFakeBoldText = true
        canvas.drawText("Supervisor:", 310f, 135f, textPaint)
        textPaint.isFakeBoldText = false
        canvas.drawText("${permit.contractorSupervisor} (${permit.contractorPhone})", 395f, 135f, textPaint)

        textPaint.isFakeBoldText = true
        canvas.drawText("Valid From:", 310f, 155f, textPaint)
        textPaint.isFakeBoldText = false
        canvas.drawText(dateFormat.format(Date(permit.startTime)), 395f, 155f, textPaint)

        textPaint.isFakeBoldText = true
        canvas.drawText("Valid Until:", 310f, 175f, textPaint)
        textPaint.isFakeBoldText = false
        canvas.drawText(dateFormat.format(Date(permit.endTime)), 395f, 175f, textPaint)

        // Work Scope
        textPaint.color = Color.rgb(11, 87, 208)
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        canvas.drawText("1. Work Title & Description", 20f, 210f, textPaint)

        textPaint.color = Color.rgb(40, 40, 40)
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = true
        canvas.drawText(permit.title, 30f, 228f, textPaint)
        textPaint.isFakeBoldText = false
        val desc1 = if (permit.description.length > 80) permit.description.substring(0, 80) else permit.description
        canvas.drawText(desc1, 30f, 243f, textPaint)

        // Mandatory Safety Checklist
        textPaint.color = Color.rgb(11, 87, 208)
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        canvas.drawText("2. Mandatory Safety Precautions & Checklist", 20f, 275f, textPaint)

        val gson = Gson()
        val checkType = object : TypeToken<List<ChecklistItem>>() {}.type
        val checklists: List<ChecklistItem> = try {
            gson.fromJson(permit.checklistJson, checkType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        var curY = 295f
        checklists.take(5).forEachIndexed { idx, item ->
            paint.color = if (item.checked) Color.rgb(19, 115, 51) else Color.GRAY
            canvas.drawCircle(35f, curY - 4f, 4f, paint)

            textPaint.color = Color.rgb(30, 30, 30)
            textPaint.textSize = 9.5f
            val qText = if (item.question.length > 78) item.question.substring(0, 78) + "..." else item.question
            canvas.drawText("${idx + 1}. $qText", 45f, curY, textPaint)
            curY += 18f
        }

        // Digital Acceptance & Signatures section
        textPaint.color = Color.rgb(11, 87, 208)
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        canvas.drawText("3. Digital Signatures & Multi-Tier Authorization", 20f, curY + 20f)
        curY += 35f

        val sigType = object : TypeToken<List<DigitalSignature>>() {}.type
        val signatures: List<DigitalSignature> = try {
            gson.fromJson(permit.signaturesJson, sigType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val requiredTiers = listOf(
            "Site Supervisor (Issuer)",
            "Department Head",
            "Safety Officer",
            "Contractor Representative"
        )

        // Draw 2x2 grid of signature boxes
        val boxWidth = 265f
        val boxHeight = 75f

        requiredTiers.forEachIndexed { i, tier ->
            val col = i % 2
            val row = i / 2
            val left = 20f + col * (boxWidth + 15f)
            val top = curY + row * (boxHeight + 12f)

            paint.color = Color.rgb(250, 250, 252)
            canvas.drawRect(left, top, left + boxWidth, top + boxHeight, paint)

            paint.color = Color.rgb(210, 215, 225)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(left, top, left + boxWidth, top + boxHeight, paint)
            paint.style = Paint.Style.FILL

            val matchedSig = signatures.find { it.roleName.contains(tier, ignoreCase = true) || tier.contains(it.roleName, ignoreCase = true) }

            textPaint.color = Color.rgb(11, 87, 208)
            textPaint.textSize = 9.5f
            textPaint.isFakeBoldText = true
            canvas.drawText(tier, left + 8f, top + 15f, textPaint)

            if (matchedSig != null) {
                // If Base64 bitmap is available
                if (matchedSig.signatureBase64.startsWith("iVBORw0KGgo") || matchedSig.signatureBase64.length > 50) {
                    try {
                        val decodedBytes = Base64.decode(matchedSig.signatureBase64, Base64.DEFAULT)
                        val sigBmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (sigBmp != null) {
                            val sigRect = Rect((left + 8f).toInt(), (top + 20f).toInt(), (left + 100f).toInt(), (top + 55f).toInt())
                            canvas.drawBitmap(sigBmp, null, sigRect, paint)
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }

                textPaint.color = Color.rgb(19, 115, 51)
                textPaint.textSize = 8.5f
                textPaint.isFakeBoldText = true
                canvas.drawText("✓ DIGITALLY SIGNED", left + 110f, top + 32f, textPaint)

                textPaint.color = Color.rgb(60, 60, 60)
                textPaint.textSize = 8f
                textPaint.isFakeBoldText = false
                canvas.drawText(matchedSig.signerName, left + 110f, top + 45f, textPaint)
                canvas.drawText(dateFormat.format(Date(matchedSig.timestamp)), left + 110f, top + 58f, textPaint)
            } else {
                textPaint.color = Color.rgb(180, 180, 180)
                textPaint.textSize = 9f
                textPaint.isFakeBoldText = false
                canvas.drawText("[Pending Digital Signature]", left + 10f, top + 45f, textPaint)
            }
        }

        // Footer
        paint.color = Color.rgb(240, 240, 240)
        canvas.drawRect(0f, 800f, 595f, 842f, paint)

        textPaint.color = Color.rgb(100, 100, 100)
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = false
        canvas.drawText("Himgiri Castings Pvt. Ltd. | Emergency Plant Contact: +91 9359610323 | Safety is Everyone's Responsibility", 40f, 822f, textPaint)

        pdfDoc.finishPage(page)

        return try {
            val fileDir = File(context.cacheDir, "permits")
            if (!fileDir.exists()) fileDir.mkdirs()
            val file = File(fileDir, "Permit_${permit.permitNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            pdfDoc.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            pdfDoc.close()
            null
        }
    }
}
