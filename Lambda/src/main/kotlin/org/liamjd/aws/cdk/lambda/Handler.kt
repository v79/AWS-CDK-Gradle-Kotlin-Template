package org.liamjd.aws.cdk.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream

/**
 * This absolutely minimal function won't return a valid response, but it will log to CloudWatch
 */
class Handler : RequestStreamHandler {

	override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {

		val logger = context.logger

		try {
			logger.log("This is lambda function handler responding\n")
		} finally {

		}
	}
}




