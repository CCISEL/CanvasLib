plugins {
    java
    kotlin("jvm") version "1.4.10"
}

group = "pt.isel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("pt.isel:CanvasLib-jvm:1.0.0")
}
