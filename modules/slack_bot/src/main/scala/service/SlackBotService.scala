package slack_bot.service

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import core.BotService
import core.external.io.NTTAPICoordination
import slack_bot.io.SlackConverter
import slack_bot.io.json.{SlackWebhookRequest, SlackWebhookResponse}
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

object SlackBot extends App with SlackBotService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}