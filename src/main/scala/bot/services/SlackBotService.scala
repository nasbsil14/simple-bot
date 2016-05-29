package bot.services

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Flow
import bot.BotService
import bot.external.io.json.{NTTAPIResponse, NTTAPIRequest}
import bot.slack.io.json.SlackWebhookResponse
import bot.slack.io.{SlackWebhookRequest, SlackConverter}
import spray.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, ExecutionContextExecutor}

trait SlackBotService extends BotService with SlackConverter {

  override val requestHandler: HttpRequest => HttpResponse = {
    case request@HttpRequest(POST, Uri.Path("/"), _, _, _) => {
      println("request:" + request)
      val botReq = Await.result(Unmarshal(request.entity).to[SlackWebhookRequest], Duration.Inf)
      val extRequest = NTTAPIRequest(botReq.text, "", "", "", "", "", "", "", "", "", "", "", "", "")
      Await.result(Http().singleRequest(HttpRequest(method = POST
        , uri = s"""${config.getString("external_api.uri")}?${config.getString("external_api.apikey")}"""
        , entity = HttpEntity(ContentType(MediaTypes.`application/json`), extRequest.toJson.prettyPrint))), Duration.Inf)
    }
    case _: HttpRequest => {
      HttpResponse(404, entity = "Not Found.")
    }
  }

  def apiResponseHandler: HttpResponse => HttpResponse = {
    case HttpResponse(StatusCodes.OK, _, entity, _) => {
      val apiRes = Await.result(Unmarshal(entity).to[NTTAPIResponse], Duration.Inf)
      HttpResponse(200, entity = HttpEntity(ContentType(MediaTypes.`application/json`), SlackWebhookResponse(apiRes.utt).toJson.prettyPrint))
    }
    case response@HttpResponse(_) => response
  }

  override val flow = Flow[HttpRequest].map(requestHandler) via Flow[HttpResponse].map(apiResponseHandler)

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
