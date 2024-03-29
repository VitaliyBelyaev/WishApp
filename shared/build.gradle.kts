plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.kmpNativeCoroutines)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }
    cocoapods {
        version = "1.0"
        summary = "WishAppSdk"
        homepage = "https://github.com/VitaliyBelyaev/WishApp"
        ios.deploymentTarget = "16.0"

        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {

        all {
            // For KMP-NativeCoroutines https://github.com/rickclephas/KMP-NativeCoroutines#kotlin
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        commonMain.dependencies {
            // Coroutines
            implementation(libs.kotlin.coroutines.core)

            // Date time
            implementation(libs.kotlin.dateTime)

            // SQLDelight
            implementation(libs.sqlDelight.runtime)
            implementation(libs.sqlDelight.extensions.coroutines)

            // UUID
            implementation(libs.kmmUuid)

            // DI
            implementation(libs.koin.core)

            // Logging
            implementation(libs.napier)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            // SQLDelight
            implementation(libs.sqlDelight.driver.android)
        }

        iosMain.dependencies {
            // SQLDelight
            implementation(libs.sqlDelight.driver.native)
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

android {
    namespace = "ru.vitaliy.belyaev.wishapp.shared"
    compileSdk = 33
    defaultConfig {
        minSdk = 23
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("WishAppDb") {
            packageName.set("ru.vitaliy.belyaev.wishapp.shared.data.database")
            dialect("app.cash.sqldelight:sqlite-3-24-dialect:${libs.versions.sqlDelight.get()}")
            schemaOutputDirectory = file("src/commonMain/sqldelight/databases")
            verifyMigrations.set(true)
        }
    }
}