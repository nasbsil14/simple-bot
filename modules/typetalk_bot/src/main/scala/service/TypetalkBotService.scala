package typetalk.io.service

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
import spray.json._
import typetalk_bot.io.TypetalkConverter
import typetalk_bot.io.json.{TypetalkWebhookRequest, TypetalkWebhookResponse}

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

object TypetalkBot extends App with TypetalkBotService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}