import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.android.gms.oss-licenses-plugin")
}

val amplitudeApiKey = "AMPLITUDE_API_KEY"
val apikeyProperties = readProperties(file("../apikey.properties"))

android {
    namespace = "ru.vitaliy.belyaev.wishapp"
    compileSdk = 33

    defaultConfig {
        applicationId = "ru.vitaliy.belyaev.wishapp"
        minSdk = 23
        targetSdk = 33
        versionCode = 21
        versionName = "1.7.0"

        buildConfigField("String", amplitudeApiKey, apikeyProperties[amplitudeApiKey] as String)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        kotlinCompilerExtensionVersion = "1.4.8"
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
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation(project(":shared"))

    // Android X common
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.7.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Google Accompanist
    implementation("com.google.accompanist:accompanist-flowlayout:${libs.versions.accompanist.get()}")
    implementation("com.google.accompanist:accompanist-navigation-animation:${libs.versions.accompanist.get()}")
    implementation("com.google.accompanist:accompanist-insets:${libs.versions.accompanist.get()}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${libs.versions.accompanist.get()}")

    // Google Android
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")
    implementation("com.google.android.play:core:1.10.3")

    // Google Firebase
    implementation(platform("com.google.firebase:firebase-bom:31.1.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    // GMS
    implementation("com.google.android.gms:play-services-auth:20.6.0")

//    // Guava
//    implementation("com.google.guava:guava:24.1-jre")
//    // Guava fix
//    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")


    // Google Drive
    implementation("com.google.api-client:google-api-client-android:2.2.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0") {
        exclude(group = "org.apache.httpcomponents")
    }

    // Amplitude
    implementation("com.amplitude:analytics-android:1.10.2")

    // DI
    implementation("io.insert-koin:koin-androidx-compose:${libs.versions.koinCompose.get()}")
    implementation("com.google.dagger:hilt-android:${libs.versions.hilt.get()}")
    kapt("com.google.dagger:hilt-android-compiler:${libs.versions.hilt.get()}")

    // Okhttp
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.1"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // Jsoup
    implementation("org.jsoup:jsoup:1.15.2")

    // Coil
    implementation("io.coil-kt:coil-compose:2.2.2")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.11")

    // Shimmer animation for loading
    implementation("com.valentinilk.shimmer:compose-shimmer:1.0.1")

    // JUnit
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest-junit:2.0.0.0")

    // Mockito
    testImplementation("org.mockito:mockito-core:3.11.2")
    testImplementation("org.mockito:mockito-inline:3.11.2")

    // Instrumentation
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

fun readProperties(propertiesFile: File) = Properties().apply {
    propertiesFile.inputStream().use { fis ->
        load(fis)
    }
}