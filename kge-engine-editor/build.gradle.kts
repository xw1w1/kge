plugins {
    id("java")
    application
    kotlin("jvm") version "2.2.20"
}

group = "kge"
val kgeEditor: String by rootProject.extra
version = kgeEditor

dependencies {
    implementation(project(":kge-api"))
    implementation("com.google.code.gson:gson:2.13.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}

application {
    mainClass.set("kge.editor.EditorBootstrapKt")
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }

kotlin { jvmToolchain(17) }