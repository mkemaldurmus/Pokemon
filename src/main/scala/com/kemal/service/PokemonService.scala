
package com.kemal.service

import akka.actor.ActorSystem
import com.kemal.controller.PokemonController.PokemonResponse
import com.kemal.model.Order.{IdAsc, IdDesc, NameAsc, NameDesc, Order}
import com.kemal.model.{PokemonDto, TypeDto}
import com.kemal.repo.PokemonRepo
import io.getquill.{LowerCase, Ord, PostgresAsyncContext, Query}

import scala.concurrent.{ExecutionContext, Future}

class PokemonService(implicit
                     pokemonRepo: PokemonRepo,
                     val ctx: PostgresAsyncContext[LowerCase.type],
                     val system: ActorSystem,
                     val ec: ExecutionContext) {

  import ctx._




}

