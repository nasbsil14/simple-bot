package bot

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpEntity._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.unmarshalling.{PredefinedFromEntityUnmarshallers, Unmarshaller, Unmarshal}
import com.typesafe.config.{Config, ConfigFactory}
import bot.json._
import spray.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Await, Future}

trait BotService extends JsonSupport {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  implicit def executor: ExecutionContextExecutor

  def config: Config
  val logger: LoggingAdapter

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]]
  // 非同期にコネクションを確立
  val bindingFuture: Future[Http.ServerBinding]

  lazy val http = Http(system)
  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
      println("request")
      val extRequest = NTTAPIRequest("hello", "", "", "", "", "", "", "", "", "", "", "", "", "")
      http.singleRequest(HttpRequest(method = POST
      , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
      , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint)))
//      val res = http.singleRequest(HttpRequest(
//        method = POST
//        , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
//        , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint)))
//      Await.result(res, Duration.Inf)
//      HttpResponse(entity = "ok")
    }
    case request@HttpRequest(POST, Uri.Path("/"), _, _, _) => {
      import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
      implicit val slackWebHookRequestUnmarshaller: FromEntityUnmarshaller[SlackWebhookRequest] = {
        PredefinedFromEntityUnmarshallers.defaultUrlEncodedFormDataUnmarshaller.map { data: FormData =>
          println(data)
          SlackWebhookRequest(
            data.fields.getOrElse("token","")
            , data.fields.getOrElse("team_id","")
            , data.fields.getOrElse("team_domain","")
            , data.fields.getOrElse("service_id","")
            , data.fields.getOrElse("channel_id","")
            , data.fields.getOrElse("channel_name","")
            , data.fields.getOrElse("timestamp","")
            , data.fields.getOrElse("user_id","")
            , data.fields.getOrElse("user_name","")
            , data.fields.getOrElse("text","").replace(s"""<@${config.getString("slack.bot_id")}>: ""","")
            , data.fields.getOrElse("trigger_word",""))
        }
      }

      println("request:" + request)
      //TODO:POSTデータがtext/planの場合、どうやってunmarshalするの？
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
        println(resData)
        HttpResponse(200, entity = HttpEntity(ContentType(MediaTypes.`application/json`), SlackWebhookResponse(resData.utt).toJson.prettyPrint))
      }
//      botReq.flatMap { bReq =>
//        println("go api")ß
//        val extRequest = NTTAPIRequest(bReq.text, "", "", "", "", "", "", "", "", "", "", "", "", "")
//        val f = http.singleRequest(HttpRequest(method = POST
//          , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
//          , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint)))
//        f.flatMap{ res =>
//          val apiRes = Unmarshal(res).to[NTTAPIResponse]
//          apiRes.flatMap(ar => Future(HttpResponse(200, entity = HttpEntity(ContentType(MediaTypes.`application/json`), ar.toJson.prettyPrint))))
//        }
//      }
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
//      val botReq = Await.result(Unmarshal(r).to[BotRequest], Duration.Inf)
//      println(botReq)
      //        entity(as[ExtRequest]) {para =>
      //          println(s"msg: ${para.msg}")
      //          complete(s"msg: ${para.msg}")
      //        }
//      Future(HttpResponse(entity = "ok"))
    }
//    case HttpRequest(GET, Uri.Path("/not_ok"), _, _, _) => {
//      sys.error("not_ok")
//    }
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

//  // エンターが入力されるまで待つ
//  println("Server online at http://127.0.0.1:8080/\nPress RETURN to stop...")
//  scala.io.StdIn.readLine()
//
//  // エンターが押されてここに処理がきたら、
//  bindingFuture
//    .flatMap(_.unbind()) // まずポートのバインドを解除
//    .onComplete(_ => system.terminate()) // 次にアクターシステムを終了
}

object Bot extends App with BotService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port"))

  // 非同期にコネクションを確立
  override val bindingFuture: Future[Http.ServerBinding] = serverSource.to(connectionHandler).run()

  // エンターが入力されるまで待つ
  println("Server online at http://127.0.0.1:9000/\nPress RETURN to stop...")
  scala.io.StdIn.readLine()

  // エンターが押されてここに処理がきたら、
  bindingFuture
    .flatMap(_.unbind()) // まずポートのバインドを解除
    .onComplete(_ => system.terminate()) // 次にアクターシステムを終了
}