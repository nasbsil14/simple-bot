package experiment

object Main extends App {
  val server: Server2 = new Server2
  server.service()

  //    val source = """{"msg": "json source"}"""
  //    val jsonAst = source.parseJson
  //    println(jsonAst.prettyPrint)
  //    println(jsonAst.compactPrint)
}
