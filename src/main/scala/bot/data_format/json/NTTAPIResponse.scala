package bot.data_format.json

final case class NTTAPIResponse(
                                 utt: String,
                                 yomi: String,
                                 mode: String,
                                 da: String,
                                 context: String
                               )
