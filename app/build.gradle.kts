import com.android.build.api.dsl.VariantDimension
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinComposeCompilerPlugin)
    alias(libs.plugins.hilt)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    id("com.google.android.gms.oss-licenses-plugin")
}

val amplitudeApiKey = "AMPLITUDE_API_KEY"
val apikeyProperties = readProperties("../apikey.properties")
val amplitudeApiKeyValue = apikeyProperties.getStringOrDefault(
    key = amplitudeApiKey,
    default = ""
)

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("11")
    }
}

android {
    namespace = "ru.vitaliy.belyaev.wishapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ru.vitaliy.belyaev.wishapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 26
        versionName = "1.9.1"

        // Страница в Google Play всегда у релизного id: у debug и autotest к нему добавлен суффикс,
        // и ссылка из такой сборки вела бы на несуществующую страницу
        buildConfigField("String", "STORE_APP_ID", "\"$applicationId\"")

        // Ключ Amplitude только в release, иначе debug и автотесты пишут в боевой проект
        stringConfigField(amplitudeApiKey, "")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    compileOptions {
        // В прогоне тестов на autotest desugaring выключен. Тестовый APK несёт свою
        // урезанную копию j$.time (её тянет androidx.test), в одном процессе она
        // перекрывает копию приложения, и код приложения падает с NoSuchMethodError.
        // Без desugaring java.time берётся из системы, поэтому такой прогон только
        // на Android 8 (API 26) и выше.
        isCoreLibraryDesugaringEnabled = providers.gradleProperty("testBuildType").orNull != "autotest"
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/INDEX.LIST"
        }
    }

    signingConfigs {
        maybeCreate("signingTest")
        getByName("signingTest") {
            storeFile = file("test_keystore")
            storePassword = "notasecret"
            keyAlias = "test"
            keyPassword = "notasecret"
        }
    }

    // Сборка для Android-тестов: `-PtestBuildType=autotest`, по умолчанию debug
    testBuildType = providers.gradleProperty("testBuildType").getOrElse("debug")

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "release_app_name", "@string/debug_app_name")
            signingConfig = signingConfigs.getByName("signingTest")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "release_app_name", "@string/app_name")
            stringConfigField(amplitudeApiKey, amplitudeApiKeyValue)
            signingConfig = signingConfigs.getByName("signingTest")
        }

        // Как release, с R8, но для автотестов: ловит то, что R8 вырезал или переименовал.
        // Keep-правила для тестов лежат отдельно и в release не попадают.
        create("autotest") {
            initWith(getByName("release"))
            // Тот же id, что у debug: тест гоняется на отдельном эмуляторе, и сборки
            // не нужно ставить рядом. Заодно не нужен свой клиент в google-services.json
            applicationIdSuffix = ".debug"
            matchingFallbacks += "release"
            proguardFiles("proguard-autotest.pro")
            resValue("string", "release_app_name", "@string/autotest_app_name")
            stringConfigField(amplitudeApiKey, "")
            signingConfig = signingConfigs.getByName("signingTest")
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.tools.desugar)

    implementation(project(":shared"))

    implementation(libs.kotlin.serialization.json)

    // Android X common
    implementation(libs.androidx.core.coreKtx)
    implementation(libs.androidx.core.coreSplashscreen)
    implementation(libs.androidx.lifecycle.lifecycleExtensions)
    implementation(libs.androidx.lifecycle.lifecycleRuntimeKtx)
    implementation(libs.androidx.lifecycle.lifecycleRuntimeCompose)
    implementation(libs.androidx.datastore.datastorePreferences)
    implementation(libs.androidx.appcompat.appcompat)
    implementation(libs.androidx.webkit.webkit)
    implementation(libs.androidx.activity.activityCompose)
    implementation(libs.androidx.constraintlayout.constraintlayoutCompose)
    implementation(libs.androidx.exifinterface.exifinterface)

    // Compose
    implementation(platform(libs.androidx.compose.composeBom))
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui.toolingPreview)
    implementation(libs.androidx.compose.materialIconsCore)

    // Compose Navigation
    implementation(libs.androidx.navigation.navigationCompose)
    implementation(libs.androidx.hilt.lifecycleViewmodelCompose)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // Google Android
    implementation(libs.google.android.material)
    implementation(libs.google.android.play.review)
    implementation(libs.google.android.play.reviewKtx)

    // Google Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)

    // GMS
    implementation(libs.google.android.gms.playServices.ossLicenses)
    implementation(libs.google.android.gms.playServices.auth)

//    // Guava
//    implementation("com.google.guava:guava:24.1-jre")
//    // Guava fix
//    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    // Google Drive
    implementation(libs.google.api.client.android) {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(libs.google.api.services.drive) {
        exclude(group = "org.apache.httpcomponents")
    }

    // Amplitude
    implementation(libs.amplitude)

    // DI
    implementation(libs.koin.android.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Okhttp
    implementation(platform(libs.squareup.okhttp3.okhttpBom))
    implementation(libs.squareup.okhttp3.okhttp)
    implementation(libs.squareup.okhttp3.logging)

    // Work with images
    implementation(libs.coilKt.coilCompose)
    implementation(libs.saket.telephoto.zoomableImageCoil)

    // Flexbox
    implementation(libs.google.android.flexbox)

    // Timber
    implementation(libs.timber)

    // LeakCanary
    debugImplementation(libs.squareup.leakcanary.leakcanaryAndroid)

    // JUnit
    testImplementation(libs.junit.junit)
    testImplementation(libs.hamcrest.hamcrestJunit)
    testImplementation(libs.kotlin.coroutines.test)

    // Mockito
    testImplementation(libs.mockito.mockitoCore)

    // Instrumentation
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.kotlin.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.composeBom))
    androidTestImplementation(libs.androidx.compose.ui.testJunit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

fun readProperties(filePath: String) = Properties().apply {
    try {
        file(filePath).inputStream().use { fis ->
            load(fis)
        }
    } catch (error: Throwable) {
        println("Error reading properties file: $filePath, error: $error")
    }
}

fun Properties.getStringOrDefault(key: String, default: String): String {
    return getProperty(key)?.removeSurrounding("\"") ?: default
}

fun VariantDimension.stringConfigField(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
}