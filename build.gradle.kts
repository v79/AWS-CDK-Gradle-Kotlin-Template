import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "org.liamjd.aws"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.31.0")
    implementation("software.constructs:constructs:[10.0.0,11.0.0)")
    // testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("org.liamjd.aws.cdk.HelloKotlinCDKAppKt")
}
