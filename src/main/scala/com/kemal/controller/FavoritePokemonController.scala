package com.kemal.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import com.kemal.Complements
import com.kemal.controller.FavoritePokemonController.FavoritePokemonList
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps

import scala.util.{Failure, Success}

class FavoritePokemonController extends Complements {
  val favoriteRout: Route = {
    (path("favorites") & post) {
      entity(as[FavoritePokemonList]) { body =>
        onComplete(favoritePokemonRepo.insertFavoriteList(body)) {
          case Success(value)     => complete(StatusCodes.Created, value.asJson)
          case Failure(exception) => failWith(exception)
        }
      }
    } ~ delete {
      parameters(Symbol("name").as[String]) { listName =>
        onComplete(favoritePokemonRepo.deleteList(listName)) { // TODO not found
          case Success(value)     => complete(StatusCodes.Accepted, value.asJson)
          case Failure(exception) => failWith(exception)
        }
      }
    }
  }
}

object FavoritePokemonController {
  case class FavoritePokemonList(name: String, pokemonNames: Seq[String])

}
