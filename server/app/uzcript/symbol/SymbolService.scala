package uzcript.symbol

import zio.{Has, Task, ULayer, ZIO, ZLayer}

package object SymbolService {
  type SymbolService = Has[SymbolService.Service]
  val symbolHost = "http://localhost:3000"
  object SymbolService {
    trait Service {
      def getGenerationHashFromBlockGenesis: Task[String]
    }
    def getGenerationHashFromBlockGenesis
      : ZIO[SymbolService, Throwable, String] =
      ZIO.accessM[SymbolService](_.get.getGenerationHashFromBlockGenesis)

    val live: ULayer[SymbolService] =
      ZLayer.succeed {
        new Service {
          override def getGenerationHashFromBlockGenesis: Task[String] =
            for {
              repositoryFactory <- SymbolNem.buildRepositoryFactory(symbolHost)
              blockRepository = repositoryFactory.createBlockRepository()
              blockGenesis <- SymbolNem.getBlockGenesis(blockRepository)
            } yield SymbolNem.getGenerationHash(blockGenesis)
        }

      }
  }
}
