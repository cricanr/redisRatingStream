package query

import models.RatingInfosRequest

import scala.util.Try

object QueryStringHelper {
  def parseQueryParams(queryParams: String): Option[RatingInfosRequest] = {
    val paramsStr = normalizeQueryParams(queryParams)
    val maybeMovieIdsParam = paramsStr.find(param => param.contains("movieIds")).map { movieIdsStr =>
      val maybeMovieIdsParams = Try(movieIdsStr.split("=")).toOption
      maybeMovieIdsParams.flatMap { movieIdsParams =>
        Try(movieIdsParams(1).split(",")).toOption.map(_.map(_.toInt).toSeq)
      }.getOrElse(Seq.empty)
    }

    val limitDefault = 5
    val limitParam = paramsStr.find(param => param.contains("limit")).map { limitStr =>
      val maybeLimitParams = Try(limitStr.split('=')).toOption
      maybeLimitParams.flatMap { limitParams =>
        Try(limitParams(1).toInt).toOption
      }.getOrElse(limitDefault)
    }.getOrElse(limitDefault)

    maybeMovieIdsParam.map(movieIdsParam => RatingInfosRequest(movieIdsParam, limitParam))
  }

  private def normalizeQueryParams(queryParams: String): Seq[String] = {
    val filteredQueryParams = queryParams.replace("?", "")
    val maybeParamsStr = Try(filteredQueryParams.split("&").toSeq).toOption
    maybeParamsStr.getOrElse(Seq.empty)
  }
}
