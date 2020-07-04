import com.typesafe.config.ConfigFactory
import play.api.ApplicationLoader.Context
import play.api.mvc.EssentialFilter
import router.Routes
import play.api.routing.Router
import play.api.{
  Application,
  ApplicationLoader,
  BuiltInComponentsFromContext,
  Configuration,
  LoggerConfigurator
}

import controllers.{AssetsComponents, SymbolController}
import zio._
import uzcript.commons.Environments._
class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = {
    val configuration = Configuration(ConfigFactory.load())
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new modules.AppComponentsInstances(
      context.copy(initialConfiguration = configuration)
    ).application
  }
}
package object modules {
  class AppComponentsInstances(context: Context)
      extends BuiltInComponentsFromContext(context)
      with AssetsComponents {
    private val reservation
      : Reservation[Any, Nothing, ZLayer[ZEnv, Throwable, AppEnvironment]] =
      Runtime.default.unsafeRun(appEnvironment.memoize.reserve)

    private implicit val appContext
      : ZLayer[zio.ZEnv, Throwable, AppEnvironment] =
      Runtime.default.unsafeRun(reservation.acquire)

    applicationLifecycle.addStopHook(
      () => Runtime.default.unsafeRunToFuture(reservation.release(Exit.unit))
    )

    override def router: Router =
      new Routes(
        httpErrorHandler,
        new SymbolController(controllerComponents),
        assets
      )
    override def httpFilters: Seq[EssentialFilter] = Seq()
  }
}
