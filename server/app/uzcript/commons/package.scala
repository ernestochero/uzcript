package uzcript

import play.api.mvc.{Action, ActionBuilder, BodyParser, Result}
import zio.{RIO, Runtime, ULayer, ZEnv, ZIO}

import scala.concurrent.Future
import uzcript.commons.Environments._

package object commons {
  object AppContext {
    val live = appContext
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
