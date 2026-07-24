import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.roborazzi)
}

val lexikonVersionName = providers.gradleProperty("lexikon.versionName").orElse("0.1.0").get()
val lexikonVersionCode = providers.gradleProperty("lexikon.versionCode").orElse("1").map { it.toInt() }.get()

val lexikonJpackagePackageVersion = run {
    val parts = lexikonVersionName.split(".")
    val major = parts.firstOrNull()?.toIntOrNull()
    if (major != null && major <= 250 && parts.size == 3) {
        val minor = parts[1].toIntOrNull()
        val patch = parts[2].toIntOrNull()
        if (minor != null && minor <= 255 && patch != null && patch <= 65535) {
            "${major + 1}.$minor.$patch"
        } else {
            "100.0.$lexikonVersionCode"
        }
    } else {
        "100.0.$lexikonVersionCode"
    }
}

group = "com.joetr.lexikon"
version = lexikonVersionName

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm("desktop")
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "lexikon.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).copy(
                    port = 10001,
                    open = providers.gradleProperty("web.openBrowser").map { it.toBoolean() }.orElse(true).get(),
                )
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)
                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)
                implementation(libs.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.coroutines.swing)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.compose.ui.test.junit4)
                implementation(libs.roborazzi.compose.desktop)
            }
        }
    }
}

extensions.configure<WasmNodeJsEnvSpec>("kotlinWasmNodeJsSpec") {
    download.set(false)
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/screenshotTest/roborazzi"))
}

tasks.withType<Test>().configureEach {
    maxParallelForks = 1
    systemProperty("java.awt.headless", "false")
}

compose.desktop {
    application {
        mainClass = "com.joetr.lexikon.desktop.MainKt"
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
        nativeDistributions {
            modules("java.instrument", "java.management", "java.net.http")
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageName = "Lexikon"
            packageVersion = lexikonJpackagePackageVersion
            description = "A daily word puzzle"
            vendor = "joetr"
            macOS {
                bundleID = "com.joetr.lexikon"
                iconFile.set(project.file("src/desktopMain/resources/icons/lexikon.icns"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icons/lexikon.ico"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icons/lexikon.png"))
            }
        }
    }
}

tasks.named<Test>("desktopTest") {
    systemProperty("roborazzi.test.record", System.getProperty("roborazzi.test.record", "false"))
}
