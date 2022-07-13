package org.liamjd.aws.routing


/**
 * The core of the routing, mapping paths to handler functions
 */
interface Routing {

	val routes: List<KRoute>
	val supportedMethods: List<APIGatewayMethod>
		get() = listOf(APIGatewayMethod.GET, APIGatewayMethod.POST, APIGatewayMethod.PUT)

	/**
	 * Add a new route to the list of valid routes
	 *@param KRoute, comprised of a method, a path, and a handling function
	 */
	fun add(route: KRoute)

	/**
	 * Find the route which best matches the path supplied in [message]
	 * @return the resolved route, or null
	 */
	fun route(message: AWSAPIGatewayInput): ResolvedKRoute?
}

class KRouter : Routing {

	/**
	 * An internal representation of the routes, a map of [HTTPMethod] names to [KRoute] objects
	 */
	private val routeMap = mutableMapOf<APIGatewayMethod, MutableList<KRoute>>()
	override val routes: List<KRoute>
		get() = routeMap.values.flatten()

	override fun add(route: KRoute) {
		val method = route.method
		if (routeMap.containsKey(method)) {
			routeMap[method]?.add(route)
		} else {
			routeMap[method] = mutableListOf(route)
		}
	}

	override fun route(message: AWSAPIGatewayInput): ResolvedKRoute? {
		val requestPath = message.path
		// TODO: needs to be much cleverer!
		val matchingMethods = routeMap[message.method] ?: return null

		return resolveRoute(requestPath, matchingMethods)
	}

	private fun resolveRoute(requestPath: String, possibleRoutes: List<KRoute>): ResolvedKRoute? {
		val matchedRoutes = possibleRoutes
			.filter { it.routePath.wibble == requestPath }
		val match = matchedRoutes.firstOrNull() ?: return null
		return ResolvedKRoute(match)
	}

	fun get(path: String, handler: RouteHandler) {
		add(KRoute(APIGatewayMethod.GET, handler, RoutePath(path)))
	}

	fun put(path: String, handler: RouteHandler) {
		add(KRoute(APIGatewayMethod.PUT, handler, RoutePath(path)))
	}

	fun post(path: String, handler: RouteHandler) {
		add(KRoute(APIGatewayMethod.POST, handler, RoutePath(path)))
	}

	suspend fun respond(message: AWSAPIGatewayInput): RouteResponse {
		return if (supportedMethods.contains(message.method)) {
			val resolvedKRoute = route(message)
			val response = if (resolvedKRoute != null) {
				resolvedKRoute.route.handler.handle(RouteContext(message))
			} else {
				RouteResponse(404)
			}
			response
		} else {
			RouteResponse(400) // unsupported method
		}
	}
}


data class KRoute(val method: APIGatewayMethod, val handler: RouteHandler, internal val routePath: RoutePath)
data class ResolvedKRoute(val route: KRoute)

data class RoutePath(val wibble: String) // TODO - this needs to be parsed from the request
data class RouteContext(val message: AWSAPIGatewayInput) // TODO
data class RouteResponse(val status: Int) // TODO


fun interface RouteHandler : RouteHandling<RouteContext, RouteResponse>

fun interface RouteHandling<ContextType, ResponseType> {
	suspend fun handle(
		context: ContextType
	): ResponseType
}
