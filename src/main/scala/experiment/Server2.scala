package experiment

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._

class Server2 {
  def service() = {
    implicit val system = ActorSystem("mySystem")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    // ルーティング
    val route = get {
      pathSingleSlash {
        complete {
          <html>
            <body>Hello world!</body>
          </html>
        }
      }
    } ~
      path("ok") {
        complete("ok")
      } ~
      path("not_ok") {
        sys.error("not_ok") // 500 エラー
      }

    val bindingFuture = Http().bindAndHandle(route, "127.0.0.1", 8080)

    println("Server online at http://127.0.0.1:8080/\nPress RETURN to stop...")
    scala.io.StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
