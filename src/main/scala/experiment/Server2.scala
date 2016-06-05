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
    val route =
    //ルートへのリクエストの場合
      pathSingleSlash {
        complete {
          <html>
            <body>Hello world!</body>
          </html>
        }
      } ~
        //ルート以下test(/test)へのリクエストの場合
        path("test") {
          //getリクエストの場合
          get {
            complete("ok")
          } ~
            //postリクエストの場合
            post {
              complete("post request ok")
            }
        } ~
        //ルート以下para(/para)始まりのパスへのリクエストの場合
        pathPrefix("para") {
          //getパラ有りの場合(/para/123)
          (get & path(Segment)) {
            param => complete(s"get request ok param:$param")
          }
        } ~
        //ルート以下not_ok(/not_ok)の場合
        path("not_ok") {
          sys.error("not_ok") // 500 エラー
        }

    val bindingFuture = Http().bindAndHandle(route, "127.0.0.1", 9000)

    println("Server online at http://127.0.0.1:9000/\nPress RETURN to stop...")
    scala.io.StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
