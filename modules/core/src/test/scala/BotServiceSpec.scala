import akka.event.NoLogging
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import core.BotService
import org.scalatest._

class BotServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with BotService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  "Service" should "return OK response" in {
    Get("/") ~> routes ~> check {
      status shouldBe OK
    }
  }
}
