package stream

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.google.inject.{Inject, Singleton}
import models.Rating
import models.Rating._
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext
import scala.util.Try

@Singleton
class RatingsStream @Inject()(ws: WSClient, redisStoreClient: RedisStoreClient)(
    implicit ec: ExecutionContext,
    mat: Materializer,
    configuration: Configuration) {

  private def getUrl: String = {
    val baseUrl = configuration.get[String]("rating.stream.baseUrl")
    val method = configuration.get[String]("rating.stream.method")

    s"$baseUrl/$method"
  }

  private val url = getUrl

  streamRatingsToRedis()

  def streamRatingsToRedis(): Unit = {
    ws.url(url).withMethod("GET").stream().foreach { response =>
      val sink = Sink.foreach[ByteString] { bytes =>
        val maybeRating = decodeRatingMessage(bytes)
        println(bytes.map(_.toChar).mkString.substring(6))
        maybeRating.foreach(rating =>
          redisStoreClient.addToAvgSortedSet(rating))
      }

      response.bodyAsSource.runWith(sink).andThen {
        case result =>
          result.get
      }
    }
  }

  private[stream] def decodeRatingMessage(bytes: ByteString): Option[Rating] = {
    val maybeMessage = Try(bytes.map(_.toChar).mkString.substring(6)).toOption
    maybeMessage.flatMap(message => decodeRating(message))
  }
}
