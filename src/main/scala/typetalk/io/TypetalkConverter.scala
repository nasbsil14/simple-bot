package core.typetalk.io

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import core.typetalk.io.json._
import com.typesafe.config.Config
import spray.json._

trait TypetalkConverter extends SprayJsonSupport with DefaultJsonProtocol {
  def config: Config

  implicit val typetalkTopicFormatter = jsonFormat6(TopicEntity)
  implicit val typetalkAccountFormatter = jsonFormat7(AccountEntity)
  implicit val typetalkPostFormatter = jsonFormat12(PostEntyity)

  implicit val typetalkWebhookRquestFormatter = jsonFormat2(TypetalkWebhookRequest)
  implicit val typetalkWebhookResponseFormatter = jsonFormat2(TypetalkWebhookResponse)
}
