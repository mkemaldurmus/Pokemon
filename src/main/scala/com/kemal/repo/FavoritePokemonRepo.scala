package com.kemal.repo

import akka.actor.ActorSystem
import com.kemal.controller.FavoritePokemonController.FavoritePokemonList
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

class FavoritePokemonRepo(implicit val ctx: PostgresAsyncContext[LowerCase.type],
                          val system: ActorSystem,
                          val ec: ExecutionContext) {

  import ctx._

  private val favoriteListQ = quote(querySchema[FavoritePokemonList]("favorite_list"))

  def insertFavoriteList(favoritePokemons: FavoritePokemonList): Future[Long] = {
    ctx.run(favoriteListQ.insert(lift(favoritePokemons)))
  }
  def deleteList(listName:String): Future[Long] ={
    ctx.run(favoriteListQ.filter(_.name == lift(listName)).delete)
  }
}
