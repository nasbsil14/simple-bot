package experiment

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future

class Server {
  def service(): Unit = {
    // おまじないを三行
    implicit val system = ActorSystem() // akka-http は akka-stream を用いて実装されています。akka-stream 内にはアクターシステムが存在します。
    implicit val materializer = ActorMaterializer() // 「ストリームを実行(run)するためのシステム」を生成するもの。
    implicit val ec = system.dispatcher

    // HTTP コネクションの発生源 (Source)
    val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
      Http().bind(interface = "127.0.0.1", port = 9000)

    // 下記 `map` で使用される関数
    val requestHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/"), _, req, _) => {
        println("get request:" + req)
        //        val client: Client = new Client
        //        client.send()
        HttpResponse(entity = "ok")
      }
      case HttpRequest(POST, Uri.Path("/"), _, req, _) => {
        println("post request:" + req)
        HttpResponse(entity = "ok")
      }
      case HttpRequest(GET, Uri.Path("/not_ok"), _, _, _) => {
        sys.error("not_ok")
      }
      case HttpRequest(GET, Uri.Path("/re"), _, _, _) => {
        println("client request")
        HttpResponse(entity = "ok")
      }
      case _: HttpRequest => {
        HttpResponse(404, entity = "Not Found.")
      }
    }

    val requestHandler2: HttpResponse => HttpResponse = {req =>
      println("req2")
      req
    }

    val logicFlow: Flow[HttpRequest, HttpResponse, _] = Flow[HttpRequest].map(requestHandler) via Flow[HttpResponse].map(requestHandler2)

    // HTTP コネクションが処理される出口 (Sink)
    val connectionHandler: Sink[Http.IncomingConnection, _] = Sink.foreach { connection: Http.IncomingConnection =>
      println("Accepted new connection from " + connection.remoteAddress)

      // コネクション内の HTTP リクエストが処理される出口 (Flow + Sink = Sink)
      connection.handleWith {
        logicFlow
        //Flow[HttpRequest].map(requestHandler)
      }
    }

    // 非同期にコネクションを確立
    val bindingFuture: Future[Http.ServerBinding] = serverSource.to(connectionHandler).run()

    // エンターが入力されるまで待つ
    println("Server online at http://127.0.0.1:9000/\nPress RETURN to stop...")
    scala.io.StdIn.readLine()

    // エンターが押されてここに処理がきたら、
    bindingFuture
      .flatMap(_.unbind()) // まずポートのバインドを解除
      .onComplete(_ => system.terminate()) // 次にアクターシステムを終了
  }
}
