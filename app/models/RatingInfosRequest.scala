package models

case class RatingInfosRequest(movieIds: Seq[Int], limit: Int)