package uzcript.commons

import uzcript.configuration.Configuration
import uzcript.mongo.{MongoService, UserRepository}
import uzcript.symbol.SymbolService.SymbolService
import zio.{ULayer, ZLayer, ZEnv}

object Environments {
  type HttpContext[A] = ZLayer[ZEnv, Throwable, A]
  type AppContext = SymbolService with UserRepository
  val symbolService: ULayer[SymbolService] = SymbolService.live
  val mongoConfig
    : ULayer[MongoService] = Configuration.live >>> MongoService.live
  val usersRepository
    : ULayer[UserRepository] = mongoConfig >>> UserRepository.live
  val appContext = symbolService ++ usersRepository
}
