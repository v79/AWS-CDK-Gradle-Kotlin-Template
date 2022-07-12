package org.liamjd.aws.cdk.lambda

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.Runtime
import software.constructs.Construct

class HelloKotlinLambdaStack(scope: Construct, id: String, props: StackProps?) : Stack(scope, id, props) {

	constructor(scope: Construct, id: String) : this(scope, id, null)

	init {
		Tags.of(this).add("experiment", "cdk-kotlin")
		Function.Builder.create(this, "LambdaFunctionFromKotlinCDK")
			.description("Minimal lambda function")
			.runtime(Runtime.JAVA_11)
//			.code(Code.fromAsset())
			.build()
	}
}
