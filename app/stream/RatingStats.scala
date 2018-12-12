package stream

import com.google.inject.Inject
import models.{RatingAverage, RatingAverageInfo}

class RatingStats @Inject()(redisStoreClient: RedisStoreClient) {

  def ratingAverage(movieId: Int): RatingAverage = {
    redisStoreClient.ratingAverage(movieId)
  }

  def isBellowMedianAverageRating(movieId: Int): Boolean = {
    val maybeMedian = redisStoreClient.getMedian
    val movieAverageRating = ratingAverage(movieId)

    maybeMedian.exists(median => movieAverageRating.ratingAverage < median)
  }

  def ratingStats(movieId: Int): RatingAverageInfo = {
    RatingAverageInfo(ratingAverage(movieId),
                      isBellowMedianAverageRating(movieId))
  }
}
