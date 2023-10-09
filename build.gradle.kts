@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinParcelize).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kapt).apply(false)
    alias(libs.plugins.sqlDelight).apply(false)
    alias(libs.plugins.kotlinCocoapods).apply(false)
    alias(libs.plugins.hilt).apply(false)
    alias(libs.plugins.googleServices).apply(false)
    alias(libs.plugins.firebaseCrashlytics).apply(false)
    alias(libs.plugins.firebasePerf).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.kmpNativeCoroutines).apply(false)
}

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
        maven("https://jitpack.io")
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}