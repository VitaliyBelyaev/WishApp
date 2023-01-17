package ru.vitaliy.belyaev.wishapp.ui.core.webview

import android.annotation.SuppressLint
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebViewFeature

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageBlock(
    modifier: Modifier = Modifier,
    urlToRender: String
) {

    val isDarkTheme = isSystemInDarkTheme()
    AndroidView(
        modifier = modifier,
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                        WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                    }
                } else {
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                        if (isDarkTheme) {
                            WebSettingsCompat.setForceDark(settings, FORCE_DARK_ON)
                        } else {
                            WebSettingsCompat.setForceDark(settings, FORCE_DARK_OFF)
                        }
                    }
                }
            }
        },
        update = { it.loadUrl(urlToRender) }
    )
}