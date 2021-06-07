package com.evolutiongaming.catshelper

import cats.effect.{Clock, Concurrent, Temporal}
import cats.implicits._
import com.evolutiongaming.catshelper.ClockHelper._

import scala.concurrent.duration.FiniteDuration
import cats.effect.kernel.Deferred
import cats.effect.kernel.Poll
import cats.effect.kernel.Ref
import cats.effect.kernel.Unique
import cats.effect.kernel.Fiber

object TimerHelper {

  implicit class TimerObjOpsTimerHelper(val self: Temporal.type) extends AnyVal {

    def empty[F[_]](implicit F: Concurrent[F]): Temporal[F] = new Temporal[F] {

      val clock = Clock.empty[F]

      override def pure[A](x: A): F[A] = F.pure(x)

      override def raiseError[A](e: Throwable): F[A] = F.raiseError(e)

      override def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]): F[A] = F.handleErrorWith(fa)(f)

      override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)

      override def tailRecM[A, B](a: A)(f: A => F[Either[A,B]]): F[B] = F.tailRecM(a)(f)

      override def forceR[A, B](fa: F[A])(fb: F[B]): F[B] = F.forceR(fa)(fb)

      override def uncancelable[A](body: Poll[F] => F[A]): F[A] = F.uncancelable(body)

      override def canceled: F[Unit] = F.canceled

      override def onCancel[A](fa: F[A], fin: F[Unit]): F[A] = F.onCancel(fa, fin)

      override def unique: F[Unique.Token] = F.unique

      override def start[A](fa: F[A]): F[Fiber[F,Throwable,A]] = F.start(fa)

      override def never[A]: F[A] = F.never

      override def cede: F[Unit] = F.cede

      override def ref[A](a: A): F[Ref[F,A]] = F.ref(a)

      override def deferred[A]: F[Deferred[F,A]] = F.deferred

      override def monotonic: F[FiniteDuration] = clock.monotonic

      override def realTime: F[FiniteDuration] = clock.realTime

      override def sleep(duration: FiniteDuration): F[Unit] = ().pure[F]
    }
  }
}
