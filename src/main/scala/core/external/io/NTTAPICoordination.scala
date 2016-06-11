package core.external.io

import java.io.IOException

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import core.external.io.json.{NTTAPIRequest, NTTAPIResponse}
import com.typesafe.config.Config
import spray.json._

import scala.concurrent.{ExecutionContextExecutor, Future}

trait NTTAPICoordination extends NTTAPIConverter {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  implicit def executor: ExecutionContextExecutor

  def config: Config

  val logger: LoggingAdapter

  lazy val externalApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("external_api.uri"))

  //外部連携stream
  def externalApiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(externalApiConnectionFlow).runWith(Sink.head)

  //主処理
  def externalApiConnect(msg: String): Future[Either[String, NTTAPIResponse]] = {
    //外部連携
    val request = NTTAPIRequest(msg, "", "", "", "", "", "", "", "", "", "", "", "", "")
    externalApiRequest(RequestBuilding.Post(s"/?${config.getString("external_api.apikey")}", HttpEntity(ContentType(MediaTypes.`application/json`), request.toJson.prettyPrint))).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[NTTAPIResponse].map(Right(_))
        case BadRequest => Future.successful(Left(s"extra connect failure"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }
}
