package com.protolight

import com.protolight.AffirmationsLibrary.*
import zio.{Task, ZIO, ZLayer}

import scala.collection.mutable

object InMemoryLibrary extends AffirmationsLibrary {
  val affirmations = mutable.Set(
    Affirmation(0, "Akceptuję cuda i wszelkie pozytywne zmiany w całym swoim życiu", "Johann Wolfgang von Goethe"),
    Affirmation(1, "Akceptuję doskonałe zdrowie i wygląd swojego ciała", "Eliza Orzeszkowa"),
    Affirmation(2, "Akceptuję i doceniam wysoki potencjał mojego ciała", "Donald Knuth"),
    Affirmation(3, "Akceptuję i szanuję swoje seksualne ciało", "Boleslaw Prus")
  )

  // ignore parameters, to be covered by a smoke test
  def getAll(paging: Option[Paging], isAscendingOrder: Option[Boolean]): Task[List[Affirmation]] =
    ZIO.succeed(affirmations.toList)

  def create(affirmation: Affirmation): Task[Affirmation] = ZIO.succeed { // warn: already exists not checked
    affirmations.add(affirmation)
    affirmation
  }

  def update(one: Affirmation): Task[Boolean] = delete(one.id).map(_ => {
    affirmations.add(Affirmation(one.id, one.content, one.author))
    true
  })

  def delete(id: Long): Task[Boolean] = ZIO.succeed(
    affirmations.find(_.id == id).map(found => {
      affirmations.remove(found)
      true
    }).getOrElse(false)
  )

  val live: ZLayer[Any, Throwable, AffirmationsLibrary] = ZLayer.succeed {InMemoryLibrary}
}
