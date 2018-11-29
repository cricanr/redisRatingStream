package controllers

import java.net.URLDecoder

import akka.actor.ActorSystem
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.Inject
import models.{RatingInfosRequest, RatingStatsInfo}
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents
}
import query.QueryStringHelper
import stream.{RatingStats, RedisStoreClient}

import scala.concurrent.{ExecutionContext, Future}

class RatingsController @Inject()(
    cc: ControllerComponents,
    actorSystem: ActorSystem,
    ratingStats: RatingStats,
    redisStoreClient: RedisStoreClient)(implicit exec: ExecutionContext)
    extends AbstractController(cc) {

  def ratingInfo(movieId: Int): Action[AnyContent] = Action.async { _ =>
    val ratingAverageInfo = ratingStats.ratingStats(movieId)
    val ratingStatInfos = ratingStatsInfo(movieId)

    println("ratingAverage: " + ratingAverageInfo.ratingAverage)
    println("median: " + ratingStatInfos.median)
    println(
      "isBellowMedianAvgRating: " + ratingAverageInfo.isBellowMedianAverage)
    println(s"ratingAverageInfo($movieId): " + ratingAverageInfo)

    Future.successful(Ok(ratingAverageInfo.asJson.toString))
  }

  def ratingInfos(queryParams: String): Action[AnyContent] = Action.async { _ =>
    val maybeRatingInfosRequest =
      QueryStringHelper.parseQueryParams(URLDecoder.decode(queryParams, "UTF8"))
    val ratingAverageInfos = maybeRatingInfosRequest
      .map { ratingInfoRequest =>
        val limitedMovieIds = limitMovieIds(ratingInfoRequest)
        limitedMovieIds.map { movieId =>
          val ratingAverageInfo = ratingStats.ratingStats(movieId)
          ratingAverageInfo
        }

      }
      .getOrElse(Seq.empty)

    Future.successful(Ok(ratingAverageInfos.asJson.toString))
  }

  private def ratingStatsInfo(movieId: Int): RatingStatsInfo = {
    val maybeMedian = redisStoreClient.getMedian
    val ratingAverageInfo = ratingStats.ratingStats(movieId)

    RatingStatsInfo(maybeMedian.getOrElse(-1), ratingAverageInfo)
  }

  private def limitMovieIds(ratingInfoRequest: RatingInfosRequest): Seq[Int] = {
    if (ratingInfoRequest.movieIds.size > ratingInfoRequest.limit)
      ratingInfoRequest.movieIds.take(ratingInfoRequest.limit)
    else
      ratingInfoRequest.movieIds
  }
}
