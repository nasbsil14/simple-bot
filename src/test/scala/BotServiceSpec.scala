import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.{Flow, Sink}
import bot.BotService
import org.scalatest._

import scalaz.concurrent.Future

class BotServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with BotService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

//  "Service" should "return OK response" in {
//    Get("/") -> requestHandler -> check {
//      status shouldBe OK
//    }
//  }

  it should "return OK response" in {
    Get("/") -> requestHandler -> check {
      status shouldBe OK
    }
  }
}
