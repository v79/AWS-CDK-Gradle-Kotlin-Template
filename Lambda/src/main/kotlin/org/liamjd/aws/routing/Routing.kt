package org.liamjd.aws.routing

import org.liamjd.aws.annotations.CantileverRouter

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

@CantileverRouter
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

		return resolveRoute(RoutePath.parse(requestPath), matchingMethods)
	}

	private fun resolveRoute(routePath: RoutePath, possibleRoutes: List<KRoute>): ResolvedKRoute? {
		val routeComponents = routePath.components.map {
			if (it is RoutePath.Component.Fixed) {
				return@map it
			} else {
				return null
			}
		}
		println("-- possibleRoutes: $possibleRoutes")
		val matchedRoutes = possibleRoutes
			.filter { it.routePath.components.size == routeComponents.size }
			.filter { candidate ->
				candidate.routePath.components.forEachIndexed { index, component ->
					println("-- checking component '$component' against ${routeComponents[index].string}")
					when (component) {
						is RoutePath.Component.Fixed -> {
							val matching = component.string.equals(
								routeComponents[index].string,
								true
							)
							if (!matching) {
								return@filter false
							}
							println("-- matched component $component")
							return@forEachIndexed
						}
						is RoutePath.Component.Parameter -> {
							// in theory, I could add some type checking here?
							// i.e. /users/123 is valid, but /users/gribble is invalid IF we add type information into RoutePath.Component.Parameter
							// until then, nothing to do here
						}
					}
				}
				return@filter true
			}
		println("Matched routes: $matchedRoutes")
		val match = matchedRoutes.firstOrNull() ?: return null
		val pathParamMap = mutableMapOf<String, String>()
		match.routePath.components.forEachIndexed { index, component ->
			if (component is RoutePath.Component.Parameter) {
				pathParamMap += component.name to routeComponents[index].string
			}
		}
		return ResolvedKRoute(match, pathParamMap)
	}

	fun get(path: String, handler: RouteHandler) {
		add(KRoute(APIGatewayMethod.GET, handler, RoutePath.parse(path)))
	}

	fun put(path: String, handler: RouteHandler) {
		add(KRoute(APIGatewayMethod.PUT, handler, RoutePath.parse(path)))
	}

	fun post(path: String, handler: RouteHandler) {
		add(KRoute(APIGatewayMethod.POST, handler, RoutePath.parse(path)))
	}

	fun path(path: String, children: () -> Unit) {
//		pathing[path] = children
	}

	fun listRoutes() {
		println("========== REGISTERED ROUTES ==========")
		routeMap.forEach { (t, u) ->
			println(t.name)
			u.forEach { kr ->
				print("\t${kr.routePath.path} [")
				kr.routePath.components.forEach {
					print("/$it")
				}
				print("]")
				println()
			}

		}
	}

	suspend fun respond(message: AWSAPIGatewayInput): RouteResponse {
		var response: RouteResponse = RouteResponse(400)
		if (supportedMethods.contains(message.method)) {
			val resolvedKRoute = route(message)
			if (resolvedKRoute == null) {
				response = RouteResponse(404)
			}
			resolvedKRoute?.let { route ->
				val routeCtx = RouteContext(message, route.paramMap)
				response = route.route.handler.handle(routeCtx)
			}
		}
		return response
	}
}

// TODO
data class RouteContext(val request: AWSAPIGatewayInput, val pathParameterMap: Map<String, String>) {
	/**
	 * Still to add:
	 * - queryParameters
	 * - utility methods to ease things
	 * - error handling
	 * - body/json handling?
	 */

}

fun RouteContext.respond(lambda: (RouteResponse.ResponseBuilder).() -> Unit): RouteResponse {
	val builder = RouteResponse.ResponseBuilder()
	lambda(builder)
	return builder.build()
}

data class RouteResponse(val status: Int) {

	class ResponseBuilder {

		private var statusCode: Int = 0

		/**
		 * Still to do:
		 * - headers
		 * - utility functions
		 * - body/entity/json
		 * - no content response stuff
		 */

		fun status(statusCode: Int): ResponseBuilder {
			this.statusCode = statusCode
			return this
		}

		fun build(): RouteResponse {
			val buildStatus = this.statusCode
			return (RouteResponse(buildStatus))
		}
	}
}

data class KRoute(val method: APIGatewayMethod, val handler: RouteHandler, internal val routePath: RoutePath) {
	override fun toString(): String {
		return "${method.name} ${routePath.path}"
	}
}

data class ResolvedKRoute(val route: KRoute, val paramMap: Map<String, String>)

data class RoutePath(internal val components: List<Component>) {

	val path: String

	init {
		val allComponents = components.joinToString("/") {
			when (it) {
				is Component.Fixed -> it.string
				is Component.Parameter -> "{${it.name}}"
			}

		}
		path = "/$allComponents"
	}

	companion object {
		fun parse(pathString: String): RoutePath {
			return PathBuilder().addComponents(pathString).build()
		}
	}

	sealed class Component() {
		data class Fixed(val string: String) : Component() {
			override fun toString() = "Fixed($string)"
		}

		data class Parameter(val name: String) : Component() {
			override fun toString() = "Parameter($name)"
		}
	}

	class PathBuilder {
		private var components = mutableListOf<Component>()

		fun addComponents(string: String): PathBuilder {
			components += string
				.split("/")
				.mapNotNull {
					val trimmedString = it
						.removePrefix("/")
						.removeSuffix("/")
					if (trimmedString.isEmpty()) {
						return@mapNotNull null
					}
					if (trimmedString.startsWith('{') && trimmedString.endsWith('}')) {
						Component.Parameter(trimmedString.removePrefix("{").removeSuffix("}"))
					} else {
						Component.Fixed(trimmedString)
					}
				}
			return this
		}

		fun build(): RoutePath = RoutePath(components)
	}
}

fun interface RouteHandler : RouteHandling<RouteContext, RouteResponse>

fun interface RouteHandling<ContextType, ResponseType> {
	suspend fun handle(
		context: ContextType
	): ResponseType
}
