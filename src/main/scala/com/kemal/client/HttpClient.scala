package com.kemal.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class HttpClient extends Directives {
  implicit val system = ActorSystem("HttpClient")
  implicit val dispatcher = system.dispatcher


  val timeout = 30.seconds

  def httpRequest(uri: String): Future[String] =
    Http().singleRequest(HttpRequest(uri = uri)).flatMap(_.entity.toStrict(timeout).map(_.data.utf8String))


}
