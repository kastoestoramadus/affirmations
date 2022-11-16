package com.protolight.persistance

import com.protolight.{AffirmationsLibrary, Author, Id}
import doobie.implicits.*
import doobie.{Fragment, Query0, Transactor, Update0}
import zio.{Task, *}
import AffirmationsLibrary.*
import zio.interop.catz.*

final class PersistentLibrary(tnx: Transactor[Task]) extends AffirmationsLibrary {
  import PersistentLibrary._

  def get(id: Long): Task[Either[NotFound, Affirmation]] =
    SQL
      .get(id)
      .option
      .transact(tnx)
      .foldZIO(
        err => ZIO.fail(err),
        {
          case Some(value) => ZIO.succeed(Right(value))
          case None        => ZIO.succeed(Left(NotFound(id)))
        }
      )

  def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]): Task[List[Affirmation]] =
    SQL
      .getAll(paging.getOrElse(Paging(0, 10000)), isAscendingOrder.getOrElse(true))
      .to[List]
      .transact(tnx)
      .foldZIO(
        err => ZIO.fail(err),
        maybeAffirmations => ZIO.succeed(maybeAffirmations)
      )

  def create(affirmation: Affirmation): Task[Either[IdAlreadyTaken, Affirmation]] =
    SQL
      .create(affirmation)
      .run
      .transact(tnx)
      .foldZIO(
        _ => ZIO.succeed(Left(IdAlreadyTaken(affirmation.id))), // FIXME not all are that
        _ => ZIO.succeed(Right(affirmation))
      )

  def update(one: Affirmation): Task[Either[NotFound, OperationSuccessful.type]] =
    SQL
      .update(one)
      .run
      .transact(tnx)
      .fold(
        _ => Left(NotFound(one.id)), // FIXME not all are that
        _ => Right(OperationSuccessful)
      )

  def delete(id: Long): Task[Either[NotFound, OperationSuccessful.type]] =
    SQL
      .delete(id)
      .run
      .transact(tnx)
      .fold(
        _ => Left(NotFound(id)), // FIXME not all are that
        _ => Right(OperationSuccessful)
      )
}

object PersistentLibrary {

  object SQL {
    def get(id: Long): Query0[Affirmation] =
      sql"SELECT * FROM affirmation WHERE id = $id".query[Affirmation]

    def getAll(paging: Paging, isAscendingOrder: Boolean): Query0[Affirmation] = {
      val order: Fragment = if (isAscendingOrder) sql" ASC " else sql" DESC "
      sql"SELECT * FROM affirmation ORDER BY content $order OFFSET ${paging.from} LIMIT ${paging.limit} ;"
        .query[Affirmation]
    }

    def create(affirmation: Affirmation): Update0 =
      sql"INSERT INTO affirmation(id, content, author) VALUES (${affirmation.id} ,${affirmation.content}, ${affirmation.author})".update

    def update(one: Affirmation): Update0 =
      sql"UPDATE affirmation SET content = ${one.content} , author = ${one.author}  WHERE id = ${one.id};".update

    def delete(id: Long): Update0 =
      sql"DELETE FROM affirmation WHERE id = $id".update
  }

  val live: ZLayer[Transactor[Task], Throwable, PersistentLibrary] =
    ZLayer {
      for (tnx <- ZIO.service[Transactor[Task]]) yield new PersistentLibrary(tnx)
    }
}
