package bot.slack.io

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.unmarshalling._
import bot.external.io.json._
import bot.external.io.json.NTTAPIResponse
import bot.slack.io.json.{SlackWebhookResponse, BotResponse, BotRequest}
import com.typesafe.config.Config
import spray.json._

trait SlackConverter extends SprayJsonSupport with DefaultJsonProtocol {
  def config: Config

  implicit val botRequestFormatter = jsonFormat1(BotRequest)
  implicit val botResponseFormatter = jsonFormat1(BotResponse)

  implicit val nttAPIRequestFormatter = jsonFormat14(NTTAPIRequest)
  implicit val nttAPIResponseFormatter = jsonFormat5(NTTAPIResponse)

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
  //implicit val SlackWebhookRequestFormatter = jsonFormat11(SlackWebhookRequest)
  implicit val SlackWebhookResponseFormatter = jsonFormat1(SlackWebhookResponse)
}
