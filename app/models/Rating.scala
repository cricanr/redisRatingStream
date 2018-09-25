package models

import io.circe.generic.auto._
import io.circe.parser.decode

case class Rating(id: Int, rating: Float)

case class RatingAverageInfo(ratingAverage: RatingAverage, isBellowMedianAverage: Boolean)

case class RatingAverage(id: Int, ratingAverage: Float)

object Rating {
  def decodeRating(message: String): Option[Rating] = {
    decode[Rating](message).toOption
  }
}