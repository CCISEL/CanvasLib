plugins {
    kotlin("multiplatform") version "1.4.10"
    `maven-publish`
    signing
}

group = "pt.isel"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    js {
        browser {
            
        }
    }
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
        val jsMain by getting
    }
}

publishing {
    publications {
        create<MavenPublication>("CanvasLib") {
            from(components["kotlin"])
        }
    }
    repositories {
        maven {
            name = "Repo"
            url = uri("file://${buildDir}/repo")
        }
    }
}