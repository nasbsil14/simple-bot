package typetalk_bot.io

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import typetalk_bot.io.json._

trait TypetalkConverter extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val typetalkTopicFormatter = jsonFormat6(TopicEntity)
  implicit val typetalkAccountFormatter = jsonFormat7(AccountEntity)
  implicit val typetalkPostFormatter = jsonFormat12(PostEntyity)

  implicit val typetalkWebhookRquestFormatter = jsonFormat2(TypetalkWebhookRequest)
  implicit val typetalkWebhookResponseFormatter = jsonFormat2(TypetalkWebhookResponse)
}
