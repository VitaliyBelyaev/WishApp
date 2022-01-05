package ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.createAppFeedback
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.SettingBlock
import ru.vitaliy.belyaev.wishapp.utils.annotatedStringResource
import ru.vitaliy.belyaev.wishapp.utils.createSendEmailIntent

@ExperimentalMaterialApi
@Composable
fun AboutAppScreen(
    onBackPressed: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit
) {

    LaunchedEffect(Unit) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "AboutApp")
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lazyListState: LazyListState = rememberLazyListState()

    val onSendFeedbackClicked: () -> Unit = {
        Firebase.analytics.logEvent("send_feedback_click", null)
        val feedback = createAppFeedback(context.resources)
        val intent = createSendEmailIntent(feedback.email, feedback.subject, feedback.message)
        try {
            context.startActivity(intent)
        } catch (t: Throwable) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.no_email_app_error))
            }
        }
    }

    Scaffold(
        topBar = {
            WishAppTopBar(
                stringResource(R.string.about_app),
                withBackIcon = true,
                onBackPressed = onBackPressed,
                lazyListState = lazyListState
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        val licensesTitle = stringResource(R.string.licenses)
        val onLicensesClick: () -> Unit = {
            OssLicensesMenuActivity.setActivityTitle(licensesTitle)
            val intent = Intent(context, OssLicensesMenuActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }
        val uriHandler = LocalUriHandler.current

        LazyColumn(state = lazyListState) {
            item {
                Text(
                    text = stringResource(
                        R.string.app_version_pattern,
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    ),
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
            item {
                val appDescription = annotatedStringResource(R.string.app_description)
                ClickableText(
                    text = appDescription,
                    style = TextStyle.Default.copy(fontSize = 16.sp, color = Color.Gray),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    onClick = {
                        Firebase.analytics.logEvent("source_code_url_click", null)
                        appDescription
                            .getStringAnnotations("URL", it, it)
                            .firstOrNull()?.let { stringAnnotation ->
                                uriHandler.openUri(stringAnnotation.item)
                            }
                    }
                )

            }
            item {
                Divider(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            }

            item {
                SettingBlock(
                    title = stringResource(R.string.feedback),
                    onClick = { onSendFeedbackClicked() }
                )
            }

            item {
                SettingBlock(
                    title = licensesTitle,
                    onClick = { onLicensesClick() }
                )
            }

            item {
                SettingBlock(
                    title = stringResource(R.string.privacy_policy),
                    onClick = { onPrivacyPolicyClicked() }
                )
            }
        }
    }
}