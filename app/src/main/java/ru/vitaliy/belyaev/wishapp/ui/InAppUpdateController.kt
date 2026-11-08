package ru.vitaliy.belyaev.wishapp.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppUpdateAnsweredEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppUpdateShownEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import timber.log.Timber

/**
 * Мягкое обновление через Google Play (in-app update, гибкий режим): Play качает новую версию
 * в фоне, человек продолжает пользоваться приложением, а когда всё скачано, на главном экране
 * снэкбар «Перезапустить» (один раз за запуск, следит `AppActivityViewModel`).
 *
 * Проверяем при каждом выходе приложения на экран (`onStart`): и при запуске, и когда его разворачивают,
 * ведь многие не закрывают приложение, а сворачивают. Предлагаем, только если устройство знает
 * о новой версии не меньше [MinStalenessDays] дней: сутки запаса, чтобы успеть остановить раскатку
 * плохой версии. После показа следующий не раньше чем через [PromptIntervalDays] дней: вернувшиеся
 * пользователи обычно заходят раз в 6–7 дней (Amplitude, ноябрь 2026), и при 5 днях предложение
 * попадает на ближайший заход, а не через один.
 * Работает только у сборок, установленных из Google Play: в остальных Play отвечает ошибкой,
 * и предложения просто нет.
 *
 * Создавать в `onCreate` активити: окно Play запускается через Activity Result API.
 */
internal class InAppUpdateController(
    private val activity: ComponentActivity,
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val onUpdateDownloaded: () -> Unit,
) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    private val updateFlowLauncher: ActivityResultLauncher<IntentSenderRequest> =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            analyticsRepository.trackEvent(InAppUpdateAnsweredEvent(accepted = result.resultCode == Activity.RESULT_OK))
        }

    // Проверка уже идёт: onStart может прийти снова, пока Play отвечает
    private var checkInProgress = false

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) onUpdateDownloaded()
    }

    init {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                appUpdateManager.registerListener(installStateListener)
            }

            override fun onStart(owner: LifecycleOwner) {
                checkForUpdate()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                appUpdateManager.unregisterListener(installStateListener)
            }
        })
    }

    private fun checkForUpdate() {
        if (checkInProgress) return
        checkInProgress = true
        activity.lifecycleScope.launch {
            try {
                val info = appUpdateManager.requestAppUpdateInfo()
                // Обновление могло докачаться, пока приложение было свёрнуто или закрыто
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    onUpdateDownloaded()
                } else if (shouldPrompt(info)) {
                    prompt(info)
                }
            } catch (e: Exception) {
                // Не из Google Play, нет Play или сети: ожидаемо, в Crashlytics не шлём
                Timber.w(e, "In-app update info is not available")
            } finally {
                checkInProgress = false
            }
        }
    }

    /** Поставить скачанное обновление: Play перезапустит приложение. */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    private suspend fun shouldPrompt(info: AppUpdateInfo): Boolean {
        // Уже качается или ждёт установки: второй раз не предлагаем
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) return false
        if (info.installStatus() in InProgressStatuses) return false
        if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) return false
        if ((info.clientVersionStalenessDays() ?: 0) < MinStalenessDays) return false
        val shownAt = dataStoreRepository.getUpdatePromptShownAtMillis() ?: return true
        return System.currentTimeMillis() - shownAt >= TimeUnit.DAYS.toMillis(PromptIntervalDays)
    }

    private suspend fun prompt(info: AppUpdateInfo) {
        val started = appUpdateManager.startUpdateFlowForResult(
            info,
            updateFlowLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
        if (!started) return
        dataStoreRepository.updateUpdatePromptShownAtMillis(System.currentTimeMillis())
        analyticsRepository.trackEvent(
            InAppUpdateShownEvent(
                stalenessDays = info.clientVersionStalenessDays() ?: 0,
                availableVersionCode = info.availableVersionCode(),
            )
        )
    }

    private companion object {
        const val MinStalenessDays = 1
        const val PromptIntervalDays = 5L

        // Отменённая или сорвавшаяся загрузка сюда не входит: такое обновление предлагаем снова
        val InProgressStatuses = setOf(
            InstallStatus.PENDING,
            InstallStatus.DOWNLOADING,
            InstallStatus.INSTALLING,
            InstallStatus.DOWNLOADED,
        )
    }
}
