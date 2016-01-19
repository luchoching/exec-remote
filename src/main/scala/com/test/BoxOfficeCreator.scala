package com.test

import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

trait BoxOfficeCreator {this: Actor =>
  def createBoxOffice: ActorRef = context.actorOf(Props[BoxOffice], "boxoffice")
}

object RemoteBoxOfficeCreator {
  val config = ConfigFactory.load("frontend").getConfig("backend")

  val host = config.getString("host")
  val port = config.getInt("port")
  val protocol = config.getString("protocol")
  val systemName = config.getString("system")
  val actorName = config.getString("actor")
}

trait RemoteBoxOfficeCreator extends BoxOfficeCreator{this: Actor =>
  import RemoteBoxOfficeCreator._

  def createPath: String = {
    s"$protocol://$systemName@$host:$port/$actorName"
  }

  override def createBoxOffice: ActorRef = {
    val path = createPath
    context.actorOf(Props(classOf[RemoteLookupProxy],path), "lookupBoxOffice")
  }
}

class RemoteLookupProxy(path: String) extends Actor with ActorLogging {
  context.setReceiveTimeout(3 seconds)
  sendIdentifyRequest()

  def sendIdentifyRequest():Unit = {
    val selection = context.actorSelection(path)
    selection ! Identify(path)
  }

  def receive = identify

  def identify: Receive = {
    case ActorIdentity(`path`, Some(actor)) =>
      context.setReceiveTimeout(Duration.Undefined)
      log.info("switching to active state")
      context.become(active(actor))
      context.watch(actor)

    case ActorIdentity(`path`, None) =>
      log.error(s"Remote actor with path $path is not avaliable")
    case ReceiveTimeout =>
      sendIdentifyRequest()
    case msg: Any =>
      log.error(s"Ignoring message $msg, remote actor is not ready yet.")
  }

  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info(s"Actor $actorRef terminated.")
      log.info("switching to identify state")
      context.become(identify)
      context.setReceiveTimeout(3 seconds)
      sendIdentifyRequest()

    case msg: Any =>
      actor forward msg
  }
}