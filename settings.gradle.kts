plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "kotlin-game-engine"

include("engine-core", "engine-editor")

include("kge-api")
include("kge-engine-editor")