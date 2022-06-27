package com.kemal.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives
import io.circe.Decoder
import io.circe.generic.auto.exportDecoder
import io.circe.parser._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class HttpClient extends Directives {
  implicit val system     = ActorSystem("HttpClient")
  implicit val dispatcher = system.dispatcher

  val timeout = 30.seconds

  def get[T:Decoder](uri: String, success: StatusCode = StatusCodes.OK): Future[T] =
    Http().singleRequest(HttpRequest(uri = uri)).flatMap {
      case response if response.status == success =>
        response.entity
          .toStrict(timeout)
          .map(_.data.utf8String)
          .map(parse(_).flatMap(_.as[T]).fold(throw _, identity))
      case response => throw new RuntimeException(s"$uri parse failed. response: $response")
    }

}
