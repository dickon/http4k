package cookbook

import org.reekwest.http.contract.ReportRouteLatency
import org.reekwest.http.contract.Root
import org.reekwest.http.contract.Route
import org.reekwest.http.contract.RouteModule
import org.reekwest.http.contract.SimpleJson
import org.reekwest.http.core.ContentType.Companion.TEXT_PLAIN
import org.reekwest.http.core.HttpHandler
import org.reekwest.http.core.Method.GET
import org.reekwest.http.core.Response
import org.reekwest.http.core.Status.Companion.OK
import org.reekwest.http.core.with
import org.reekwest.http.filters.ResponseFilters
import org.reekwest.http.formats.Argo
import org.reekwest.http.lens.Body
import org.reekwest.http.lens.Path
import org.reekwest.http.lens.int
import org.reekwest.http.server.asJettyServer
import java.time.Clock


fun main(args: Array<String>) {

    fun add(value1: Int, value2: Int): HttpHandler = {
        Response(OK).with(
            Body.string(TEXT_PLAIN).required() to (value1 + value2).toString()
        )
    }

    fun echo(name: String, age: Int): HttpHandler = {
        Response(OK).with(
            Body.string(TEXT_PLAIN).required() to "hello $name you are $age"
        )
    }

    val handler = RouteModule(Root / "foo", SimpleJson(Argo), ResponseFilters.ReportRouteLatency(Clock.systemUTC(), {
        name, latency ->
        println(name + " took " + latency)
    }))
//        .securedBy(ApiKey(Query.int().required("api"), { it == 42 }))
        .withRoute(Route("add").at(GET) / "add" / Path.int().of("value1") / Path.int().of("value2") bind ::add)
        .withRoute(Route("echo").at(GET) / "echo" / Path.of("name") / Path.int().of("age") bind ::echo)
        .toHttpHandler()

    handler.asJettyServer(8000).start().block()
}