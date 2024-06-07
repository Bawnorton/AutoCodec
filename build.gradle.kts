plugins {
    id("java")
    id("maven-publish")
}


group = "com.bawnorton.autocodec"
version = "0.1.0"

var exports = listOf(
    "jdk.compiler/com.sun.tools.javac.code",
    "jdk.compiler/com.sun.tools.javac.api",
    "jdk.compiler/com.sun.tools.javac.util",
    "jdk.compiler/com.sun.tools.javac.file",
    "jdk.compiler/com.sun.tools.javac.processing",
    "jdk.compiler/com.sun.tools.javac.tree",
    "jdk.compiler/com.sun.tools.javac.parser",
)
var exportArgs = exports.flatMap { listOf("--add-exports", "$it=ALL-UNNAMED") }

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    implementation("com.mojang:datafixerupper:8.0.16")

    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.23.1")

    implementation("com.google.auto.service:auto-service:1.0.1")
    annotationProcessor("com.google.auto.service:auto-service:1.0.1")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("com.google.testing.compile:compile-testing:0.21.0")
    testImplementation("org.ow2.asm:asm:9.1")
    testImplementation("org.ow2.asm:asm-tree:9.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }

    jvmArgs(exportArgs)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(exportArgs)
    options.annotationProcessorPath = configurations["annotationProcessor"]
}

tasks.withType<Jar> {
    archiveBaseName.set("autocodec")
}

extensions.configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "autocodec"

            from(components["java"])
        }
    }
}

