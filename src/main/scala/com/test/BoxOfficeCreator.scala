package com.test

import akka.actor.{Props, ActorRef, Actor}

trait BoxOfficeCreator {this: Actor =>
  def createBoxOffice: ActorRef = context.actorOf(Props[BoxOffice], "boxoffice")
}

