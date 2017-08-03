package controllers

import models.ResidencyStatusResult
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, NOT_FOUND, charset, contentType, route}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.time.TaxYearResolver

class MatchFoundControllerSpec extends UnitSpec with WithFakeApplication{

  val fakeRequest = FakeRequest("GET", "/")

  object TestMatchFoundController extends MatchFoundController

  val residencyStatusResult = ResidencyStatusResult(
    Messages("scottish.taxpayer"),
    Messages("scottish.taxpayer"),
    TaxYearResolver.currentTaxYear.toString,
    (TaxYearResolver.currentTaxYear + 1).toString,
    "Jimmy McGill","1999-1-1","")

  "StartPageController" should {

    "respond to GET /relief-at-source/start" in {
      val result = route(fakeApplication, FakeRequest(GET, "/relief-at-source/match-found"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return 200" in {
      val result = TestMatchFoundController.get(residencyStatusResult)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = TestMatchFoundController.get(residencyStatusResult)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

}
