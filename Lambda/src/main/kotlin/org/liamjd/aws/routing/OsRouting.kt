package org.liamjd.aws.routing

data class OsRouting(val routes: List<OsRoute>) {

}


data class OsRoute(
	val method: APIGatewayMethod,
	internal val path: String,
	val handler: OsRouteHandler
)


typealias OsRouteHandler = (AWSAPIGatewayInput) -> RouteResponse
