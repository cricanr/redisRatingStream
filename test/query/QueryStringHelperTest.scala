package query

import models.RatingInfosRequest
import org.scalatest.{MustMatchers, WordSpec}

class QueryStringHelperTest extends WordSpec with MustMatchers {
  "The QueryStringHelper" when {
    "calling parseQueryParams" should {
      "return a valid RatingInfosRequest" in {
        val queryParams = "?movieIds=680,43,23&limit=10"
        QueryStringHelper.parseQueryParams(queryParams) mustBe Some(
          RatingInfosRequest(Seq(680, 43, 23), 10))
      }
    }

    "calling parseQueryParams with movieIds and no limit" should {
      "return a valid RatingInfosRequest with default limit = 5" in {
        val queryParams = "?movieIds=68,23"
        QueryStringHelper.parseQueryParams(queryParams) mustBe Some(
          RatingInfosRequest(Seq(68, 23), 100))
      }
    }

    "calling parseQueryParams without movieIds and no limit" should {
      "return no RatingInfosRequest" in {
        val queryParams = ""
        QueryStringHelper.parseQueryParams(queryParams) mustBe None
      }
    }

    "calling parseQueryParams with invalid query parameters" should {
      "return no RatingInfosRequest" in {
        val queryParams = "?cat=Tom"
        QueryStringHelper.parseQueryParams(queryParams) mustBe None
      }
    }

    "calling parseQueryParams with invalid query parameters and known parameter movieIds" should {
      "return a RatingInfosRequest with movieIds and default limit" in {
        val queryParams = "?movieIds=23&cat=Tom"
        QueryStringHelper.parseQueryParams(queryParams) mustBe Some(
          RatingInfosRequest(Seq(23), 100))
      }
    }

    "calling parseQueryParams with valid query parameters in different order" should {
      "return a valid RatingInfosRequest with movieIds " in {
        val queryParams = "?cat=Tom&movieIds=23"
        QueryStringHelper.parseQueryParams(queryParams) mustBe Some(
          RatingInfosRequest(Seq(23), 100))
      }
    }
  }
}
