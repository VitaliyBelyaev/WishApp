import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    id("com.google.android.gms.oss-licenses-plugin")
}

val amplitudeApiKey = "AMPLITUDE_API_KEY"
val apikeyProperties = readProperties(file("../apikey.properties"))

android {
    namespace = "ru.vitaliy.belyaev.wishapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.vitaliy.belyaev.wishapp"
        minSdk = 23
        targetSdk = 34
        versionCode = 21
        versionName = "1.7.0"

        buildConfigField("String", amplitudeApiKey, apikeyProperties[amplitudeApiKey] as String)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        android.buildFeatures.buildConfig = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/ASL2.0"
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
    implementation(libs.androidx.datastore.datastorePreferences)
    implementation(libs.androidx.appcompat.appcompat)
    implementation(libs.androidx.webkit.webkit)
    implementation(libs.androidx.activity.activityCompose)
    implementation(libs.androidx.constraintlayout.constraintlayoutCompose)
    implementation(libs.androidx.exifinterface.exifinterface)

    // Compose
    implementation(platform(libs.androidx.compose.composeBom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Compose Navigation
    implementation(libs.androidx.navigation.navigationCompose)
    implementation(libs.androidx.hilt.hiltNavigationCompose)

    // Google Accompanist
    implementation(libs.accompanist.systemUiController)

    // Google Android
    implementation(libs.google.android.material)
    implementation(libs.google.android.play.core)

    // Google Firebase
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

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
    kapt(libs.hilt.android.compiler)

    // Okhttp
    implementation(platform(libs.okhttp.bom))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

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

    // Mockito
    testImplementation(libs.mockito.mockitoCore)
    testImplementation(libs.mockito.mockitoInline)

    // Instrumentation
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

fun readProperties(propertiesFile: File) = Properties().apply {
    propertiesFile.inputStream().use { fis ->
        load(fis)
    }
}