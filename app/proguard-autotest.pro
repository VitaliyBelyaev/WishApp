# Keep-правила только для сборки autotest, в release не попадают.
#
# R8 не видит тесты и вырезает или переименовывает то, что приложению не нужно,
# а тест вызывает напрямую. Здесь только то, что SmokeTest и DemoData трогают
# в обход интерфейса: заполнение базы и отключение аналитики. Всё, что тест
# прокликивает, проходит через R8 как в релизе. Не расширяйте список без нужды:
# каждый keep прячет класс от проверки.

# Библиотеки, которые нужны самому тестовому APK. Он не несёт копий библиотек,
# которые уже есть в приложении, и берёт их из приложения. Без этих правил R8
# вырезает то, что приложению не нужно, и тест падает раньше первого шага.
# Без allowobfuscation: с переименованием R8 расходится с тестовым APK на
# служебных классах Kotlin, например DefaultImpls у ContinuationInterceptor.
-keep class androidx.tracing.** { *; }
-keep class androidx.concurrent.futures.** { *; }
-keep class androidx.collection.** { *; }
-keep class androidx.core.view.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Код приложения, который SmokeTest и DemoData вызывают напрямую: заполнение базы
# и отключение аналитики перед запуском.
-keep class ru.vitaliy.belyaev.wishapp.R$string { *; }
-keep class ru.vitaliy.belyaev.wishapp.ui.AppActivity
-keep class ru.vitaliy.belyaev.wishapp.data.repository.analytics.AmplitudeWrapper { *; }
-keep class ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk { *; }
-keep class ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory { *; }
-keep class ru.vitaliy.belyaev.wishapp.shared.data.repository.DatabaseRepository { *; }
-keep class ru.vitaliy.belyaev.wishapp.shared.domain.LinksAdapter { *; }
-keep class ru.vitaliy.belyaev.wishapp.shared.domain.entity.** { *; }

# Отключение Firebase перед тестом
-keep class com.google.firebase.analytics.FirebaseAnalytics {
    public static *** getInstance(android.content.Context);
    public void setAnalyticsCollectionEnabled(boolean);
}
-keep class com.google.firebase.crashlytics.FirebaseCrashlytics {
    public static *** getInstance();
    public void setCrashlyticsCollectionEnabled(boolean);
}
-keep class com.amplitude.core.Amplitude {
    public *** getConfiguration();
}
-keep class com.amplitude.core.Configuration {
    public void setOptOut(boolean);
}
