plugins {
    id("java")
    kotlin("jvm")
}

group = "kge"
version = "unspecified"

val jomlVersion: String by rootProject.extra
val imguiVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.joml:joml:${jomlVersion}")
    implementation("io.github.spair:imgui-java-binding:${imguiVersion}")
    implementation("io.github.spair:imgui-java-lwjgl3:${imguiVersion}")
    implementation("io.github.spair:imgui-java-natives-windows:${imguiVersion}")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(kotlin("stdlib-jdk8"))
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }

kotlin { jvmToolchain(17) }