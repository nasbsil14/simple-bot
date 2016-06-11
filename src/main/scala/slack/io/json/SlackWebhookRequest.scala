package core.slack.io.json

final case class SlackWebhookRequest(
                                      token: String,
                                      team_id: String,
                                      team_domain: String,
                                      service_id: String,
                                      channel_id: String,
                                      channel_name: String,
                                      timestamp :String,
                                      user_id: String,
                                      user_name: String,
                                      text: String,
                                      trigger_word: String
                                    )
