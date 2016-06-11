package bot.services

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import bot.BotService
import bot.external.io.NTTAPICoordination
import bot.slack.io.SlackConverter
import bot.slack.io.json.{SlackWebhookRequest, SlackWebhookResponse}
import spray.json._

trait SlackBotService extends BotService with SlackConverter with NTTAPICoordination {

  override val routes = {
    logRequestResult("Slack Bot Service") {
      pathSingleSlash {
        (post & entity(as[SlackWebhookRequest])) { req =>
          complete {
            externalApiConnect(req.text).map[ToResponseMarshallable] {
              case Right(extResponse) => HttpEntity(ContentType(MediaTypes.`application/json`), SlackWebhookResponse(extResponse.utt).toJson.prettyPrint)
              case Left(errorMessage) => BadRequest -> errorMessage
            }
          }
        }
      }
    }
  }

}
