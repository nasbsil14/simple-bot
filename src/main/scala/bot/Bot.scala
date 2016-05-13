package bot

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpEntity._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.unmarshalling.Unmarshal
import bot.data_format.{SlackWebhookRequest, Converters}
import com.typesafe.config.{Config, ConfigFactory}
import bot.data_format.json._
import spray.json._

import scala.concurrent.{ExecutionContextExecutor, Future}

trait BotService extends Converters {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  implicit def executor: ExecutionContextExecutor

  def config: Config
  val logger: LoggingAdapter

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]]
  val bindingFuture: Future[Http.ServerBinding]

  lazy val http = Http(system)
  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
      println("request")
      val extRequest = NTTAPIRequest("hello", "", "", "", "", "", "", "", "", "", "", "", "", "")
      http.singleRequest(HttpRequest(method = POST
      , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
      , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint)))
    }
    case request@HttpRequest(POST, Uri.Path("/"), _, _, _) => {
      println("request:" + request)
      val botReq = Unmarshal(request.entity).to[SlackWebhookRequest]
      for {
        botData <- botReq
        apiRes <- {
          println(botData.text)
          val extRequest = NTTAPIRequest(botData.text, "", "", "", "", "", "", "", "", "", "", "", "", "")
          http.singleRequest(HttpRequest(method = POST
            , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
            , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint)))
        }
        resData <- Unmarshal(apiRes).to[NTTAPIResponse]
      } yield {
        HttpResponse(200, entity = HttpEntity(ContentType(MediaTypes.`application/json`), SlackWebhookResponse(resData.utt).toJson.prettyPrint))
      }
    }
    case r@HttpRequest(POST, Uri.Path("/msg"), _, _, _) => {
      println(r.entity)
      val botReq = Unmarshal(r).to[BotRequest]
      botReq.flatMap { req =>
        val extRequest = ExtRequest(req.msg, "", "", "", "", "", "", "", "", "", "", "", "", "")
        http.singleRequest(HttpRequest(method = POST
          , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
          , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint)))
      }
    }
    case _: HttpRequest => {
      Future(HttpResponse(404, entity = "Not Found."))
    }
  }

  val connectionHandler: Sink[Http.IncomingConnection, _] = Sink.foreach { connection: Http.IncomingConnection =>
    println("Accepted new connection from " + connection.remoteAddress)

    connection.handleWith {
      //TODO mapAsyncでFuture返す関数使えるようにする。parallelismはひとまず1
      Flow[HttpRequest].mapAsync(1)(requestHandler)
    }
  }
}

object Bot extends App with BotService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port"))

  override val bindingFuture: Future[Http.ServerBinding] = serverSource.to(connectionHandler).run()

  println("Server online at http://127.0.0.1:9000/\nPress RETURN to stop...")
  scala.io.StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind()) // まずポートのバインドを解除
    .onComplete(_ => system.terminate()) // 次にアクターシステムを終了
}