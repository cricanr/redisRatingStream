package stream

import com.google.inject.Inject
import com.redis.RedisClient.{ASC, SortOrder}
import models.Rating.decodeRating
import models.{Rating, RatingAverage}

trait IStoreClient {
  def addToAvgSortedSet(rating: Rating): Unit

  def cleanup: Option[Long]

  def getMedian: Option[Double]

  def ratingAverage(movieId: Int): RatingAverage
}

class RedisStoreClient @Inject() extends IStoreClient {

  import com.redis._

  private val ratingsRedisSortedList = "ratingsAvg2"
  private val redisClient = new RedisClient("localhost", 6379)

  private def ratingAvgLines(movieId: Int): List[String] = getRatingAvgLines(movieId: Int)

  private def getRatingAvgLines(id: Int): List[String] = {
    redisClient.zscan(ratingsRedisSortedList, 0, s"""{*id*:$id,*rating*}""", count = 1000000000).flatMap(a => a._2) match {
      case Some(value) =>
        value.collect { case ratingLine if ratingLine isDefined => ratingLine.get }
      case None =>
        List.empty
    }
  }

  def ratingAverage(movieId: Int): RatingAverage = {
    val ratingAvgLinesUnfiltered = ratingAvgLines(movieId)

    val maybeRatingsPruned = ratingAvgLinesUnfiltered.collect { case line if line nonEmpty => line.trim }
    val maybeRating = maybeRatingsPruned.headOption.flatMap(line => decodeRating(line))
    maybeRating.map(rating => RatingAverage(rating.id, rating.rating)).getOrElse(RatingAverage(movieId, -1))
  }

  def getMedian: Option[Double] = {
    val maybeSize = redisClient.zcard(ratingsRedisSortedList)
    maybeSize.flatMap { s =>
      if (s % 2 == 0) {
        val midIndex = s.toInt / 2
        val midNextIndex = midIndex + 1

        val c = for {
          mid <- redisClient.zrangeWithScore(ratingsRedisSortedList, midIndex, midIndex)
          midNext <- redisClient.zrangeWithScore(ratingsRedisSortedList, midNextIndex, midNextIndex)
        } yield (mid.head._2 + midNext.head._2) / 2

        c
      } else {
        val index = (s.toInt + 1) / 2
        val median = redisClient.zrangeWithScore(ratingsRedisSortedList, index, index).map(a => a.head._2)

        median
      }
    }
  }

  def addToAvgSortedSet(rating: Rating): Unit = {
    redisClient.zscan(ratingsRedisSortedList, 0, s"""{"id":${rating.id},"rating":*}""").flatMap(a => a._2) match {
      case Some(value) =>
        val item = value.collect { case c if c isDefined => c.get }
        if (item.isEmpty)
          redisClient.zadd(ratingsRedisSortedList, rating.rating, s"""{"id":${rating.id},"rating":${rating.rating}}""")
        else {
          println("item: " + item)
          val maybeExistingRating = Rating.decodeRating(item.head)
          maybeExistingRating.map { existingRating =>
            val averageRating = (rating.rating + existingRating.rating) / 2
            val updatedRating = Rating(existingRating.id, averageRating)

            redisClient.zrem(ratingsRedisSortedList, item.head)
            redisClient.zadd(ratingsRedisSortedList, updatedRating.rating, s"""{"id":${rating.id},"rating":${updatedRating.rating}}""")

            redisClient.zrangeWithScore(ratingsRedisSortedList, 0, 100, sortAs = new SortOrder {
              ASC
            })
          }.getOrElse(redisClient.zadd(ratingsRedisSortedList, rating.rating, s"""{"id":${rating.id},"rating":${rating.rating}}"""))
        }
      case None =>
        redisClient.zadd(ratingsRedisSortedList, rating.rating, s"""{"id":${rating.id},"rating":${rating.rating}}""")
    }
  }

  def cleanup: Option[Long] = {
    redisClient.zremrangebyrank(ratingsRedisSortedList, 0, 1000000000)
  }
}
