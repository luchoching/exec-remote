package com.test

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

object BackendMain extends App {
  val config = ConfigFactory.load("backend")
  val system = ActorSystem("backend", config)

  system.actorOf(Props[BoxOffice], "boxOffice")
}