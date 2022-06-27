package com.kemal.model

import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import io.circe.{Decoder, Encoder}

case class PokemonDto(id: Int,
                      name: String,
                      height: Int,
                      weight: Int,
                      frontDefault: String,
                      backDefault: String,
                      evolution: String,
                      stats: Seq[Stats])

case class TypeDto(pid: Int, name: String)
case class OffsetDto(current_offset: Int)

case class ClientResponse(results: Seq[PokemonInfo])
case class ClientResponseWithOffset(results: Seq[PokemonInfo], offset: Int)

case class PokemonInfo(name: String, url: String)

case class EvolutionPokemon(chain: Chain)

case class EvolutionChain(evolution_chain: DetailSpecies)

case class Chain(evolves_to: Seq[Evolves])

case class Evolves(species: Species, evolves_to: Seq[EvolvesTo])

case class Species(name: String)

case class EvolvesTo(species: Species)

case class DetailSpecies(url: String)

case class PokemonDetail(id: Int,
                         name: String,
                         height: Int,
                         weight: Int,
                         stats: Seq[Stats],
                         types: Seq[Types],
                         sprites: Sprites,
                         species: Option[DetailSpecies])

case class Sprites(back_default: String, front_default: String)

case class Types(`type`: Type)

case class Type(name: String)

case class Stats(base_stat: Int, stat: Stat)

case class Stat(name: String)

object Order extends ExtendedEnum {
  type Order = Value

  val IdAsc: Order    = Value("id,asc")
  val IdDesc: Order   = Value("id,desc")
  val NameAsc: Order  = Value("name,asc")
  val NameDesc: Order = Value("name,desc")
}

trait ExtendedEnum extends Enumeration {
  type T = Value

  lazy val valueToType: Map[String, T] = values.toList.map(v => v.toString -> v).toMap
  lazy val validValues: List[String]   = valueToType.keySet.toList

  implicit val unmarshaller: FromStringUnmarshaller[T] = Unmarshaller { _ => value =>
    valueToType.get(value) match {
      case Some(enumEntry) =>
        FastFuture.successful(enumEntry)
      case None =>
        FastFuture.failed(
          new IllegalArgumentException(s"Invalid value '$value'. Expected one of:[$validValues]")
        )
    }
  }

  implicit lazy val enumDecoder: Decoder[T]   = Decoder.decodeEnumeration(this)
  implicit lazy val genderEncoder: Encoder[T] = Encoder.encodeEnumeration(this)
}
