package ru.vitaliy.belyaev.wishapp.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsNames
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.entity.WishWithTags
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppTheme
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    private val viewModel: AppActivityViewModel by viewModels()

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        super.onCreate(savedInstanceState)

        viewModel.wishListToShareLiveData.observe(this) {
            val wishListAsFormattedText = generateFormattedWishList(it)
            startActivity(createSharePlainTextIntent(wishListAsFormattedText))
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
            WishAppTheme(selectedTheme = selectedTheme) {
                Navigation { viewModel.onShareWishListClicked(it) }
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