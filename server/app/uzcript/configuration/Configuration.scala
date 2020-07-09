package uzcript.configuration
import com.typesafe.config.{Config, ConfigFactory}
import zio._
object Configuration {
  lazy val config: Config = ConfigFactory.load()
  final case class MongoConfig(uri: String,
                               userCollection: String,
                               database: String)
  val live: ULayer[Configuration] =
    ZLayer.fromEffectMany {
      ZIO
        .fromOption(loadMongoConfig(config))
        .bimap(_ => new Exception("failed to build MongoConfig"), Has(_))
        .orDie
    }
  def getStringOption(config: Config, key: String): Option[String] =
    if (config.hasPath(key)) Option(config.getString(key)) else None
  def loadMongoConfig(config: Config): Option[MongoConfig] = {
    for {
      uri <- getStringOption(config, "mongo-config.uri")
      userCollection <- getStringOption(config, "mongo-config.user-collection")
      database <- getStringOption(config, "mongo-config.database")
    } yield MongoConfig(uri, userCollection, database)
  }

}
