package ru.vitaliy.belyaev.wishapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.model.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.theme.WishAppTheme

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    private val viewModel: AppActivityViewModel by viewModels()

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WishApp)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewModel.wishListToShareLiveData.observe(this) {
            val wishListAsFormattedText = generateFormattedWishList(it)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, wishListAsFormattedText)
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        viewModel.requestReviewLiveData.observe(this) {
            analyticsRepository.trackEvent(AnalyticsNames.Event.IN_APP_REVIEW_REQUESTED)
            val reviewManager = ReviewManagerFactory.create(this)
            reviewManager
                .requestReviewFlow()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        analyticsRepository.trackEvent(AnalyticsNames.Event.IN_APP_REVIEW_SHOWN)
                        reviewManager.launchReviewFlow(this, reviewInfo)
                    } else {
                        task.exception?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                    }
                }
        }

        setContent {
            val selectedTheme: Theme by viewModel.selectedTheme.collectAsState()
            val modeInt = when (selectedTheme) {
                Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            }
            // We need to change theme in Activity too for proper colors that we get from resources
            AppCompatDelegate.setDefaultNightMode(modeInt)
            WishAppTheme(selectedTheme = selectedTheme) {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = MaterialTheme.colors.isLight
                val navBarColor = MaterialTheme.colors.background
                SideEffect {
                    systemUiController.setNavigationBarColor(
                        color = navBarColor,
                        darkIcons = useDarkIcons
                    )
                }

                ProvideWindowInsets {
                    val insets = LocalWindowInsets.current
                    val statusBar = with(LocalDensity.current) { insets.statusBars.top.toDp() }
                    Surface(
                        color = MaterialTheme.colors.background,
                        modifier = Modifier.padding(top = statusBar)
                    ) {
                        Navigation { viewModel.onShareWishListClicked(it) }
                    }
                }
            }
        }
    }

    private fun generateFormattedWishList(wishes: List<WishWithTags>): String {
        val builder = StringBuilder().apply {
            append(getString(R.string.wish_list_title))
            append("\n\n")
        }

        wishes.forEachIndexed { index, wish ->
            val number = index + 1
            builder.append("$number. ${wish.title}\n")
            if (wish.comment.isNotBlank()) {
                builder.append("\n")
                builder.append("${wish.comment}\n")
            }
            if (wish.link.isNotBlank()) {
                builder.append("\n")
                builder.append("${wish.link}\n")
            }
            if (index != wishes.lastIndex) {
                builder.append("_____________")
                builder.append("\n\n")
            }
        }
        return builder.toString()
    }
}