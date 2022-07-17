package org.liamjd.aws.cdk

import org.liamjd.aws.annotations.CantileverRouter
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import software.amazon.awscdk.App
import software.amazon.awscdk.StackProps
import java.io.File
import java.net.URLClassLoader

fun main() {
	val app = App()

	val lambdaFile = File("Lambda/build/libs/lambda.jar")
	val classLoader: URLClassLoader = URLClassLoader(arrayOf(lambdaFile.toURI().toURL()))
	val reflections = Reflections(ConfigurationBuilder().addClassLoaders(classLoader))

	val routers = reflections.getTypesAnnotatedWith(CantileverRouter::class.java)
	println(routers.size)
	routers.forEachIndexed { index, clazz ->
		println("Router #$index : ${clazz.simpleName}")
	}

	val stack = HelloKotlinCdkStack(app, "HelloKotlinCDKStack", StackProps.builder().build())

	app.synth()
}
