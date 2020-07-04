package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uzcript.commons._
import uzcript.shared.SharedMessages
import uzcript.symbol.SymbolService.SymbolService
import uzcript.commons.Environments._

class SymbolController(val controllerComponents: ControllerComponents)(
  implicit c: HttpContext[AppContext]
) extends BaseController {
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
    Ok(views.html.dashboard(""))
  }

  def profile: Action[AnyContent] = Action {
    Ok(views.html.profile(""))
  }

}
