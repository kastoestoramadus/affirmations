package com.protolight

import zio.*

import scala.collection.mutable

trait AffirmationsLibrary {
  import AffirmationsLibrary._
  /*
    isAscendingOrder, Ascending as default/None; Ascending when true, Descending when false;
   */
  def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]): Task[List[Affirmation]]

  def get(id: Long): Task[Either[NotFound, Affirmation]]

  def create(affirmation: Affirmation): Task[Either[IdAlreadyTaken, Affirmation]]

  def update(one: Affirmation): Task[Either[NotFound, OperationSuccessful.type]]

  def delete(id: Long): Task[Either[NotFound, OperationSuccessful.type]]
}

object AffirmationsLibrary {
  case class NotFound(id: Long) extends Throwable
  case class IdAlreadyTaken(id: Long) extends Throwable
  case object OperationSuccessful

  case class Paging(from: Int, limit: Int)

  case class Affirmation(id: Long, content: String, author: Author)

  val library: URIO[AffirmationsLibrary, AffirmationsLibrary] = ZIO.service

  def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]) =
    ZIO.serviceWithZIO[AffirmationsLibrary](_.getAll(paging, isAscendingOrder))

  def get(id: Long) =
    ZIO.serviceWithZIO[AffirmationsLibrary](_.get(id))

  def create(affirmation: Affirmation) =
    ZIO.serviceWithZIO[AffirmationsLibrary](_.create(affirmation))

  def update(one: Affirmation) =
    ZIO.serviceWithZIO[AffirmationsLibrary](_.update(one))

  def delete(id: Long) =
    ZIO.serviceWithZIO[AffirmationsLibrary](_.delete(id))
}