package bot.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val botRequestFormatter = jsonFormat1(BotRequest)
  implicit val botResponseFormatter = jsonFormat1(BotResponse)

  implicit val extRequestFormatter = jsonFormat14(ExtRequest)
  implicit val extResponseFormatter = jsonFormat1(ExtResponse)

  implicit val NTTAPIRequestFormatter = jsonFormat14(NTTAPIRequest)
  implicit val NTTAPIResponseFormatter = jsonFormat5(NTTAPIResponse)

  //implicit val SlackWebhookRequestFormatter = jsonFormat11(SlackWebhookRequest)
  implicit val SlackWebhookResponseFormatter = jsonFormat1(SlackWebhookResponse)
}
