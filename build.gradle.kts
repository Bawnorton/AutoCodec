plugins {
    id("java")
}

val Project.moduleName get() = if (parent == null) "common" else name

allprojects {
    apply(plugin = "java")

    group = "com.bawnorton.autocodec"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net")
    }

    dependencies {
        compileOnly("com.mojang:datafixerupper:8.0.16")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    tasks.withType<Jar> {
        archiveBaseName.set("autocodec-$moduleName")
    }
}

