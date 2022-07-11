package org.liamjd.aws.cdk

import software.amazon.awscdk.App
import software.amazon.awscdk.StackProps

fun main() {
	val app = App()

	val stack = HelloKotlinCdkStack(app, "HelloKotlinCDKStack", StackProps.builder().build())

	app.synth()
}
