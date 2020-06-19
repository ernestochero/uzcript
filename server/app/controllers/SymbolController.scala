package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uzcript.commons.{AppContext, HttpContext, _}
import uzcript.shared.SharedMessages
import uzcript.symbol.SymbolNem.{buildRepositoryFactory, getBlockGenesis}
import uzcript.symbol.SymbolService.SymbolService
import zio.Task

class SymbolController(val controllerComponents: ControllerComponents)(
  implicit c: HttpContext[AppContext]
) extends BaseController {
  val symbolHost = "http://localhost:3000"
  val blockGenesis: Task[String] = for {
    repositoryFactory <- buildRepositoryFactory(symbolHost)
    genesis <- getBlockGenesis(repositoryFactory.createBlockRepository())
  } yield genesis.getHash

  case class GenesisDTO(name: String)

  object GenesisDTO {
    implicit val format = Json.format[GenesisDTO]
  }

  def genesis: Action[AnyContent] = Action.asyncZio[AppContext] { _ =>
    for {
      genesis <- SymbolService.getGenerationHashFromBlockGenesis.orElseFail(
        InternalServerError("")
      )
    } yield Ok(Json.toJson(genesis))
  }

  def index: Action[AnyContent] = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }

  def dashboard: Action[AnyContent] = Action {
    Ok(views.html.dashboard("Hello World"))
  }

}
