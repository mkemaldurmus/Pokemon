package com.kemal.config

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

  val config: Config = ConfigFactory.load()

  val postgresConfig: Config = config.getConfig("postgres")
  val url: String            = config.getString("host.url")
  val limit: Int            = config.getInt("host.limit")
}
