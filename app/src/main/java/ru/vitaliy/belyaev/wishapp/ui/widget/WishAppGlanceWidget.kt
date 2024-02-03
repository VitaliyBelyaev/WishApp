package ru.vitaliy.belyaev.wishapp.ui.widget

import android.content.Context
import androidx.compose.material3.Text
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent

class WishAppGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.


        provideContent {
            // create your AppWidget here
            Text("Hello World WISHAPP!")
        }
    }
}