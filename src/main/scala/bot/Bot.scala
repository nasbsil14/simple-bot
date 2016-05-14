package bot

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpEntity._
import akka.http.scaladsl.model._
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

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]]
  val bindingFuture: Future[Http.ServerBinding]

  lazy val http = Http(system)
  val requestHandler: HttpRequest => Future[HttpResponse]= {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
      Future(HttpResponse(200, entity = "OK"))
    }
    case _: HttpRequest => {
      Future(HttpResponse(404, entity = "Not Found."))
    }
  }

  val connectionHandler: Sink[Http.IncomingConnection, _] = Sink.foreach { connection: Http.IncomingConnection =>
    println("Accepted new connection from " + connection.remoteAddress)

    connection.handleWith {
      //TODO mapAsyncでFuture返す関数使えるようにする。parallelismはひとまず1
      Flow[HttpRequest].mapAsync(1)(requestHandler)
    }
  }
}

object Bot extends App with BotService {self: SlackBotService =>
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port"))

  override val bindingFuture: Future[Http.ServerBinding] = serverSource.to(connectionHandler).run()

  println(s"""Server online at http://${config.getString("http.interface")}:${config.getInt("http.port")}/\nPress RETURN to stop...""")
  scala.io.StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}