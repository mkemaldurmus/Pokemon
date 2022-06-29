import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kemal.Boot.postgresCtxLower
import com.kemal.controller.PokemonController
import com.kemal.controller.PokemonController.{DetailResponse, EvolutionResponse, PokemonResponse, TypesResponse}
import com.kemal.model.{PokemonDto, Stat, Stats, TypeDto}
import com.kemal.repo.PokemonRepo
import de.heikoseeberger.akkahttpcirce._
import io.circe.generic.auto._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class PokemonControllerSpec()
    extends WordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with FailFastCirceSupport {

  implicit val pokemonRepo: PokemonRepo = new PokemonRepo()

  lazy val routes: Route = new PokemonController().pokemonRoute

  val inputPokemon: PokemonDto = PokemonDto(
    1,
    "pikachu",
    50,
    50,
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
    Seq("pikachu", "pika").toString(),
    Seq(Stats(10, Stat("pikachu")))
  )
  val inputTypes: TypeDto = TypeDto(1, "electiric")

  val pokemonResponse = List(
    PokemonResponse(1, "pikachu", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png")
  )
  val typesResponse = List(
    TypesResponse(Some(1), "electiric")
  )
  val evolutionResponse = List(EvolutionResponse(1, "pikachu", Seq("pikachu", "pika").toString()))

  val detailResponse: DetailResponse = DetailResponse(
    1,
    "pikachu",
    50,
    50,
    Seq(Stats(10, Stat("pikachu"))),
    List("electiric"),
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
  )

  override def beforeAll(): Unit = {
    Await.result(pokemonRepo.insertPokemon(inputPokemon), 2 seconds)
    Await.result(pokemonRepo.insertTypes(inputTypes), 2 seconds)
    Await.result(pokemonRepo.insertOffset(10), 2 seconds)
  }

  override def afterAll(): Unit = {
    Await.result(pokemonRepo.deleteTypes(inputTypes.name), 2 seconds)
    Await.result(pokemonRepo.deletePokemon(inputPokemon.id), 2 seconds)
    Await.result(pokemonRepo.deleteOffset(), 2 seconds)
    super.afterAll()
  }

  "PokemonController" should {
    "return Ok with default parameters" in {
      Get("/pokemons") ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)
        responseAs[List[PokemonResponse]] shouldBe pokemonResponse
      }
    }
    "return Ok with parameters" in {
      Get("/pokemons?filter=electiric") ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)
        responseAs[List[PokemonResponse]] shouldBe pokemonResponse
      }

    }
    "return NotFound with filter parameter" in {
      Get("/pokemons?filter=water") ~> routes ~> check {
        status should ===(StatusCodes.NotFound)

        contentType should ===(ContentTypes.`application/json`)
        responseAs[List[PokemonResponse]] shouldBe List.empty
      }
    }

    "return Ok with default parameter" in {
      Get("/types") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[List[TypesResponse]] shouldBe typesResponse
      }
    }
    "return Ok with sort parameter" in {
      Get("/types?sort=name,asc") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[List[TypesResponse]] shouldBe typesResponse
      }
    }

    "return Internal Server Error with wrong parameter" in {
      Get("/types?sort=id,asc") ~> routes ~> check {
        status should ===(StatusCodes.InternalServerError)
      }
    }

    "return Ok with name parameter" in {
      Get("/evolution?name=pikachu") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[List[EvolutionResponse]] shouldBe evolutionResponse
      }
    }

    "return BadRequest with wrong parameters" in {
      Get("/evolution?name=pikachu&id=1") ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
        responseAs[String] shouldBe "id or name required"
      }
    }

    "return NotFound with name parameter" in {
      Get("/evolution?name=pikach") ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[List[EvolutionResponse]] shouldBe List.empty
      }
    }

    "return Ok with id parameter" in {
      Get("/detail?id=1") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[DetailResponse] shouldBe detailResponse
      }
    }

    "return ok with id parameter" in {
      Get("/detail?id=1") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[DetailResponse] shouldBe detailResponse
      }
    }
  }
}
