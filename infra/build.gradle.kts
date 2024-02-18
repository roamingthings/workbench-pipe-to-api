plugins {
    id("application")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    implementation(platform(libs.micronaut.bom))
    implementation(libs.micronaut.starterCdk) {
        exclude(group = "software.amazon.awscdk", module = "aws-cdk-lib")
    }
    implementation(libs.aws.cdkLib)

    compileOnly(libs.lombok)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)

    testCompileOnly(libs.lombok)
}

application {
    mainClass.set("de.roamingthings.pipetoapi.Main")
}

tasks.withType<JavaCompile>() {
    dependsOn(":enrichment:shadowJar")
    dependsOn(":order:shadowJar")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
