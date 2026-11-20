package ru.vitaliy.belyaev.wishapp.ui.screens.about

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.utils.PrivacyPolicy
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeader
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeaderCollapse
import ru.vitaliy.belyaev.wishapp.ui.components.header.headerHeight
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.displayM
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

/** Как загрузилась страница политики. */
internal enum class PolicyLoad { LOADING, LOADED, FAILED }

/**
 * Политика конфиденциальности внутри приложения (экран `policy` в прототипе): страница с сайта. Без сети —
 * «Политика не открылась» с «Повторить».
 */
@Composable
fun PolicyScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: PolicyViewModel = hiltViewModel()) {
    trackScreenShow { viewModel.trackScreenShow() }

    var load by remember { mutableStateOf(PolicyLoad.LOADING) }
    var attempt by remember { mutableIntStateOf(0) }
    var scrollY by remember { mutableIntStateOf(0) }
    val collapsePx = with(LocalDensity.current) { PageHeaderCollapse.toPx() }
    // Страница идёт под статус-бар и панель навигации: отступы задаём ей самой, в CSS-пикселях (= dp)
    val density = LocalDensity.current
    val top = headerHeight().value + WishAppSpacing.s2.value
    val bottom = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }.value + PAGE_BOTTOM_DP

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        PolicyWebView(
            url = PrivacyPolicy.url,
            attempt = attempt,
            insetsScript = "document.documentElement.style.paddingTop='${top}px';" +
                "document.documentElement.style.paddingBottom='${bottom}px';",
            onLoad = { load = it },
            onScroll = { scrollY = it },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                // Своя страница ошибки WebView не нужна: вместо неё наш экран
                .alpha(if (load == PolicyLoad.LOADED) 1f else 0f),
        )
        when (load) {
            PolicyLoad.LOADING -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .testTag(PolicyTestTags.LOADING),
            )
            PolicyLoad.FAILED -> PolicyFailed(
                onRetry = {
                    load = PolicyLoad.LOADING
                    attempt++
                },
            )
            PolicyLoad.LOADED -> Unit
        }
        PageHeader(
            title = stringResource(R.string.privacy_policy),
            progress = { if (load == PolicyLoad.LOADED) (scrollY / collapsePx).coerceIn(0f, 1f) else 0f },
            onBack = onBack,
        )
    }
}

/**
 * WebView со страницей: ошибка главной страницы (нет сети, ответ 4xx/5xx) — [PolicyLoad.FAILED].
 * [attempt] растёт на «Повторить» и загружает страницу заново.
 */
@Composable
private fun PolicyWebView(
    url: String,
    attempt: Int,
    insetsScript: String,
    onLoad: (PolicyLoad) -> Unit,
    onScroll: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnLoad by rememberUpdatedState(onLoad)
    val currentOnScroll by rememberUpdatedState(onScroll)
    val client = remember { PolicyWebClient(onLoad = { currentOnLoad(it) }) }
    client.insetsScript = insetsScript
    AndroidView(
        modifier = modifier.testTag(PolicyTestTags.WEB),
        factory = { context ->
            WebView(context).apply {
                // Прозрачный, чтобы в тёмной теме не вспыхивал белым до загрузки
                setBackgroundColor(Color.TRANSPARENT)
                // Только чтобы задать странице отступы под шапку и панель навигации
                @SuppressLint("SetJavaScriptEnabled")
                settings.javaScriptEnabled = true
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                }
                webViewClient = client
                setOnScrollChangeListener { _, _, y, _, _ -> currentOnScroll(y) }
                tag = attempt
                loadUrl(url)
            }
        },
        update = { webView ->
            client.applyInsets(webView)
            if (webView.tag != attempt) {
                webView.tag = attempt
                client.retry()
                webView.loadUrl(url)
            }
        },
        onRelease = { it.destroy() },
    )
}

/**
 * После ошибки WebView сам открывает свою страницу ошибки и снова зовёт `onPageStarted` и
 * `onPageFinished`. Их не слушаем: ошибка держится до «Повторить» ([retry]).
 * Страница показывается, когда ей заданы отступы [insetsScript]: без них текст мелькнул бы под шапкой.
 */
private class PolicyWebClient(private val onLoad: (PolicyLoad) -> Unit) : WebViewClient() {

    private var failed = false
    private var loaded = false
    var insetsScript = ""

    /** Отступы поменялись (поворот, другая панель навигации): задаём их уже открытой странице. */
    fun applyInsets(view: WebView) {
        if (loaded) view.evaluateJavascript(insetsScript, null)
    }

    fun retry() {
        failed = false
        loaded = false
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        loaded = false
        if (!failed) onLoad(PolicyLoad.LOADING)
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (request.isForMainFrame) fail()
    }

    override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
        if (request.isForMainFrame && response.statusCode >= HTTP_ERROR) fail()
    }

    override fun onPageFinished(view: WebView, url: String?) {
        if (failed) return
        view.evaluateJavascript(insetsScript) {
            if (!failed) {
                loaded = true
                onLoad(PolicyLoad.LOADED)
            }
        }
    }

    private fun fail() {
        if (failed) return
        failed = true
        onLoad(PolicyLoad.FAILED)
    }
}

/** «Политика не открылась»: по центру экрана, кнопка «Повторить». */
@Composable
private fun PolicyFailed(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = WishAppSpacing.s8)
            .testTag(PolicyTestTags.FAILED),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.policy_failed_title),
            style = MaterialTheme.typography.displayM,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.policy_failed_text),
            style = MaterialTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = WishAppSpacing.s2, bottom = 20.dp),
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            modifier = Modifier.height(52.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_refresh),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = WishAppSpacing.s2)
                    .size(18.dp),
            )
            Text(text = stringResource(R.string.retry), style = MaterialTheme.typography.label)
        }
    }
}

object PolicyTestTags {
    const val WEB = "policy_web"
    const val LOADING = "policy_loading"
    const val FAILED = "policy_failed"
}

private const val HTTP_ERROR = 400

/** Воздух под текстом страницы над панелью навигации, dp. */
private const val PAGE_BOTTOM_DP = 40f
