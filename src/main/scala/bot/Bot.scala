package bot

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpEntity._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import bot.services.SlackBotService
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}

trait BotService {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  implicit def executor: ExecutionContextExecutor

  def config: Config
  val logger: LoggingAdapter

//  lazy val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
//    Http().outgoingConnection(config.getString("services.ip-api.host"), config.getInt("services.ip-api.port"))
//
//  //外部連携stream
//  def extApiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(ipApiConnectionFlow).runWith(Sink.head)
//
//  //主処理
//  def fetchIpInfo(ip: String): Future[Either[String, IpInfo]] = {
//    //外部連携
//    ipApiRequest(RequestBuilding.Get(s"/json/$ip")).flatMap { response =>
//      response.status match {
//        case OK => Unmarshal(response.entity).to[IpInfo].map(Right(_))
//        case BadRequest => Future.successful(Left(s"$ip: incorrect IP format"))
//        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
//          val error = s"FreeGeoIP request failed with status code ${response.status} and entity $entity"
//          logger.error(error)
//          Future.failed(new IOException(error))
//        }
//      }
//    }
//  }

  val routes = {
    logRequestResult("service") {
      pathSingleSlash {
        complete("ok")
      }
    }
  }
////  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]]
////  val bindingFuture: Future[Http.ServerBinding]
//
////  lazy val http = Http(system)
//  val requestHandler: HttpRequest => HttpResponse = {
//    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
//      HttpResponse(200, entity = "OK")
//    }
//    case _: HttpRequest => {
//      HttpResponse(404, entity = "Not Found.")
//    }
//  }
//
//  val flow = Flow[HttpRequest].map(requestHandler)
//
//  val connectionHandler: Sink[Http.IncomingConnection, _] = Sink.foreach { connection: Http.IncomingConnection =>
//    println("Accepted new connection from " + connection.remoteAddress)
//
//    connection.handleWith {
//      flow
////      //TODO mapAsyncでFuture返す関数使えるようにする。parallelismはひとまず1
////      Flow[HttpRequest].mapAsync(1)(requestHandler)
//    }
//  }

}

object Bot extends App with BotService {self: SlackBotService =>
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

//  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
//    Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port"))
//
//  val bindingFuture: Future[Http.ServerBinding] = serverSource.to(connectionHandler).run()
//
//  println(s"""Server online at http://${config.getString("http.interface")}:${config.getInt("http.port")}/\nPress RETURN to stop...""")
//  scala.io.StdIn.readLine()
//
//  bindingFuture
//    .flatMap(_.unbind())
//    .onComplete(_ => system.terminate())
}