package com.evolutiongaming

import cats.effect.Bracket
import cats.{ApplicativeError, MonadError}

package object catshelper {

  type ApplicativeThrowable[F[_]] = ApplicativeError[F, Throwable]

  object ApplicativeThrowable {

    @deprecated("use `summon` instead", "2.0.2")
    def apply[F[_]](implicit F: ApplicativeThrowable[F]): ApplicativeThrowable[F] = F

    def summon[F[_]](implicit F: ApplicativeThrowable[F]): ApplicativeThrowable[F] = F
  }


  type MonadThrowable[F[_]] = MonadError[F, Throwable]

  object MonadThrowable {

    @deprecated("use `summon` instead", "2.0.2")
    def apply[F[_]](implicit F: MonadThrowable[F]): MonadThrowable[F] = F

    def summon[F[_]](implicit F: MonadThrowable[F]): MonadThrowable[F] = F
  }


  type BracketThrowable[F[_]] = Bracket[F, Throwable]


  object BracketThrowable {

    @deprecated("use `summon` instead", "2.0.2")
    def apply[F[_]](implicit F: BracketThrowable[F]): BracketThrowable[F] = F

    def summon[F[_]](implicit F: BracketThrowable[F]): BracketThrowable[F] = F
  }
}
