package com.protolight.persistance

import com.protolight.{AffirmationsLibrary, Author, Id}
import doobie.implicits.*
import doobie.{Query0, Transactor, Update0}
import zio.{Task, *}
import AffirmationsLibrary.*
import zio.interop.catz.*

final class PersistentLibrary(tnx: Transactor[Task]) extends AffirmationsLibrary {
  import PersistentLibrary._

  def get(id: Long): Task[Affirmation] =
    SQL
      .get(id)
      .option
      .transact(tnx)
      .foldZIO(
        err => ZIO.fail(err),
        {
          case Some(value) => ZIO.succeed(value)
          case None        => ZIO.fail(NotFound(id))
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

  def create(affirmation: Affirmation): Task[Affirmation] =
    SQL
      .create(affirmation)
      .run
      .transact(tnx)
      .foldZIO(err => ZIO.fail(err), _ => ZIO.succeed(affirmation))

  def update(one: Affirmation): Task[Boolean] =
    SQL
      .update(one)
      .run
      .transact(tnx)
      .fold(_ => false, _ => true)

  def delete(id: Long): Task[Boolean] =
    SQL
      .delete(id)
      .run
      .transact(tnx)
      .fold(_ => false, _ => true)
}

object PersistentLibrary {

  object SQL {
    def get(id: Long): Query0[Affirmation] =
      sql"SELECT * FROM affirmation WHERE id = $id".query[Affirmation]

    def getAll(paging: Paging, isAscendingOrder: Boolean): Query0[Affirmation] = {
      val order = if (isAscendingOrder) " ASC " else " DESC "
      // FIXME why "ORDER BY content $order" produces a syntax error; BUG? works fine in psql console
      // sql"SELECT * FROM affirmation ORDER BY content $order OFFSET ${paging.from} LIMIT ${paging.limit} ;"
      sql"SELECT * FROM affirmation OFFSET ${paging.from} LIMIT ${paging.limit} ;"
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
