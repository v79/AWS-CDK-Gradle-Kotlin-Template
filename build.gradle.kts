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
	google()
	maven {
		url = uri("https://repo.maven.apache.org/maven2/")
	}
}

dependencies {
	implementation("software.amazon.awscdk:aws-cdk-lib:2.56.1")
	implementation("software.constructs:constructs:10.1.200")

	// reflection
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
	implementation("org.reflections:reflections:0.10.2")
	implementation(project(":annotations"))
	// testing
	testImplementation(kotlin("test"))
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
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
