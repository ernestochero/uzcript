package uzcript

import play.api.mvc.{Action, ActionBuilder, BodyParser, Result}
import uzcript.symbol.SymbolService.SymbolService
import zio.{RIO, ZEnv, ZIO, ZLayer}
import zio.Runtime

import scala.concurrent.Future

package object commons {
  type AppContext = SymbolService
  type HttpContext[A] = ZLayer[ZEnv, Throwable, A]
  object AppContext {
    val live: ZLayer[ZEnv, Throwable, AppContext] = SymbolService.live
  }

  implicit class ActionBuilderOps[+R[_], B](ab: ActionBuilder[R, B]) {
    case class AsyncTaskBuilder[Ctx <: zio.Has[_]](dummy: Boolean = false) {
      def apply(
        cb: R[B] => RIO[Ctx, Result]
      )(implicit r: HttpContext[Ctx]): Action[B] =
        ab.async { c =>
          val value: ZIO[ZEnv, Throwable, Result] = cb(c).provideLayer(r)
          val future: Future[Result] = Runtime.default.unsafeRunToFuture(value)
          future
        }

      def apply[A](
        bp: BodyParser[A]
      )(cb: R[A] => RIO[Ctx, Result])(implicit r: HttpContext[Ctx]): Action[A] =
        ab.async[A](bp) { c =>
          val value: ZIO[ZEnv, Throwable, Result] = cb(c).provideLayer(r)
          val future: Future[Result] = Runtime.default.unsafeRunToFuture(value)
          future
        }
    }

    case class AsyncZioBuilder[Ctx <: zio.Has[_]](dummy: Boolean = false) {

      def apply(
        cb: R[B] => ZIO[Ctx, Result, Result]
      )(implicit r: HttpContext[Ctx]): Action[B] =
        ab.async { c =>
          val value: ZIO[ZEnv, Throwable, Result] =
            cb(c).either.map(_.merge).provideLayer(r)
          val future: Future[Result] = Runtime.default.unsafeRunToFuture(value)
          future
        }

      def apply[A](bp: BodyParser[A])(
        cb: R[A] => ZIO[Ctx, Result, Result]
      )(implicit r: HttpContext[Ctx]): Action[A] =
        ab.async[A](bp) { c =>
          val value: ZIO[ZEnv, Throwable, Result] =
            cb(c).either.map(_.merge).provideLayer(r)
          val future: Future[Result] = Runtime.default.unsafeRunToFuture(value)
          future
        }
    }

    def asyncTask[Ctx <: zio.Has[_]] = AsyncTaskBuilder[Ctx]()
    def asyncZio[Ctx <: zio.Has[_]] = AsyncZioBuilder[Ctx]()
  }
}
