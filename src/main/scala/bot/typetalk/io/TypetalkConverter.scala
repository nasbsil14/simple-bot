package bot.typetalk.io

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.unmarshalling._
import bot.external.io.json.{NTTAPIResponse, _}
import bot.typetalk.io
import bot.typetalk.io.json._
import com.typesafe.config.Config
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait TypetalkConverter extends SprayJsonSupport with DefaultJsonProtocol {
  def config: Config

  implicit val nttAPIRequestFormatter = jsonFormat14(NTTAPIRequest)
  implicit val nttAPIResponseFormatter = jsonFormat5(NTTAPIResponse)

  implicit val typetalkTopicUnmarshaller: FromEntityUnmarshaller[TopicEntity] = {
    PredefinedFromEntityUnmarshallers.defaultUrlEncodedFormDataUnmarshaller.map { data: FormData =>
      println(data)
      TopicEntity(
        data.fields.getOrElse("id","")
        , data.fields.getOrElse("name","")
        , data.fields.getOrElse("suggestion","")
        , data.fields.getOrElse("lastPostedAt","")
        , data.fields.getOrElse("createdAt","")
        , data.fields.getOrElse("updatedAt","")
      )
    }
  }

  implicit val typetalkAccountUnmarshaller: FromEntityUnmarshaller[AccountEntity] = {
    PredefinedFromEntityUnmarshallers.defaultUrlEncodedFormDataUnmarshaller.map { data: FormData =>
      println(data)
      AccountEntity(
        data.fields.getOrElse("id","")
        , data.fields.getOrElse("name","")
        , data.fields.getOrElse("fullName","")
        , data.fields.getOrElse("suggestion","")
        , data.fields.getOrElse("imageUrl","")
        , data.fields.getOrElse("createdAt","")
        , data.fields.getOrElse("updatedAt","")
      )
    }
  }

  implicit val typetalkPostUnmarshaller: FromEntityUnmarshaller[PostEntyity] = {
    PredefinedFromEntityUnmarshallers.defaultUrlEncodedFormDataUnmarshaller.map { data: FormData =>
      println(data)
      PostEntyity(
        data.fields.getOrElse("id","")
        , data.fields.getOrElse("topicId","")
        , data.fields.getOrElse("replyTo","")
        , data.fields.getOrElse("message","")
        , Await.result(Unmarshal(data.fields.getOrElse("account", "")).to[AccountEntity], Duration.Inf)
        , data.fields.getOrElse("mention","")
        , data.fields.getOrElse("attachments","")
        , data.fields.getOrElse("likes","")
        , data.fields.getOrElse("talks","")
        , data.fields.getOrElse("links","")
        , data.fields.getOrElse("createdAt","")
        , data.fields.getOrElse("updatedAt","")
      )
    }
  }

  implicit val typetalkWebHookRequestUnmarshaller: FromEntityUnmarshaller[TypetalkWebhookRequest] = {
    PredefinedFromEntityUnmarshallers.defaultUrlEncodedFormDataUnmarshaller.map { data: FormData =>
      println(data)
      TypetalkWebhookRequest(
        Await.result(Unmarshal(data.fields.getOrElse("topic", "")).to[TopicEntity], Duration.Inf)
        , Await.result(Unmarshal(data.fields.getOrElse("post", "")).to[PostEntyity], Duration.Inf)
      )
    }
  }

  implicit val typetalkWebhookResponseFormatter = jsonFormat2(TypetalkWebhookResponse)
}
