import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.7.10"
	`maven-publish`
	application
}

group = "org.liamjd.aws"
version = "1.1-SNAPSHOT"

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
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.21")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.21")
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

tasks.withType<JavaExec> {
	dependsOn(":Lambda:shadowJar")
}
