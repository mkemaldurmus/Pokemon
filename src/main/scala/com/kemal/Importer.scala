package com.kemal

import akka.stream.scaladsl.{Sink, Source}
import com.kemal.config.AppConfig.{limit, url}
import com.kemal.model._
import io.circe.generic.auto.exportDecoder
import io.circe.parser

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Importer extends App with Complements {

  def pokemonData(offset: Int): Future[ClientResponseWithOffset] = {
    httpClient
      .httpRequest(s"$url?offset=$offset&limit=$limit")
      .map(
        parser
          .parse(_)
          .flatMap(_.as[ClientResponse])
          .fold(throw _, identity))
      .map(res => ClientResponseWithOffset(res.results, offset))
  }

  Source(1 to 100) // TODO toplam pokemon sayısı / limit
    .mapAsync(1) { count =>
      val clientResponse = pokemonRepo.getOffset.flatMap(pokemonData)
      val x = clientResponse.flatMap { clientResponse =>
        val y = clientResponse.results
          .map { pokemonInfo =>
            for {
              pokemonDetail <- httpClient
                                .httpRequest(pokemonInfo.url)
                                .map(parser.parse(_).flatMap(_.as[PokemonDetail]).fold(throw _, identity))
              evolutionChain <- httpClient
                                 .httpRequest(pokemonDetail.species.map(_.url).get)
                                 .map(parser.parse(_).flatMap(_.as[EvolutionChain]).fold(throw _, identity))
              evolutionPokemon <- httpClient
                                   .httpRequest(evolutionChain.evolution_chain.url)
                                   .map(parser.parse(_).flatMap(_.as[EvolutionPokemon]).fold(throw _, identity)) // Todo import pokemon detail
              _ <- {
                import pokemonDetail._
                val firstEvolution = evolutionPokemon.chain.evolves_to.map(_.species.name).headOption.getOrElse("")
                val secondEvolution =
                  evolutionPokemon.chain.evolves_to.flatMap(_.evolves_to.map(_.species.name)).headOption.getOrElse("")
                pokemonRepo
                  .insertPokemon(
                    PokemonDto(id,
                               name,
                               height,
                               weight,
                               sprites.front_default,
                               sprites.back_default,
                               s"$firstEvolution -> $secondEvolution",
                               stats))
              }
              _ = pokemonDetail.types.map(res => pokemonRepo.insertTypes(TypeDto(pokemonDetail.id, res.`type`.name)))
            } yield Future.successful(())
          }
          .map(_.flatten)
        Future.sequence(y)
      }

      x.map(_ => clientResponse.flatMap(res => pokemonRepo.insertOffset(res.offset + res.results.size + 1)))
    }
    .runWith(Sink.ignore)
    .onComplete {
      case Success(value)     => println("Bitti " + value)
      case Failure(exception) => println(exception.printStackTrace())
    }
} //TODO release reorces
