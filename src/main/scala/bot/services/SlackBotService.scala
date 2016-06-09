package bot.services

import java.io.IOException

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source, Flow}
import bot.BotService
import bot.external.io.json.{NTTAPIResponse, NTTAPIRequest}
import bot.slack.io.json.{SlackWebhookRequest, SlackWebhookResponse}
import bot.slack.io.SlackConverter
import spray.json._
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, ExecutionContextExecutor}

trait SlackBotService extends BotService with SlackConverter {

  lazy val extApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("external_api.uri"))

  //外部連携stream
  def extApiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(extApiConnectionFlow).runWith(Sink.head)

  //主処理
  def extConnect(msg: String): Future[Either[String, NTTAPIResponse]] = {
    //外部連携
    val extRequest = NTTAPIRequest(msg, "", "", "", "", "", "", "", "", "", "", "", "", "")
    extApiRequest(RequestBuilding.Post(s"/?${config.getString("external_api.apikey")}", HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint))).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[NTTAPIResponse].map(Right(_))
        case BadRequest => Future.successful(Left(s"extra connect failure"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }

  override val routes = {
    logRequestResult("Slack Bot Service") {
      pathSingleSlash {
        (post & entity(as[SlackWebhookRequest])) { req =>
          complete {
            extConnect(req.text).map[ToResponseMarshallable] {
              case Right(extResponse) => HttpEntity(ContentType(MediaTypes.`application/json`), SlackWebhookResponse(extResponse.utt).toJson.prettyPrint)
              case Left(errorMessage) => BadRequest -> errorMessage
            }
          }
        }
      }
    }
  }

  //  override val requestHandler: HttpRequest => HttpResponse = {
//    case request@HttpRequest(POST, Uri.Path("/"), _, _, _) => {
//      println("request:" + request)
//      val botReq = Await.result(Unmarshal(request.entity).to[SlackWebhookRequest], Duration.Inf)
//      val extRequest = NTTAPIRequest(botReq.text, "", "", "", "", "", "", "", "", "", "", "", "", "")
//      Await.result(Http().singleRequest(HttpRequest(method = POST
//        , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
//        , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint))), Duration.Inf)
//    }
//    case _: HttpRequest => {
//      HttpResponse(404, entity = "Not Found.")
//    }
//  }
//
//  def apiResponseHandler: HttpResponse => HttpResponse = {
//    case HttpResponse(StatusCodes.OK, _, entity, _) => {
//      val apiRes = Await.result(Unmarshal(entity).to[NTTAPIResponse], Duration.Inf)
//      HttpResponse(200, entity = HttpEntity(ContentType(MediaTypes.`application/json`), SlackWebhookResponse(apiRes.utt).toJson.prettyPrint))
//    }
//    case response@HttpResponse(_) => response
//  }
//
//  override val flow = Flow[HttpRequest].map(requestHandler) via Flow[HttpResponse].map(apiResponseHandler)

  //  override val requestHandler: HttpRequest => Future[HttpResponse] = {
//    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
//      println("request")
//      val extRequest = NTTAPIRequest("hello", "", "", "", "", "", "", "", "", "", "", "", "", "")
//      Http().singleRequest(HttpRequest(method = POST
//        , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
//        , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint)))
//    }
//    case request@HttpRequest(POST, Uri.Path("/"), _, _, _) => {
//      println("request:" + request)
//      val botReq = Unmarshal(request.entity).to[SlackWebhookRequest]
//      for {
//        botData <- botReq
//        apiRes <- {
//          println(botData.text)
//          val extRequest = NTTAPIRequest(botData.text, "", "", "", "", "", "", "", "", "", "", "", "", "")
//          http.singleRequest(HttpRequest(method = POST
//            , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
//            , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint)))
//        }
//        resData <- Unmarshal(apiRes).to[NTTAPIResponse]
//      } yield {
//        HttpResponse(200, entity = HttpEntity(ContentType(MediaTypes.`application/json`), SlackWebhookResponse(resData.utt).toJson.prettyPrint))
//      }
//    }
//    case _: HttpRequest => {
//      Future(HttpResponse(404, entity = "Not Found."))
//    }
//  }
}
