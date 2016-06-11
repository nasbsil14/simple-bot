package core.external.io

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.unmarshalling._
import core.external.io.json.{NTTAPIResponse, _}
import core.slack.io.json.{SlackWebhookRequest, SlackWebhookResponse}
import com.typesafe.config.Config
import spray.json._

trait NTTAPIConverter extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val nttAPIRequestFormatter = jsonFormat14(NTTAPIRequest)
  implicit val nttAPIResponseFormatter = jsonFormat5(NTTAPIResponse)
}
