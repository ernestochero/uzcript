package uzcript
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.bson.codecs.configuration.CodecRegistries.{
  fromProviders,
  fromRegistries
}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.bson.codecs.Macros
import uzcript.configuration.Configuration.MongoConfig
import uzcript.shared.User
import zio.{Has, RIO, Task, URLayer, ZIO, ZLayer}

package object mongo {
  type MongoService = Has[MongoService.Resource]
  type UserRepository = Has[UserRepository.Service]

  def getUserByAddress(
    address: String
  ): ZIO[UserRepository, Throwable, Option[User]] =
    RIO.accessM(_.get.getUserByAddress(address))

  object MongoService {
    lazy val userCodecProvider: CodecProvider =
      Macros.createCodecProvider[User]()
    lazy val codecRegistry: CodecRegistry =
      fromRegistries(fromProviders(userCodecProvider), DEFAULT_CODEC_REGISTRY)
    trait Resource {
      def userMongoCollection: MongoCollection[User]
    }
    val live: URLayer[Has[MongoConfig], MongoService] =
      ZLayer.fromService { mongoConfig =>
        val mongoClient = MongoClient(mongoConfig.uri)
        val database = mongoClient
          .getDatabase(mongoConfig.database)
          .withCodecRegistry(codecRegistry)
        new Resource {
          override def userMongoCollection: MongoCollection[User] =
            database.getCollection(mongoConfig.userCollection)
        }
      }
  }
}
