package ru.vitaliy.belyaev.wishapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.flexbox.FlexboxLayout
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishEntity

const val WISHLIST_PDF_DIR_NAME = "wishlist_pdf"

object ShareWishListPdfGenerator {

    private const val PAGE_WIDTH_A4 = 595
    private const val PAGE_HEIGHT_A4 = 842
    private const val BITMAP_SAMPLE_SIZE = 4
    private const val BITMAP_HEIGHT = 120
    private const val IMAGES_MARGIN = 2
    private const val WISHES_SPACER_HEIGHT = 10

    fun generatePdfForWishList(
        @StringRes titleResId: Int,
        wishes: List<WishEntity>,
        context: Context,
    ): File {
        val pdfDir = File(context.filesDir, WISHLIST_PDF_DIR_NAME)
        if (!pdfDir.exists()) {
            pdfDir.mkdir()
        }
        val pdfFile = File(pdfDir, "WishApp_wishlist_${System.currentTimeMillis()}.pdf")

        // Create and fill with data view for drawing on pdf page canvas
        val allBitmaps = mutableListOf<Bitmap>()
        val view = createView(context, wishes, context.getString(titleResId), allBitmaps)

        // Measure view with width set exact to A4 page width and height set to unspecified for
        // measuring how tall view with all data set want to be
        val specWidth = View.MeasureSpec.makeMeasureSpec(PAGE_WIDTH_A4, View.MeasureSpec.EXACTLY)
        val specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(specWidth, specHeight)

        val requiredWidth: Int = view.measuredWidth
        val requiredHeight: Int = view.measuredHeight

        val pageHeight = max(PAGE_HEIGHT_A4, requiredHeight)
        val pageInfo = PageInfo.Builder(
            PAGE_WIDTH_A4,
            pageHeight,
            1
        ).create()

        val document = PdfDocument()
        val page: PdfDocument.Page = document.startPage(pageInfo)

        // Layout and draw view on page canvas
        view.layout(0, 0, requiredWidth, requiredHeight)
        view.draw(page.canvas)

        // Recycle all bitmaps to free memory
        allBitmaps.forEach { it.recycle() }
        allBitmaps.clear()

        // Finish page and write document to file
        document.finishPage(page)
        pdfFile.outputStream().use {
            document.writeTo(it)
            document.close()
        }

        return pdfFile
    }

    private fun createView(
        context: Context,
        wishes: List<WishEntity>,
        title: String,
        allBitmaps: MutableList<Bitmap>
    ): LinearLayout {
        val view: LinearLayout =
            LayoutInflater.from(context).inflate(R.layout.wishes_pdf_view, null) as LinearLayout

        view.findViewById<TextView>(R.id.title).apply {
            text = title
        }

        wishes.forEachIndexed { wishIndex, wishEntity ->
            val wishView = LayoutInflater.from(context).inflate(R.layout.wish_container_view, null) as LinearLayout

            val titleView = wishView.findViewById<TextView>(R.id.wishTitle)
            val commentView = wishView.findViewById<TextView>(R.id.wishComment)
            val linksContainer = wishView.findViewById<LinearLayout>(R.id.linksContainer)
            val imagesContainer = wishView.findViewById<FlexboxLayout>(R.id.imagesContainer).apply {
                removeAllViews()
            }

            val numberedTitle = "${wishIndex + 1}. ${wishEntity.title}"
            titleView.text = numberedTitle

            with(commentView) {
                text = wishEntity.comment
                isVisible = wishEntity.comment.isNotBlank()
            }

            setupAndFillLinksContainerView(context, linksContainer, wishEntity)

            val options = BitmapFactory.Options().apply {
                inSampleSize = BITMAP_SAMPLE_SIZE
            }
            val bitmaps = wishEntity.images.map {
                BitmapFactory.decodeByteArray(it.rawData, 0, it.rawData.size, options)
            }
            allBitmaps.addAll(bitmaps)

            bitmaps.forEach { bitmap ->
                val imageView = ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setImageBitmap(bitmap)
                }

                val bitmapRatio = bitmap.width.toDouble() / bitmap.height.toDouble()

                // Height always BITMAP_HEIGHT
                val imageViewHeight = BITMAP_HEIGHT
                val imageViewWidth = (imageViewHeight * bitmapRatio).roundToInt()

                val lp = FlexboxLayout.LayoutParams(imageViewWidth, imageViewHeight).apply {
                    setMargins(0, 0, IMAGES_MARGIN, IMAGES_MARGIN)
                }

                imagesContainer.addView(imageView, lp)
            }
            view.addView(wishView)

            View(context).apply {
                val spacerViewLp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    WISHES_SPACER_HEIGHT
                )
                view.addView(this, spacerViewLp)
            }
        }

        return view
    }

    private fun setupAndFillLinksContainerView(
        context: Context,
        linksContainer: LinearLayout,
        wishEntity: WishEntity
    ) {
        linksContainer.removeAllViews()

        val isVisible = wishEntity.links.isNotEmpty()
        linksContainer.isVisible = isVisible
        if (!isVisible) {
            return
        }

        wishEntity.links.forEach { link ->
            val linkView = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, 9f)
                text = link
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                hyperlinkStyle(link)
                movementMethod = LinkMovementMethod.getInstance()
            }

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            linksContainer.addView(linkView, lp)
        }
    }

    private fun TextView.hyperlinkStyle(url: String) {
        setText(
            SpannableString(text).apply {
                setSpan(
                    URLSpan(url),
                    0,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            context,
                            R.color.launcherIconPrimaryColor
                        )
                    ),
                    0,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

            },
            TextView.BufferType.SPANNABLE
        )
    }
}