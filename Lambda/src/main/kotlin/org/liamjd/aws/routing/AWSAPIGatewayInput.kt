package org.liamjd.aws.routing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.util.*

enum class APIGatewayMethod {
	GET,
	POST,
	PUT,
	PATCH,
	DELETE,
	HEAD,
	OPTIONS
}

// pilfered and modified from Ktor
@Serializable
data class AWSAPIGatewayInput(

	@SerialName("body") val inputBody: String?,
	@SerialName("headers") val inputHeaders: Map<String, String>?,
	@SerialName("httpMethod") val method: APIGatewayMethod,
	val isBase64Encoded: Boolean = false,
	// not going to support these, Ktor doesn't!
//	val multiValueHeaders: Map<String, String>?,
	// not going to support these, Ktor doesn't!
//	val multiValueQueryStringParameters: MultiValueQueryStringParameters?,
	val path: String,
	val pathParameters: Map<String, String>?,
	val queryStringParameters: Map<String, String>?,
	val requestContext: RequestContext,
	val resource: String,
	val stageVariables: Map<String, String>?
) {

	val headers: Map<String, List<String>>?
		get() = inputHeaders?.mapValues { (_, value) -> value.split(",").map { it.trim() } }

	val body: ByteArray?
		get() = inputBody?.let {
			if (isBase64Encoded) {
				Base64.getDecoder().decode(it)
			} else {
				it.toByteArray()
			}
		}

	@Serializable
	data class RequestContext(
		val accountId: String,
		val resourceId: String,
		val stage: String,
		val requestId: String,
		val requestTimeEpoch: Long,
		val identity: RequestIdentity,
		val path: String,
		val resourcePath: String,
		val httpMethod: APIGatewayMethod,
		val apiId: String,
		val protocol: String,
	) {
		/* @Serializable
		 data class Authorizer(
			 @SerialName("claims")
			 val claims: Any? = Any(),
			 @SerialName("scopes")
			 val scopes: Any? = Any()
		 )*/

		@Serializable
		data class RequestIdentity(
			val accessKey: String? = null,
			val accountId: String? = null,
			val caller: String? = null,
			val clientCert: ClientCert = ClientCert(),
			val cognitoAuthenticationProvider: String? = null,
			val cognitoAuthenticationType: String? = null,
			val cognitoIdentityId: String? = null,
			val cognitoIdentityPoolId: String? = null,
			val principalOrgId: String? = null,
			val sourceIp: String = "",
			val user: String? = null,
			val userAgent: String = "",
			val userArn: String? = null,
		) {
			@Serializable
			data class ClientCert(
				val clientCertPem: String = "",
				val issuerDN: String = "",
				val serialNumber: String = "",
				val subjectDN: String = "",
				val validity: Validity = Validity()
			) {
				@Serializable
				data class Validity(
					val notAfter: String = "",
					val notBefore: String = ""
				)
			}
		}
	}

	@Deprecated("Not supported")
	@Serializable
	data class MultiValueQueryStringParameters(val mvQSP: Map<String, List<String>>)
}


enum class MimeType(val mimeText: String, val isBinary: Boolean, val extension: String) {
	PLAIN("text/plain", false, "txt"),
	MARKDOWN("text/markdown", false, "md"),
	HTML("text/html", false, "html"),
	CSS("text/css", false, "css"),

	PNG("image/png", true, "png"),
	APNG("image/apng", true, "apng"),
	GIF("image/gif", true, "gif"),
	SVG("image/svg", true, "svg"),
	JPEG("image/jpeg", true, "jpeg"),
	BMP("image/bmp", true, "bmp"),
	WEBP("image/webp", true, "webp"),
	TTF("font/ttf", true, "ttf"),

	JS("application/javascript", false, "js"),
	JSMAP("application/json", false, "map"),
	JSON("application/json", false, "json"),
	XML("application/xml", false, "xml"),
	ZIP("application/zip", true, "zip"),
	GZIP("application/gzip", true, "gzip");

	companion object {
		fun binary() = values().filter { it.isBinary }.toTypedArray()
		fun forDeclaration(type: String, subtype: String) = values().find { "${type}/${subtype}" == it.mimeText }
		fun forFile(file: File) = values().find { it.extension == file.extension }
	}
}
