package com.kemal

import akka.stream.scaladsl.{Sink, Source}
import com.kemal.config.AppConfig.{limit, url}
import com.kemal.model._
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Importer extends App with Complements {

  private def pokemonData(offset: Int): Future[ClientResponseWithOffset] = {
    httpClient
      .get[ClientResponse](s"$url?offset=$offset&limit=$limit")
      .map(res => ClientResponseWithOffset(res.results, offset))
  }

  Source
    .future(pokemonRepo.getOffset)
    .flatMapConcat(offset =>
      Source.unfoldAsync(offset) { currentOffset =>
        pokemonData(currentOffset).map {
          case ClientResponseWithOffset(Nil, _)              => None
          case c @ ClientResponseWithOffset(_, _) => Some((offset, c))
        }
    })
    .mapAsync(1) { clientResponse =>
      Source(clientResponse.results)
        .mapAsync(1) { pokemonInfo =>
          for {
            pokemonDetail    <- httpClient.get[PokemonDetail](pokemonInfo.url)
            evolutionChain   <- httpClient.get[EvolutionChain](pokemonDetail.species.map(_.url).get)
            evolutionPokemon <- httpClient.get[EvolutionPokemon](evolutionChain.evolution_chain.url) // Todo import pokemon detail
            firstEvolution   = evolutionPokemon.chain.evolves_to.map(_.species.name).headOption.getOrElse("")
            secondEvolution = evolutionPokemon.chain.evolves_to
              .flatMap(_.evolves_to.map(_.species.name))
              .headOption
              .getOrElse("")
            _ <- {
              import pokemonDetail._
              val firstEvolution = evolutionPokemon.chain.evolves_to.map(_.species.name).headOption.getOrElse("")
              val secondEvolution =
                evolutionPokemon.chain.evolves_to
                  .flatMap(_.evolves_to.map(_.species.name))
                  .headOption
                  .getOrElse("")
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
          } yield pokemonDetail.id
        }
        .runWith(Sink.last)
        .flatMap(offset => pokemonRepo.insertOffset(offset))
    }
    .runWith(Sink.fold(0L) {
      case (acc, stats) =>
        val total = acc + stats
        if (total % 100 == 0) {
          logger.info(s"Processed $total pokemon.")
        }
        total
    })
    .onComplete {
      case Success(total) =>
        logger.info(s"Completed, total pokemon count: $total")
        releaseResources(0)
      case Failure(ex) =>
        logger.error("Failed:", ex)
        releaseResources(1)
    }
}