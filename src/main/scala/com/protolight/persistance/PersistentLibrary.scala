package com.protolight.persistance

import com.protolight.{AffirmationsLibrary, Author, Id}
import doobie.implicits.*
import doobie.{Query0, Transactor, Update0}
import zio.{Task, *}
import AffirmationsLibrary.*
import zio.interop.catz.*

final class PersistentLibrary(tnx: Transactor[Task]) extends AffirmationsLibrary {
  import PersistentLibrary._

  def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]): Task[List[Affirmation]] =
    SQL
      .getAll(paging, isAscendingOrder)
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
    def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]): Query0[Affirmation] =
      sql"SELECT * FROM affirmation".query[Affirmation]

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
