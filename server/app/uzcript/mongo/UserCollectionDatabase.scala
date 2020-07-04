package uzcript.mongo

import org.mongodb.scala.MongoCollection
import uzcript.shared.User
import zio.Task
import org.mongodb.scala.bson.Document
import uzcript.commons.Transformers._
private[mongo] final case class UserCollectionDatabase(
  userCollection: MongoCollection[User]
) extends UserRepository.Service {
  override def getUserByAddress(address: String): Task[Option[User]] =
    userCollection
      .find(Document("address" -> address))
      .toFuture()
      .toTask
      .map(_.headOption)
}
