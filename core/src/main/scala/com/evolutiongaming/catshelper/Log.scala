package com.evolutiongaming.catshelper

import cats.effect.Sync
import cats.data.NonEmptyMap
import cats.{Applicative, ~>}
import org.slf4j.{Logger, MDC}

trait Log[F[_]] {

  @inline def debug(msg: => String): F[Unit] = debug(msg, mdc = Log.Mdc.empty)

  @inline def info(msg: => String): F[Unit] = info(msg, mdc = Log.Mdc.empty)

  @inline def warn(msg: => String): F[Unit] = warn(msg, mdc = Log.Mdc.empty)

  @inline def warn(msg: => String, cause: Throwable): F[Unit] = warn(msg, cause, mdc = Log.Mdc.empty)

  @inline def error(msg: => String): F[Unit] = error(msg, mdc = Log.Mdc.empty)

  @inline def error(msg: => String, cause: Throwable): F[Unit] = error(msg, cause, mdc = Log.Mdc.empty)

  def debug(msg: => String, mdc: Log.Mdc): F[Unit]

  def info(msg: => String, mdc: Log.Mdc): F[Unit]

  def warn(msg: => String, mdc: Log.Mdc): F[Unit]

  def warn(msg: => String, cause: Throwable, mdc: Log.Mdc): F[Unit]

  def error(msg: => String, mdc: Log.Mdc): F[Unit]

  def error(msg: => String, cause: Throwable, mdc: Log.Mdc): F[Unit]
}

object Log {

  sealed trait Mdc
  object Mdc {

    private object Empty extends Mdc
    private final case class Context(values: NonEmptyMap[String, String]) extends Mdc {
      override def toString: String = s"MDC(${values.toSortedMap.mkString(", ")})"
    }

    val empty: Mdc = Empty

    def apply(head: (String, String), tail: (String, String)*): Mdc = Context(NonEmptyMap.of(head, tail: _*))

    implicit final class MdcOps(val mdc: Mdc) extends AnyVal {
      def context: Option[NonEmptyMap[String, String]] = mdc match {
        case Empty => None
        case Context(values) => Some(values)
      }
    }
  }

  def apply[F[_]](implicit F: Log[F]): Log[F] = F

  def summon[F[_]](implicit F: Log[F]): Log[F] = F

  def apply[F[_]: Sync](logger: Logger): Log[F] = new Log[F] {

    def withMDC(mdc: Log.Mdc)(log: => Unit): Unit = {
      import Mdc.MdcOps
      mdc.context match {
        case None => log
        case Some(mdc) =>
          val backup = MDC.getCopyOfContextMap
          MDC.clear()
          mdc.toSortedMap foreach { case (k, v) => MDC.put(k, v) }
          log
          if (backup != null && !backup.isEmpty) MDC.setContextMap(backup)
      }
    }

    def debug(msg: => String, mdc: Log.Mdc) = {
      Sync[F].delay {
        if (logger.isDebugEnabled) withMDC(mdc) { logger.debug(msg) }
      }
    }

    def info(msg: => String, mdc: Log.Mdc) = {
      Sync[F].delay {
        if (logger.isInfoEnabled) withMDC(mdc) { logger.info(msg) }
      }
    }

    def warn(msg: => String, mdc: Log.Mdc) = {
      Sync[F].delay {
        if (logger.isWarnEnabled) withMDC(mdc) { logger.warn(msg) }
      }
    }

    def warn(msg: => String, cause: Throwable, mdc: Log.Mdc) = {
      Sync[F].delay {
        if (logger.isWarnEnabled) withMDC(mdc) { logger.warn(msg, cause) }
      }
    }

    def error(msg: => String, mdc: Log.Mdc) = {
      Sync[F].delay {
        if (logger.isErrorEnabled) withMDC(mdc) { logger.error(msg) }
      }
    }

    def error(msg: => String, cause: Throwable, mdc: Log.Mdc) = {
      Sync[F].delay {
        if (logger.isErrorEnabled) withMDC(mdc) { logger.error(msg, cause) }
      }
    }
  }

  def const[F[_]](unit: F[Unit]): Log[F] = new Log[F] {

    def debug(msg: => String, mdc: Log.Mdc) = unit

    def info(msg: => String, mdc: Log.Mdc) = unit

    def warn(msg: => String, mdc: Log.Mdc) = unit

    def warn(msg: => String, cause: Throwable, mdc: Log.Mdc) = unit

    def error(msg: => String, mdc: Log.Mdc) = unit

    def error(msg: => String, cause: Throwable, mdc: Log.Mdc) = unit
  }

  def empty[F[_]: Applicative]: Log[F] = const(Applicative[F].unit)

  implicit class LogOps[F[_]](val self: Log[F]) extends AnyVal {

    def mapK[G[_]](f: F ~> G): Log[G] = new Log[G] {

      def debug(msg: => String, mdc: Log.Mdc) = f(self.debug(msg, mdc))

      def info(msg: => String, mdc: Log.Mdc) = f(self.info(msg, mdc))

      def warn(msg: => String, mdc: Log.Mdc) = f(self.warn(msg, mdc))

      def warn(msg: => String, cause: Throwable, mdc: Log.Mdc) = f(self.warn(msg, cause, mdc))

      def error(msg: => String, mdc: Log.Mdc) = f(self.error(msg, mdc))

      def error(msg: => String, cause: Throwable, mdc: Log.Mdc) = f(self.error(msg, cause, mdc))
    }

    def mapMsg(f: String => String): Log[F] = new Log[F] {

      def debug(msg: => String, mdc: Log.Mdc) = self.debug(f(msg), mdc)

      def info(msg: => String, mdc: Log.Mdc) = self.info(f(msg), mdc)

      def warn(msg: => String, mdc: Log.Mdc) = self.warn(f(msg), mdc)

      def warn(msg: => String, cause: Throwable, mdc: Log.Mdc) = self.warn(f(msg), cause, mdc)

      def error(msg: => String, mdc: Log.Mdc) = self.error(f(msg), mdc)

      def error(msg: => String, cause: Throwable, mdc: Log.Mdc) = self.error(f(msg), cause, mdc)
    }

    def prefixed(prefix: String): Log[F] = mapMsg(msg => s"$prefix $msg")
  }
}
