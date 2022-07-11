package org.liamjd.aws.cdk

import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.s3.Bucket
import software.constructs.Construct

class HelloKotlinCdkStack(scope: Construct, id: String, props: StackProps?) : Stack(scope, id, props) {

	constructor(scope: Construct, id: String) : this(scope, id, null)

	init {
		Tags.of(this).add("experiment", "cdk-kotlin")
		Bucket.Builder.create(this, "MyFirstKotlinCDKBucket")
			.versioned(false)
			.removalPolicy(RemovalPolicy.DESTROY)
			.autoDeleteObjects(true)
			.build()
	}
}
