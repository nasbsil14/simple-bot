package core.typetalk.io.json

case class PostEntyity(
                        id: String,
                        topicId: String,
                        replyTo: String,
                        message: String,
                        account: AccountEntity,
                        mention: String,
                        attachments: String,
                        likes: String,
                        talks: String,
                        links: String,
                        createdAt: String,
                        updatedAt: String
                      )