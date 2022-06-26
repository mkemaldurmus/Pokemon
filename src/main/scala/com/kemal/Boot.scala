package com.kemal

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.util.{Failure, Success}

object Boot extends App with Complements {

  val routes = pokemonController.getRoute ~ favoritePokemonControlller.favoriteRout

  Http().newServerAt("localhost", 9000).bind(routes).onComplete {
    case Success(value) => println("server start 9000")
    case Failure(exception) => println(exception.printStackTrace())
  }
}
