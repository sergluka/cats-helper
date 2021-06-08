package com.evolutiongaming.catshelper

import cats.effect.unsafe.IORuntime

trait TestIORuntime {
    implicit val ioRuntime: IORuntime = cats.effect.unsafe.IORuntime.global
}