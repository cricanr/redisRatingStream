package query

import models.RatingInfosRequest
import org.scalatest.{MustMatchers, WordSpec}

class QueryStringHelperTest extends WordSpec with MustMatchers {
  "The QueryStringHelper" when {
    "calling parseQueryParams" should {
      "return a valid RatingInfosRequest" ignore {
        val queryParams = "?movieIds=680,43,23&limit=5"
        QueryStringHelper.parseQueryParams(queryParams) mustBe RatingInfosRequest(Seq(680, 42,23), 5)
      }
    }
  }
}
