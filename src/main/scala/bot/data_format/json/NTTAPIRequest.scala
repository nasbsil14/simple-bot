package bot.data_format.json

final case class NTTAPIRequest(
                             utt: String,
                             context: String,
                             nickname: String,
                             nickname_y: String,
                             sex: String,
                             bloodtype: String,
                             birthdateY: String,
                             birthdateM: String,
                             birthdateD: String,
                             age: String,
                             constellations: String,
                             place: String,
                             mode: String,
                             t: String
                           )
