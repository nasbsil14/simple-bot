import core.external.io.NTTAPIConverter
import core.external.io.json.{NTTAPIRequest, NTTAPIResponse}
import org.scalatest._
import spray.json._

class NTTAPIConverterSpec extends FlatSpec with NTTAPIConverter {

  it should "request param convert" in {
    val x = NTTAPIRequest("こんにちは", "10001", "99999", "光", "ヒカリ", "女", "B", "1997", "5", "30", "16", "双子座", "東京", "dialog", "20")
    val data = JsonParser("""{
                 "utt":"こんにちは",
                 "context":"10001",
                 "user":"99999",
                 "nickname":"光",
                 "nickname_y":"ヒカリ",
                 "sex":"女",
                 "bloodtype":"B",
                 "birthdateY":"1997",
                 "birthdateM":"5",
                 "birthdateD":"30",
                 "age":"16",
                 "constellations":"双子座",
                 "place":"東京",
                 "mode":"dialog",
                 "t":"20"
                 }""")

    val result = data.convertTo[NTTAPIRequest]
    assert(result == x)
  }

  it should "response param convert" in {
    val x = NTTAPIResponse("こんにちは光さん", "こんにちはヒカリさん", "dialog", "0", "aaabbbccc111222333 ")
    val data = JsonParser("""{
                  "utt":"こんにちは光さん",
                  "yomi":"こんにちはヒカリさん",
                  "mode":"dialog",
                  "da":"0",
                  "context":"aaabbbccc111222333 "
                  }""")

    val result = data.convertTo[NTTAPIResponse]
    assert(result == x)
  }
}
