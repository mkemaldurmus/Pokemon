package com.kemal

import akka.http.scaladsl.Http

import scala.util.{Failure, Success}

object Boot extends App with Complements {

  val routes = pokemonController.getRoute

  Http().newServerAt("localhost", 9000).bind(routes).onComplete {
    case Success(value) => println("server start 9000")
    case Failure(exception) => println(exception.printStackTrace())
  }

}
