package org.liamjd.aws.routing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class Cantilever {
	val router: KRouter = KRouter()

	init {
		router.apply {
			get("/") { ctx ->
				println("I've found the get / method")
				ctx.respond {
					status(200)
				}
			}

			get("/a/longer/path") {
				println("I can match a longer path")
				RouteResponse(200)
			}

			put("/") {
				println("I can respond to a PUT request")
				RouteResponse(201)
			}

			get("/users/{userId}") {
				println("You've reached route /users/{userId}")
				RouteResponse(200)
			}

			get("/search/{businessName}") {
				println("You were searching for a business")
				RouteResponse(200)
			}

			get("/posts/{pageId}/count") { ctx ->
				println("Return the count for page pageId: ${ctx.pathParameterMap["pageId"]}")

				ctx.respond {
					status(200)
				}
			}

			path("/users/") {
				get("bob") {
					println("This is route GET /users/bob")
					RouteResponse(200)
				}
			}

		}

		router.listRoutes()
	}

	suspend fun respond(message: AWSAPIGatewayInput): RouteResponse {
		return router.respond(message)
	}
}

@OptIn(ExperimentalCoroutinesApi::class)
class RoutingTest {

	private val fakeAPIRequestContext = AWSAPIGatewayInput.RequestContext(
		"accountId",
		"resourceId",
		"stage",
		"requestId",
		1000L,
		AWSAPIGatewayInput.RequestContext.RequestIdentity(),
		path = "/",
		resourcePath = "/",
		httpMethod = APIGatewayMethod.GET,
		apiId = "apiId",
		protocol = "protocol"
	)
	private val emptyGetRequest = AWSAPIGatewayInput(
		inputBody = null,
		method = APIGatewayMethod.GET,
		path = "/",
		inputHeaders = emptyMap(),
		pathParameters = emptyMap(),
		queryStringParameters = emptyMap(),
		requestContext = fakeAPIRequestContext,
		resource = "/",
		stageVariables = emptyMap()
	)

	val fakeHandler = RouteHandler {
		RouteResponse(200)
	}

	@Test
	fun `KRoute class can add a route specified by a path and a method`() {
		val router = KRouter()
		val fakeRoutePath = RoutePath.parse("/")

		router.add(KRoute(APIGatewayMethod.GET, handler = fakeHandler, fakeRoutePath))

		assertNotNull(
			router.routes
		) {
			assertEquals(1, it.size)
		}
	}

	@Test
	fun `Router can invoke the fakeHandler`() {
		val router = KRouter()
		val fakeRoutePath = RoutePath.parse("/")

		router.add(KRoute(APIGatewayMethod.GET, handler = fakeHandler, fakeRoutePath))

		val response = router.route(emptyGetRequest)
		assertNotNull(response)
	}

	// Now we start testing the real application

	@Test
	fun `cantilever get request to root returns status 200`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest)

		assertNotNull(response) {
			assertEquals(200, response.status)
		}
	}

	@Test
	fun `returns 404 status when route not found`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(path = "/invalid-path"))
		assertNotNull(response) {
			assertEquals(404, response.status)
		}
	}

	@Test
	fun `returns 404 status when method isn't supported for a particular route`() = runTest {
		// TODO: should this be a 400 or other such error, instead of 404?
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(method = APIGatewayMethod.POST))
		assertNotNull(response) {
			assertEquals(404, response.status)
		}
	}

	@Test
	fun `can match a longer path`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(path = "/a/longer/path"))
		assertNotNull(response) {
			assertEquals(200, response.status)
		}
	}

	@Test
	fun `can respond to a PUT request with a 201 status`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(method = APIGatewayMethod.PUT))
		assertNotNull(response) {
			assertEquals(201, response.status)
		}
	}

	@Test
	fun `returns a 400 error if method is not supported`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(method = APIGatewayMethod.DELETE))
		assertNotNull(response) {
			assertEquals(400, response.status)
		}
	}

	// returning a body in the response
	// understanding path parameters like GET /users/123

	@Test
	fun `resolves to a path containing a numeric parameter`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(path = "/users/123"))
		assertNotNull(response) {
			assertEquals(200, response.status)
		}
	}

	@Test
	fun `resolves to a path containing a string parameter`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(path = "/search/my-business-name"))
		assertNotNull(response) {
			assertEquals(200, response.status)
		}
	}

	@Test
	fun `resolves to complex parameterised path`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(path = "/posts/23145/count"))
		assertNotNull(response) {
			assertEquals(200, response.status)
		}
	}


	// understanding query parameters like GET /users/userId?=123
	// combining paths, e.g. path("/users/") { get("{id}"), post("new/{name}")} equals GET /users/123 and POST /users/new/bob
	@Disabled
	@Test
	fun `a method nested in a path resolves correctly`() = runTest {
		val cantilever = Cantilever()
		val response = cantilever.respond(emptyGetRequest.copy(path = "/users/bob"))
		assertNotNull(response) {
			assertEquals(200, response.status)
		}
	}
}
