package org.liamjd.aws.routing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class Cantilever {
	val router: KRouter = KRouter()

	init {
		router.get("/") {
			println("I've found the get / method")
			RouteResponse(200)
		}

		router.get("/a/longer/path") {
			println("I can match a longer path")
			RouteResponse(200)
		}

		router.put("/") {
			println("I can respond to a PUT request")
			RouteResponse(201)
		}
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
		val fakeRoutePath = RoutePath("/")

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
		val fakeRoutePath = RoutePath("/")

		router.add(KRoute(APIGatewayMethod.GET, handler = fakeHandler, fakeRoutePath))

		val response = router.route(emptyGetRequest)
		assertNotNull(response)
	}

	// Now we start testing the real application, haha

	@Test
	fun `cantilever get request returns status 200`() = runTest {
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
	fun `returns 404 status when method isn't supported`() = runTest {
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
	fun `can respond to a PUT request`() = runTest {
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

	// Next test - returning a body in the response
}
