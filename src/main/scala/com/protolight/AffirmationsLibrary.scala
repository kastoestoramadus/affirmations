package com.protolight

import zio.*

import scala.collection.mutable

trait AffirmationsLibrary {
  import AffirmationsLibrary._
  /*
    isAscendingOrder, unidentified order when None; Ascending when true, Descending when false;
  */
  def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]): Task[List[Affirmation]]

  def get(id: Long): Task[Affirmation]
  
  def create(affirmation: Affirmation): Task[Affirmation]

  def update(one: Affirmation): Task[Boolean]

  def delete(id: Long): Task[Boolean]
}

object AffirmationsLibrary {
  case class NotFound(id: Long) extends Exception(s"Affirmation with id=$id Not Found")
  
  case class Paging(from: Int, limit: Int)

  case class Affirmation(id: Long, content: String, author: Author)

  val library: URIO[AffirmationsLibrary, AffirmationsLibrary] = ZIO.service

  def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]) =
    ZIO.serviceWithZIO[AffirmationsLibrary](_.getAll(paging, isAscendingOrder))
}