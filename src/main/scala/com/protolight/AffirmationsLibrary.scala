package com.protolight

import zio._

import scala.collection.mutable

trait AffirmationsLibrary {
  import AffirmationsLibrary._
  /*
    isAscendingOrder, unidentified order when None; Ascending when true, Descending when false;
  */
  def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]): Task[List[Affirmation]]

  // TODO get one

  def create(affirmation: Affirmation): Task[Affirmation]

  def update(one: Affirmation): Task[Boolean]

  def delete(id: Long): Task[Boolean]
}

object AffirmationsLibrary {
  case class Paging(from: Int, limit: Int)

  case class Affirmation(id: Long, content: String, author: Author)
}