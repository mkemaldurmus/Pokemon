package com.kemal

import akka.actor.ActorSystem
import com.kemal.client.HttpClient
import com.kemal.config.AppConfig.postgresConfig
import com.kemal.controller.PokemonController
import com.kemal.repo.PokemonRepo
import io.getquill.{LowerCase, PostgresAsyncContext}

import scala.concurrent.ExecutionContextExecutor

trait Complements {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit lazy val postgresCtxLower: PostgresAsyncContext[LowerCase.type] =
    new PostgresAsyncContext(LowerCase, postgresConfig)

  implicit lazy val pokemonRepo: PokemonRepo = new PokemonRepo()
  lazy val httpClient = new HttpClient()
  implicit  lazy val pokemonController: PokemonController =new PokemonController()

  //docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_USER=user postgres:12.6
}
