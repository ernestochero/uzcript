package uzcript.symbol

import java.math.BigInteger

import io.nem.symbol.sdk.api.BlockRepository
import io.nem.symbol.sdk.infrastructure.vertx.RepositoryFactoryVertxImpl
import io.nem.symbol.sdk.model.blockchain.BlockInfo
import zio.{Task, ZIO}
import uzcript.commons.Transformers._
object SymbolNem {
  def buildRepositoryFactory(
    endPoint: String
  ): Task[RepositoryFactoryVertxImpl] =
    ZIO.effect(new RepositoryFactoryVertxImpl(endPoint))
  def getBlockGenesis(blockRepository: BlockRepository): Task[BlockInfo] =
    blockRepository.getBlockByHeight(BigInteger.valueOf(1)).toFuture.toTask
}
