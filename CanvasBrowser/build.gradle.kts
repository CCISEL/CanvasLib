plugins {
    id("org.jetbrains.kotlin.js") version "1.4.10"
}

group = "pt.isel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("pt.isel:CanvasLib-js:1.0.0")
}

kotlin {
    js {
        browser {
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
}