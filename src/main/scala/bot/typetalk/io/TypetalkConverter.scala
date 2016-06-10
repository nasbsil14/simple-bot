package bot.typetalk.io

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.unmarshalling._
import bot.external.io.json.{NTTAPIResponse, _}
import bot.typetalk.io
import bot.typetalk.io.json._
import com.typesafe.config.Config
import spray.json._

import scala.concurrent.{Await, Future, ExecutionContextExecutor}
import scala.concurrent.duration.Duration

import scala.concurrent.ExecutionContext.Implicits.global

trait TypetalkConverter extends SprayJsonSupport with DefaultJsonProtocol {
  def config: Config

  implicit val nttAPIRequestFormatter = jsonFormat14(NTTAPIRequest)
  implicit val nttAPIResponseFormatter = jsonFormat5(NTTAPIResponse)

  implicit val typetalkTopicFormatter = jsonFormat6(TopicEntity)
  implicit val typetalkAccountFormatter = jsonFormat7(AccountEntity)
  implicit val typetalkPostFormatter = jsonFormat12(PostEntyity)

  implicit val typetalkWebhookRquestFormatter = jsonFormat2(TypetalkWebhookRequest)
  implicit val typetalkWebhookResponseFormatter = jsonFormat2(TypetalkWebhookResponse)
}
