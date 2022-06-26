package com.kemal

import akka.stream.scaladsl.{Sink, Source}
import com.kemal.model._
import io.circe.generic.auto.exportDecoder
import io.circe.parser

import scala.util.{Failure, Success}


object Importer extends App with Complements {

  val pokemonData = httpClient.httpRequest("https://pokeapi.co/api/v2/pokemon?limit=151")
    .map(parser.parse(_)
      .flatMap(_.as[ClientResponse])
      .fold(throw _, identity))
    .map(_.results)

  Source.future(pokemonData)
    .mapConcat(identity)
    .mapAsync(32) { pokemonInfo =>
      for {
        pokemonDetail <- httpClient.httpRequest(pokemonInfo.url).map(parser.parse(_).flatMap(_.as[PokemonDetail]).fold(throw _, identity))
        evolutionChain <- httpClient.httpRequest(pokemonDetail.species.map(_.url).get).map(parser.parse(_).flatMap(_.as[EvolutionChain]).fold(throw _, identity))
        evolutionPokemon <- httpClient.httpRequest(evolutionChain.evolution_chain.url).map(parser.parse(_).flatMap(_.as[EvolutionPokemon]).fold(throw _, identity)) // Todo import pokemon detail
        _ <- pokemonRepo.insertPokemon(PokemonDto(pokemonDetail.id, pokemonDetail.name, pokemonDetail.height, pokemonDetail.weight, pokemonDetail.sprites.front_default, pokemonDetail.sprites.back_default, s"${evolutionPokemon.chain.evolves_to.map(_.species.name).headOption.getOrElse("")} -> ${evolutionPokemon.chain.evolves_to.flatMap(_.evolves_to.map(_.species.name)).headOption.getOrElse("")}", pokemonDetail.stats))
        res = pokemonRepo.insertTypes(TypeDto(pokemonDetail.id, pokemonDetail.types.map(_.`type`.name)))
      } yield res
    }
    .runWith(Sink.ignore).onComplete {
    case Success(value) => println(value)
    case Failure(exception) => println(exception.printStackTrace())
  }
} //TODO release reorces
