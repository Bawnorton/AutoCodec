plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.auto.service:auto-service:1.0.1")
    annotationProcessor("com.google.auto.service:auto-service:1.0.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    options.compilerArgs.addAll(listOf(
        "--add-modules", "jdk.compiler",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED"
    ))

    options.annotationProcessorPath = configurations["annotationProcessor"]
}

