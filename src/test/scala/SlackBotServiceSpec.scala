import akka.event.NoLogging
import akka.http.scaladsl.model.{MediaTypes, ContentType, HttpEntity}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import core.slack.io.json.{SlackWebhookRequest, SlackWebhookResponse}
import experiment.BotService
import org.scalatest._
import slack.io.service.SlackBotService
import spray.json._
import akka.http.scaladsl.model._


class SlackBotServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with SlackBotService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  implicit val testRequestFormatter = jsonFormat11(SlackWebhookRequest)

  "Service" should "return OK response" in {
    val request: SlackWebhookRequest = SlackWebhookRequest("","","","","","","","","","","")
    Post("/",  HttpEntity(ContentType(MediaTypes.`application/json`), request.toJson.prettyPrint)) -> routes -> check {
      status shouldBe OK
    }
  }

  it should "return API message" in {
    val request: SlackWebhookRequest = SlackWebhookRequest("","","","","","","","","","","")
    val response: SlackWebhookResponse = SlackWebhookResponse("")
    Post("/",  HttpEntity(ContentType(MediaTypes.`application/json`), request.toJson.prettyPrint)) -> routes -> check {
      status shouldBe OK
      contentType shouldBe MediaTypes.`application/json`
      responseAs[SlackWebhookResponse] shouldBe response
    }
  }
}
