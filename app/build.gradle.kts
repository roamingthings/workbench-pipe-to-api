import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    alias(libs.plugins.micronaut.minimalLibrary)
    alias(libs.plugins.shadow)
}

version = "0.1"
group = "de.roamingthings"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(libs.micronaut.serdeProcessor)

    implementation(libs.bundles.micronaut.lambdaBase)
    implementation(libs.bundles.micronaut.serde)
    implementation(libs.aws.lambdaJavaEvents)
    implementation(platform(libs.log4j2.bom))
    implementation(libs.log4j2.api)

    runtimeOnly(libs.bundles.logging.runtime)

    testImplementation(libs.bundles.snapshotTesting)
    testImplementation(libs.bundles.jackson.base)
    testRuntimeOnly(libs.bundles.jackson.runtime)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

micronaut {
    runtime("lambda_java")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("de.roamingthings.*")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer())
}
