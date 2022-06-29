import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ValidationRejection
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.kemal.client.PokemonClient
import com.kemal.repo.PokemonRepo
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
//import org.mockito.Mockito.when

import scala.concurrent.Future
import scala.concurrent.duration._

class PokemonController
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with ErrorAccumulatingCirceSupport
    with StrictLogging
    with MockitoSugar {

  implicit val actorSystem      = ActorSystem("case-actor-system")
  implicit val routeTestTimeout = RouteTestTimeout(10.second)


  val pokemonClientMock = mock[PokemonClient]
  val pokemonRepoMock   = mock[PokemonRepo]

  val restService = new PokemonController()

  "rest service" should {

    "return status" in {
      Get(s"/status") ~> restService.restService.fa ~> check {
        handled shouldBe true
      }
    }

    "return products with default parameter" in {
      Get(s"/product") ~> restService.routes ~> check {
        val response = responseAs[Seq[Product]]
        response.size shouldEqual 11
      }
    }

    "return 10 product" in {
      val productServiceMock = new ProductService(elasticClientMock, None) {
        override val sortByV: FieldSort = FieldSort("click").order(SortOrder.DESC)
        override def getProducts(start: Int, size: Int): Future[Seq[Product]] = {
          Future.successful(products.take(10))
        }
      }
      val restService = new PokemonController(productServiceMock)

      Get(s"/product?size=3") ~> restService.routes ~> check {
        val response = responseAs[Seq[Product]]
        response.size shouldEqual 10
      }
    }

    "return 3 product ordered by click desc" in {
      val productService = new ProductService(elasticClientMock, None) {
        //override val sortByV: FieldSort = FieldSort("click").order(SortOrder.DESC)
        override def getProducts(start: Int, size: Int): Future[Seq[Product]] = {
          Future.successful(products.sortWith(_.click > _.click).take(3))
        }
      }
      val restService = new PokemonController(productService)

      Get(s"/product?size=3") ~> restService.routes ~> check {
        val response = responseAs[Seq[Product]]
        response(0).click shouldEqual 333
        response(1).click shouldEqual 77
        response(2).click shouldEqual 56
      }
    }

    "return 3 product ordered by click asc" in {
      val productService = new ProductService(elasticClientMock, None) {
        override def getProducts(start: Int, size: Int): Future[Seq[Product]] = {
          Future.successful(products.sortWith(_.click < _.click).take(3))
        }
      }
      val restService = new PokemonController(productService)

      Get(s"/product?size=3") ~> restService.routes ~> check {
        val response = responseAs[Seq[Product]]
        response(0).click shouldEqual 1
        response(1).click shouldEqual 5
        response(2).click shouldEqual 6
      }
    }

    "return 2 product ordered by purchase desc" in {
      val productService = new ProductService(elasticClientMock, None) {
        override def getProducts(start: Int, size: Int): Future[Seq[Product]] = {
          Future.successful(products.sortWith(_.purchase > _.purchase).take(2))
        }
      }
      val restService = new PokemonController(productService)

      Get(s"/product?size=2") ~> restService.routes ~> check {
        val response = responseAs[Seq[Product]]
        response(0).purchase shouldEqual 999
        response(1).purchase shouldEqual 787
      }
    }

    "return 2 product ordered by purchase asc" in {
      val productService = new ProductService(elasticClientMock, None) {
        override def getProducts(start: Int, size: Int): Future[Seq[Product]] = {
          Future.successful(products.sortWith(_.purchase < _.purchase).take(2))
        }
      }
      val restService = new PokemonController(productService)

      Get(s"/product?size=2") ~> restService.routes ~> check {
        val response = responseAs[Seq[Product]]
        response(0).purchase shouldEqual 0
        response(1).purchase shouldEqual 4
      }
    }

    "return 'page 2 size 10' ordered by default" in {
      val productService = new ProductService(elasticClientMock, None) {
        override def getProducts(start: Int, size: Int): Future[Seq[Product]] = {
          Future.successful(products.sortWith(_.click < _.click).slice(10, 11))
        }
      }
      val restService = new PokemonController(productService)

      Get(s"/product?page=2&size=10") ~> restService.routes ~> check {
        val response = responseAs[Seq[Product]]
        response.size shouldEqual 1
        response.head.click shouldEqual 333
      }
    }

    "return 'page 3 size 3' ordered by default" in {
      val productService = new ProductService(elasticClientMock, None) {
        override def getProducts(start: Int, size: Int): Future[Seq[Product]] = {
          Future.successful(products.sortWith(_.click < _.click).slice(5, 8))
        }
      }
      val restService = new PokemonController(productService)

      Get(s"/product?page=3&size=3") ~> restService.routes ~> check {
        val response = responseAs[Seq[Product]]
        response.size shouldEqual 3
      }
    }

    "return not found" in {
      val productService = new ProductService(elasticClientMock, None) {
        override def getProducts(start: Int, size: Int): Future[Seq[Product]] = {
          Future.successful(Nil)
        }
      }
      val restService = new PokemonController(productService)

      Get(s"/product") ~> restService.routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return error when wrong pagination positive" in {
      Get(s"/product?page=1000000") ~> restService.routes ~> check {
        rejection shouldBe a[ValidationRejection]
      }
    }

    "return error when wrong pagination negative" in {
      Get(s"/product?page=-1") ~> restService.routes ~> check {
        rejection shouldBe a[ValidationRejection]
      }
    }

    "return error when wrong size" in {
      Get(s"/product?size=999999") ~> restService.routes ~> check {
        rejection shouldBe a[ValidationRejection]
      }
    }

    "return error when wrong size and page" in {
      Get(s"/product?size=999999&page=-4") ~> restService.routes ~> check {
        rejection shouldBe a[ValidationRejection]
      }
    }

  }
}
