plugins {
    id("java")
    kotlin("jvm") version "2.2.20"
}

group = "com.craftware"
version = "unspecified"

val lwjglVersion: String by rootProject.extra
val jomlVersion: String by rootProject.extra
val imguiVersion: String by rootProject.extra

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.joml:joml:$jomlVersion")

    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")

    runtimeOnly("org.lwjgl:lwjgl:${lwjglVersion}:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw:${lwjglVersion}:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:${lwjglVersion}:natives-windows")

    implementation("io.github.spair:imgui-java-binding:$imguiVersion")
    implementation("io.github.spair:imgui-java-lwjgl3:$imguiVersion")
    runtimeOnly("io.github.spair:imgui-java-natives-windows:$imguiVersion")
}

tasks.test {
    useJUnitPlatform()
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(24)
}