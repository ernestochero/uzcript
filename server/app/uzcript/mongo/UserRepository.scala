package uzcript.mongo
import uzcript.shared.User
import zio.{Task, URLayer, ZLayer}

object UserRepository {
  trait Service {
    def getUserByAddress(address: String): Task[Option[User]]
  }
  val live: URLayer[MongoService, UserRepository] =
    ZLayer.fromService { dbResource =>
      UserCollectionDatabase(dbResource.userMongoCollection)
    }
}
