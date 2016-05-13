package experiment

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

import scala.concurrent.ExecutionContext.Implicits.global

class Client {
  def send(): Unit = {
    val system = ActorSystem("mySystem")
    val props = Props[ClientActor]
    val actor = system.actorOf(props, name = "myActor")

    for (i <- 0 until 3) {
      actor ! "hi!"
      Thread.sleep(1000)
    }
//    while (true) {
//      actor ! "hi!"
//      Thread.sleep(1000)
//    }
    system.terminate()
  }
}

class ClientActor extends Actor {

  import akka.pattern.pipe

  // アクター内では明示的に system を渡します
  final implicit val materializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val http = Http(context.system)
  val log = Logging(context.system, this)

  def receive = {
    case s: String => {
      // 非同期処理が完了したら self にメッセージとしてパイプします。
      log.info("received: %s" format s)
      http.singleRequest(HttpRequest(uri = "http://127.0.0.1:8080/re")).pipeTo(self)
      // http.singleRequest(HttpRequest(uri = "http://www.example.com")).pipeTo(self)
      // http.singleRequest(HttpRequest(uri = "https://www.example.com")).pipeTo(self) // https の場合
    }
    case HttpResponse(StatusCodes.OK, headers, entity, _) => {
      log.info("Got response, body: " + entity.toString)
    }
    case HttpResponse(code, _, _, _) => {
      log.info("Request failed, response code: " + code)
    }
    case _ => {
      log.info("unkown")
    }
  }
}