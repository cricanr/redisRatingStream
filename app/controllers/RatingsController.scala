package controllers

import akka.actor.ActorSystem
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import stream.{RatingStats, RedisStoreClient}

import scala.concurrent.{ExecutionContext, Future}

class RatingsController @Inject()(cc: ControllerComponents,
                                  actorSystem: ActorSystem,
                                  ratingStats: RatingStats,
                                  redisStoreClient: RedisStoreClient)
                                 (implicit exec: ExecutionContext) extends AbstractController(cc) {
  def ratingInfo(movieId: Int): Action[AnyContent] = Action.async { _ =>
    val median = redisStoreClient.getMedian

    val ratingAverage = ratingStats.ratingAverage(movieId)
    val isBellowMedianAvgRating = ratingStats.isBellowMedianAverageRating(movieId)
    val ratingAverageInfo = ratingStats.ratingStats(movieId)

    println("ratingAverage: " + ratingAverage)
    println("median: " + median)
    println("isBellowMedianAvgRating: " + isBellowMedianAvgRating)
    println(s"ratingAverageInfo($movieId): " + ratingAverageInfo)

    Future.successful(Ok(ratingAverageInfo.asJson.toString))
  }
}
