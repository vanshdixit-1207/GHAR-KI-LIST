package com.example.pdf

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.GroceryCategory
import com.example.data.model.GroceryItemEntity
import com.example.data.model.Language
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfShareManager {

    /**
     * Generates a high-contrast, clean PDF list formatted for Indian Kirana Store / grocery shopping
     */
    fun generateGroceryPdf(
        context: Context,
        selectedItems: List<GroceryItemEntity>,
        language: Language,
        storeOrFamilyName: String = ""
    ): File? {
        if (selectedItems.isEmpty()) return null

        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 width (points)
            val pageHeight = 842 // Standard A4 height (points)
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val currentDate = sdf.format(Date())

            var currentY = 40f

            // 1. Header Banner
            val headerRect = RectF(30f, currentY, pageWidth - 30f, currentY + 70f)
            paint.color = Color.rgb(27, 94, 32) // Deep Green
            canvas.drawRoundRect(headerRect, 8f, 8f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 20f
            val titleText = if (language == Language.HINDI) "घर की लिस्ट — किराना पर्ची" else "Ghar Ki List — Grocery Shopping Slip"
            canvas.drawText(titleText, 45f, currentY + 32f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.DEFAULT
            val subText = "📅 $currentDate  |  📋 ${if (language == Language.HINDI) "कुल सामान" else "Total Items"}: ${selectedItems.size}"
            canvas.drawText(subText, 45f, currentY + 54f, paint)

            currentY += 85f

            // Optional Store/Family Note
            if (storeOrFamilyName.isNotBlank()) {
                paint.color = Color.rgb(230, 81, 0) // Warm saffron
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("🏪 $storeOrFamilyName", 35f, currentY + 12f, paint)
                currentY += 24f
            }

            // 2. Table Header
            paint.color = Color.rgb(243, 244, 246) // Light Gray
            val tableHeaderRect = RectF(30f, currentY, pageWidth - 30f, currentY + 26f)
            canvas.drawRoundRect(tableHeaderRect, 4f, 4f, paint)

            paint.color = Color.rgb(31, 41, 55)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f

            canvas.drawText("क्र.", 40f, currentY + 18f, paint)
            canvas.drawText(if (language == Language.HINDI) "सामान का नाम" else "Item Name", 75f, currentY + 18f, paint)
            canvas.drawText(if (language == Language.HINDI) "मात्रा" else "Qty", 320f, currentY + 18f, paint)
            canvas.drawText(if (language == Language.HINDI) "श्रेणी" else "Category", 410f, currentY + 18f, paint)
            canvas.drawText(if (language == Language.HINDI) "दुकानदार टिक" else "Done", 505f, currentY + 18f, paint)

            currentY += 34f

            // Group items by category for clear reading
            val grouped = selectedItems.groupBy { it.category }
            var itemIndex = 1

            for ((categoryKey, itemsInCat) in grouped) {
                val category = GroceryCategory.fromId(categoryKey)

                // Category Section Header
                if (currentY > pageHeight - 60f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                }

                paint.color = Color.rgb(232, 245, 233)
                val catHeaderRect = RectF(30f, currentY, pageWidth - 30f, currentY + 22f)
                canvas.drawRect(catHeaderRect, paint)

                paint.color = Color.rgb(27, 94, 32)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 12f
                val catTitle = "${category.emoji} ${category.getDisplayName(language)}"
                canvas.drawText(catTitle, 38f, currentY + 16f, paint)

                currentY += 26f

                // Items in this category
                for (item in itemsInCat) {
                    if (currentY > pageHeight - 50f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f
                    }

                    // Row Zebra Background
                    if (itemIndex % 2 == 0) {
                        paint.color = Color.rgb(250, 250, 250)
                        canvas.drawRect(RectF(30f, currentY, pageWidth - 30f, currentY + 22f), paint)
                    }

                    // Index
                    paint.color = Color.rgb(107, 114, 128)
                    paint.typeface = Typeface.DEFAULT
                    paint.textSize = 11f
                    canvas.drawText("$itemIndex.", 40f, currentY + 16f, paint)

                    // Item Name (Bilingual)
                    paint.color = Color.rgb(17, 24, 39)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 12f
                    val itemName = "${item.iconEmoji} ${item.nameHindi} (${item.nameEnglish})"
                    // Truncate if too long
                    val safeName = if (itemName.length > 36) itemName.take(34) + ".." else itemName
                    canvas.drawText(safeName, 75f, currentY + 16f, paint)

                    // Quantity (Highlighted)
                    paint.color = Color.rgb(198, 40, 40)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 12f
                    canvas.drawText(item.quantity, 320f, currentY + 16f, paint)

                    // Category Name
                    paint.color = Color.rgb(107, 114, 128)
                    paint.typeface = Typeface.DEFAULT
                    paint.textSize = 10f
                    val catShort = category.getDisplayName(language)
                    val safeCat = if (catShort.length > 14) catShort.take(12) + ".." else catShort
                    canvas.drawText(safeCat, 410f, currentY + 16f, paint)

                    // Checkbox outline for shopkeeper
                    paint.color = Color.rgb(156, 163, 175)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1f
                    canvas.drawRect(RectF(520f, currentY + 4f, 532f, currentY + 16f), paint)
                    paint.style = Paint.Style.FILL

                    currentY += 22f
                    itemIndex++
                }

                currentY += 6f
            }

            // 3. Footer
            paint.color = Color.rgb(156, 163, 175)
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            val footerText = if (language == Language.HINDI)
                "🙏 दुकानदार भाई, कृपया सामान अच्छी तरह पैक करें | घर की लिस्ट ऐप"
            else
                "🙏 Dear Kirana Store, please pack these items safely | Ghar Ki List App"
            canvas.drawText(footerText, 35f, pageHeight - 25f, paint)

            pdfDocument.finishPage(page)

            // Save PDF to cache dir
            val cacheDir = File(context.cacheDir, "grocery_pdfs").apply { mkdirs() }
            val pdfFile = File(cacheDir, "Ghar_Ki_List_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Formats items into a ready-to-send WhatsApp grocery list message
     */
    fun formatWhatsAppMessage(
        items: List<GroceryItemEntity>,
        language: Language,
        storeOrFamilyName: String = ""
    ): String {
        if (items.isEmpty()) return ""

        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date())

        val sb = StringBuilder()
        sb.append("🛒 *घर की लिस्ट / GHAR KI LIST* 🛒\n")
        sb.append("📅 *तारीख / Date:* ").append(dateStr).append("\n")
        sb.append("📋 *कुल सामान / Total Items:* ").append(items.size).append("\n")

        if (storeOrFamilyName.isNotBlank()) {
            sb.append("🏪 *दुकान / For:* ").append(storeOrFamilyName).append("\n")
        }
        sb.append("─────────────────────\n\n")

        val grouped = items.groupBy { it.category }
        for ((catKey, itemList) in grouped) {
            val category = GroceryCategory.fromId(catKey)
            sb.append(category.emoji).append(" *").append(category.getDisplayName(language)).append("*\n")
            for (item in itemList) {
                sb.append("  • ").append(item.iconEmoji).append(" *")
                    .append(item.nameHindi).append(" (").append(item.nameEnglish).append(")* : ")
                    .append(item.quantity).append("\n")
            }
            sb.append("\n")
        }

        sb.append("─────────────────────\n")
        sb.append("🙏 *दुकानदार भाई, कृपया ऊपर दिया गया सामान पैक करके तैयार रखें।*\n")
        sb.append("_📱 Created with Ghar Ki List App_")

        return sb.toString()
    }

    /**
     * Opens native share sheet to send the generated PDF file via WhatsApp / other apps
     */
    fun sharePdfFile(context: Context, pdfFile: File, language: Language) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareSubject = if (language == Language.HINDI) "घर की लिस्ट — किराना पर्ची" else "Ghar Ki List — Grocery Shopping Slip"
            val shareText = if (language == Language.HINDI) "🛒 घर की लिस्ट — कृपया यह किराना पर्ची देखकर सामान तैयार रखें।" else "🛒 Ghar Ki List — Please prepare the grocery items in this attached list."

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                putExtra(Intent.EXTRA_TEXT, shareText)
                clipData = ClipData.newRawUri("Grocery PDF", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserTitle = if (language == Language.HINDI) "दुकानदार को PDF भेजें (WhatsApp / Share)" else "Share Grocery PDF (WhatsApp / Any App)"
            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Shares formatted text directly to WhatsApp or native share sheet
     */
    fun shareTextToWhatsApp(context: Context, textMessage: String, language: Language) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textMessage)
            putExtra(Intent.EXTRA_SUBJECT, if (language == Language.HINDI) "घर की लिस्ट" else "Ghar Ki List")
        }

        // Try direct WhatsApp if installed
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, textMessage)
        }

        try {
            if (whatsappIntent.resolveActivity(context.packageManager) != null) {
                whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(whatsappIntent)
                return
            }
        } catch (_: Exception) {}

        // Fallback to standard chooser
        val chooser = Intent.createChooser(
            intent,
            if (language == Language.HINDI) "दुकानदार को लिस्ट भेजें" else "Send Grocery List"
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Copies message to clipboard
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ghar Ki List", text)
        clipboard.setPrimaryClip(clip)
    }
}
