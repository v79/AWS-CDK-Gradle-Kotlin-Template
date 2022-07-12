package org.liamjd.aws.cdk

import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.apigateway.LambdaRestApi
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.Runtime
import software.amazon.awscdk.services.s3.Bucket
import software.constructs.Construct

class HelloKotlinCdkStack(scope: Construct, id: String, props: StackProps?) : Stack(scope, id, props) {

	constructor(scope: Construct, id: String) : this(scope, id, null)

	init {
		Tags.of(this).add("experiment", "cdk-kotlin")
		val bucket = Bucket.Builder.create(this, "MyFirstKotlinCDKBucket")
			.versioned(false)
			.removalPolicy(RemovalPolicy.DESTROY)
			.autoDeleteObjects(true)
			.build()

		val lambdaFunction = Function.Builder.create(this, "LambdaFunctionFromKotlinCDK")
			.description("Minimal lambda function")
			.runtime(Runtime.JAVA_11)
			.code(Code.fromAsset("./Lambda/build/libs/lambda.jar"))
			.handler("org.liamjd.aws.cdk.lambda.Handler")
			.build()

		// TODO: This creates some 'ANY' routes by default; how to stop this?
		val api = LambdaRestApi.Builder.create(this, "KotlinCDKAPIGateway")
			.restApiName("KotlinCDKAPIGateway")
			.description("Simple gateway to lambda function ${lambdaFunction.functionName}")
			.handler(lambdaFunction)
			.build()

		val root = api.root.addMethod("GET")
	}
}
