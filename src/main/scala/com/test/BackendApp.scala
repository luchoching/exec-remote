package com.test

import akka.actor.{Props, Actor, ActorSystem}
import com.typesafe.config.ConfigFactory

object BackendApp extends App {

  val conf =
    """
      akka {
        actor {
          provider = "akka.remote.RemoteActorRefProvider"
        }

        remote {
          enabled-transports = ["akka.remote.netty.tcp"]
          netty.tcp {
            hostname = "0.0.0.0"
            port = 2551
          }
        }
      }
    """.stripMargin

  val config = ConfigFactory.parseString(conf)

  val backend = ActorSystem("backend", config)
  backend.actorOf(Props[Simple], "simple")

}

class Simple extends Actor {
  def receive = {
    case m =>
      println(s"received $m!")
  }
}