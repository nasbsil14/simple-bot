package bot.services

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import bot.BotService
import bot.external.io.NTTAPICoordination
import bot.typetalk.io.TypetalkConverter
import bot.typetalk.io.json.{TypetalkWebhookRequest, TypetalkWebhookResponse}
import spray.json._

trait TypetalkBotService extends BotService with TypetalkConverter with NTTAPICoordination {

  override val routes = {
    logRequestResult("Typetalk Bot Service") {
      pathSingleSlash {
        (post & entity(as[TypetalkWebhookRequest])) { req =>
          complete {
            externalApiConnect(req.post.message).map[ToResponseMarshallable] {
              case Right(extResponse) => HttpEntity(ContentType(MediaTypes.`application/json`), TypetalkWebhookResponse(extResponse.utt, "").toJson.prettyPrint)
              case Left(errorMessage) => BadRequest -> errorMessage
            }
          }
        }
      }
    }
  }

}
