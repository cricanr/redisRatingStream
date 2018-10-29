package query

import models.RatingInfosRequest

import scala.util.Try

object QueryStringHelper {
  def parseQueryParams(queryParams: String): RatingInfosRequest = {
    queryParams.replace("?", "")
    val maybeParamsStr = Try(queryParams.split("&")).toOption
    maybeParamsStr.map { paramStr =>
      val maybeMovieIdsStr = paramStr.headOption
      val maybeMovieIds = maybeMovieIdsStr.flatMap { movieIdsStr =>
        val maybeMovieIdsParams = Try(movieIdsStr.split("=")).toOption
        maybeMovieIdsParams.map{movieIdsParams =>
          val movieIdsStr = movieIdsParams.headOption
          if (movieIdsStr.exists(paramName => paramName.equals("movieIds"))) {
            val maybeMovieIds = Try(movieIdsParams(1).split(",")).toOption.map(_.map(_.toInt))
            val maybeLimitParam = Try(paramStr(1)).toOption
            maybeLimitParam.map { limitParam =>
              val maybeLimitParams = Try(limitParam.split('=')).toOption
              maybeLimitParams.flatMap(limitParams => limitParams.headOption)
            }
          }

        }
      }
//        Try(movieIdsStr.split(",").map(_.toInt)).toOption)
      val maybeLimitStr = Try(paramStr(1)).toOption
      maybeLimitStr.map(limitStr => limitStr)
    }

    ???
  }
}
