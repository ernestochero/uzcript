package uzcript.symbol

import io.nem.symbol.sdk.api.RepositoryFactory
import zio.{Has, Ref, ULayer, ZIO, ZLayer}

package object SymbolService {
  type SymbolService = Has[SymbolService.Service]
  val symbolHost = "http://localhost:3000"
  object SymbolService {
    trait Service {
      def getGenerationHashFromBlockGenesis
        : ZIO[SymbolService, Throwable, String]
    }
    def getGenerationHashFromBlockGenesis
      : ZIO[SymbolService, Throwable, String] =
      ZIO.accessM[SymbolService](_.get.getGenerationHashFromBlockGenesis)

    def live: ULayer[SymbolService] =
      ZLayer.succeed {
        new Service {
          override def getGenerationHashFromBlockGenesis
            : ZIO[SymbolService, Throwable, String] =
            for {
              repositoryFactory <- SymbolNem.buildRepositoryFactory(symbolHost)
              blockRepository = repositoryFactory.createBlockRepository()
              blockGenesis <- SymbolNem.getBlockGenesis(blockRepository)
            } yield blockGenesis.getGenerationHash
        }

      }
  }
}
