package uzcript.configuration
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

object Configuration {
  final case class MongoConfig(uri: String,
                               userCollection: String,
                               database: String,
                               user: String,
                               password: String)
  val live: ULayer[Configuration] = ZLayer.fromEffectMany(
    ZIO
      .effect(ConfigSource.default.loadOrThrow[MongoConfig])
      .map(Has(_))
      .orDie
  )
}
