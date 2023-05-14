plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.squareup.sqldelight")
    id("com.rickclephas.kmp.nativecoroutines")
    id("com.google.devtools.ksp")
}

kotlin.sourceSets.all {
    // For KMP-NativeCoroutines https://github.com/rickclephas/KMP-NativeCoroutines#kotlin
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

kotlin {
    cocoapods {

        version = "1.0"
        summary = "WishApp"
        homepage = ""
        ios.deploymentTarget = "13.5"

        pod("KMPNativeCoroutinesAsync") {
            version = "~> ${libs.versions.nativeCoroutines.get()}"
        }

        framework {
            // Required properties
            // Framework name configuration. Use this property instead of deprecated 'frameworkName'
            baseName = "WishAppFramework"
            isStatic = true
        }
    }

    android()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinCoroutines.get()}")

                // Date time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinDateTime.get()}")

                // SQLDelight
                implementation("com.squareup.sqldelight:runtime:${libs.versions.sqlDelight.get()}")
                implementation("com.squareup.sqldelight:coroutines-extensions:${libs.versions.sqlDelight.get()}")

                // UUID
                implementation("com.benasher44:uuid:${libs.versions.uuid.get()}")

                // Logging
                implementation("io.github.aakira:napier:${libs.versions.napier.get()}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                // SQLDelight
                implementation("com.squareup.sqldelight:android-driver:${libs.versions.sqlDelight.get()}")
            }
        }
        val androidTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                // SQLDelight
                implementation("com.squareup.sqldelight:native-driver:${libs.versions.sqlDelight.get()}")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "ru.vitaliy.belyaev.wishapp.shared"
    compileSdk = 33
    defaultConfig {
        minSdk = 23
        targetSdk = 33
    }
}

sqldelight {
    database("WishAppDb") {
        packageName = "ru.vitaliy.belyaev.wishapp.shared.data.database"
        dialect = "sqlite:3.24"
        schemaOutputDirectory = file("src/main/sqldelight/databases")
        verifyMigrations = true
    }
}
//
//configurations.configureEach {
//    val wishappSharedAttr = Attribute.of("WishAppSharedAttr", String::class.java)
////    println("confirugration: $this")
////    error("fnejfneunf")
//    print("configurations: $name")
//    if (name.startsWith("metadata")) {
//        return@configureEach
//    }
//    if (name.endsWith("ApiElements") || name.endsWith("RuntimeElements")) {
//        val targetName = name.removeSuffix("ApiElements")
//            .removeSuffix("RuntimeElements")
//            .removeSuffix("CInterop")
//            .replace("Ir", "Legacy")
//        val target = kotlin.targets.getByName(targetName)
//        val engine = target.attributes.getAttribute(wishappSharedAttr)
//        attributes {
//            engine?.let { attribute(wishappSharedAttr, it) }
//        }
//    }
//}

//val wishappSharedAttr = Attribute.of("WishAppSharedAttr", String::class.java)
//
//configurations.named(":shared:podDebugFrameworkIosArm64").configure {
//    attributes {
//        attribute(wishappSharedAttr, "pod-debug")
//    }
//}
//
//configurations.named(":shared:debugFrameworkIosArm64").configure {
//    attributes {
//        attribute(wishappSharedAttr, "debug")
//    }
//}