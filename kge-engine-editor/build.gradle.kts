plugins {
    id("java")
    kotlin("jvm") version "2.2.20"
}

group = "kge"
val kgeEditor: String by rootProject.extra
version = kgeEditor

dependencies {
    implementation(project(":kge-api"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}