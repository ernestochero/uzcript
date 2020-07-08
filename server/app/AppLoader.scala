import com.typesafe.config.ConfigFactory
import play.api.ApplicationLoader.Context
import play.api.mvc.EssentialFilter
import router.Routes
import play.api.routing.Router
import play.api.{
  Application,
  ApplicationLoader,
  BuiltInComponentsFromContext,
  LoggerConfigurator
}

import controllers.{AssetsComponents, SymbolController}
import zio._
import uzcript.commons.Environments._
class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new modules.AppComponentsInstances(context).application
  }
}
package object modules {
  class AppComponentsInstances(context: Context)
      extends BuiltInComponentsFromContext(context)
      with AssetsComponents {
    import zio.interop.catz._

    implicit val runtime: Runtime[ZEnv] = Runtime.default

    type Eff[A] = ZIO[ZEnv, Throwable, A]

    private implicit val (
      appContext: ZLayer[ZEnv, Throwable, AppEnvironment],
      release: Eff[Unit]
    ) =
      Runtime.default.unsafeRun(
        appEnvironment.memoize.toResource[Eff].allocated
      )
    applicationLifecycle.addStopHook(
      () => Runtime.default.unsafeRunToFuture(release)
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
