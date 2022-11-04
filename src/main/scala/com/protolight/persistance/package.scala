package com.protolight

import com.protolight.AffirmationsLibrary._
import zio.*

package object persistance {


  def getAffirmations(paging: Option[Paging], isAscendingOrder: Option[Boolean]): RIO[AffirmationsLibrary, List[Affirmation]] =
    ZIO.environmentWithZIO(_.get.getAll(paging, isAscendingOrder))

  def createAffirmation(affirmation: Affirmation): RIO[AffirmationsLibrary, Affirmation] =
    ZIO.environmentWithZIO(_.get.create(affirmation))

  def updateAffirmation(a: Affirmation): RIO[AffirmationsLibrary, Boolean] = ZIO.environmentWithZIO(_.get.update(a))

  def deleteAffirmation(id: Int): RIO[AffirmationsLibrary, Boolean] = ZIO.environmentWithZIO(_.get.delete(id))
}
