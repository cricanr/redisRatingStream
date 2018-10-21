package stream

import com.google.inject.Inject
import com.redis.RedisClient
import com.redis.RedisClient.{ASC, SortOrder}
import models.Rating.decodeRating
import models.{Rating, RatingAverage}
import play.api.Configuration

trait IStoreClient {
  def addToAvgSortedSet(rating: Rating): Unit

  def cleanup: Option[Long]

  def getMedian: Option[Double]

  def ratingAverage(movieId: Int): RatingAverage
}

class RedisStoreClient @Inject()(redisClient: RedisClient)
                                (implicit configuration: Configuration)
  extends IStoreClient {

  private val ratingsRedisSortedList = configuration.get[String]("redis.ratingsSortedList")

  private def formatRating(ratingId: Int, rating: Float) = s"""{"id":$ratingId,"rating":$rating}"""

  private def ratingAvgLines(movieId: Int): List[String] = getRatingAvgLines(movieId: Int)

  private def getRatingAvgLines(id: Int): List[String] = {
    redisClient.zscan(ratingsRedisSortedList, 0, s"""{*id*:$id,*rating*}""", count = 1000000000).flatMap(res => res._2) match {
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
        } yield {
          val maybeFirstMidScore = mid.headOption.map(_._2)
          val maybeNextMidScore = midNext.headOption.map(_._2)
          for {
            firstMidScore <- maybeFirstMidScore
            nextMidScore <- maybeNextMidScore
          } yield (firstMidScore + nextMidScore) / 2
        }

        c.flatten
      } else {
        val index = (s.toInt + 1) / 2
        val median = redisClient.zrangeWithScore(ratingsRedisSortedList, index, index).flatMap(rangeWithScore => rangeWithScore.headOption.map(item => item._2))

        median
      }
    }
  }

  def addToAvgSortedSet(rating: Rating): Unit = {
    redisClient.zscan(ratingsRedisSortedList, 0, s"""{"id":${rating.id},"rating":*}""").flatMap(a => a._2) match {
      case Some(value) =>
        val item = value.collect { case c if c isDefined => c.get }
        if (item.isEmpty)
          redisClient.zadd(ratingsRedisSortedList, rating.rating, formatRating(rating.id, rating.rating))
        else {
          val maybeExistingRating = Rating.decodeRating(item.head)
          maybeExistingRating.map { existingRating =>
            val averageRating = (rating.rating + existingRating.rating) / 2
            val updatedRating = Rating(existingRating.id, averageRating)

            redisClient.zrem(ratingsRedisSortedList, item.head)
            redisClient.zadd(ratingsRedisSortedList, updatedRating.rating, formatRating(rating.id, updatedRating.rating))

            redisClient.zrangeWithScore(ratingsRedisSortedList, 0, 100, sortAs = new SortOrder {
              ASC
            })
          }.getOrElse(redisClient.zadd(ratingsRedisSortedList, rating.rating, formatRating(rating.id, rating.rating)))
        }
      case None =>
        redisClient.zadd(ratingsRedisSortedList, rating.rating, formatRating(rating.id, rating.rating))
    }
  }

  def cleanup: Option[Long] = {
    redisClient.zremrangebyrank(ratingsRedisSortedList, 0, 1000000000)
  }
}
