package query

import models.RatingInfosRequest

import scala.util.Try

object QueryStringHelper {
  def parseQueryParams(queryParams: String): Option[RatingInfosRequest] = {
    val filteredQueryParams = queryParams.replace("?", "")
    val maybeParamsStr = Try(filteredQueryParams.split("&")).toOption
    maybeParamsStr.flatMap { paramStr =>
      val maybeMovieIdsStr = paramStr.headOption
      maybeMovieIdsStr.flatMap { movieIdsStr =>
        val maybeMovieIdsParams = Try(movieIdsStr.split("=")).toOption
        maybeMovieIdsParams.flatMap { movieIdsParams =>
          val movieIdsStr = movieIdsParams.headOption
          if (movieIdsStr.exists(paramName => paramName.equals("movieIds"))) {
            val maybeMovieIds = Try(movieIdsParams(1).split(",")).toOption.map(_.map(_.toInt))
            val maybeLimitParam = Try(paramStr(1)).toOption
            maybeLimitParam.map { limitParam =>
              val maybeLimitParams = Try(limitParam.split('=')).toOption
              maybeLimitParams.flatMap { limitParams =>
                val limitParamKey = limitParams.headOption
                if (limitParamKey.exists(paramName => paramName.equals("limit"))) {
                  val maybeLimitParamValue = Try(limitParams(1).toInt).toOption
                  for {
                    movieIds <- maybeMovieIds
                    limitParamValue <- maybeLimitParamValue
                  } yield RatingInfosRequest(movieIds.toSeq, limitParamValue)
                }
                else
                  maybeMovieIds.map(movieIds => RatingInfosRequest(movieIds.toSeq, 5))
              }
            }.getOrElse(maybeMovieIds.map(movieIds => RatingInfosRequest(movieIds.toSeq, 5)))
          }
          else None
        }
      }
    }
  }
}
