package core.external.io

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import core.external.io.json.{NTTAPIResponse, _}
import spray.json._

trait NTTAPIConverter extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val nttAPIRequestFormatter = jsonFormat15(NTTAPIRequest)
  implicit val nttAPIResponseFormatter = jsonFormat5(NTTAPIResponse)
}
