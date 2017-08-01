/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import connectors.{CustomerMatchingAPIConnector, ResidencyStatusAPIConnector}
import helpers.RandomNino
import helpers.helpers.I18nHelper
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, _}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.time.TaxYearResolver

import scala.concurrent.Future

class MemberDetailsControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper with MockitoSugar {

  implicit val headerCarrier = HeaderCarrier()

  val fakeRequest = FakeRequest()
  val currentTaxYear = TaxYearResolver.currentTaxYear
  val uuid = "b5a4c95d-93ff-4054-b416-79c8a7e6f712"
  val SCOTTISH = "scotResident"
  val NON_SCOTTISH = "otherUKResident"
  val nino = RandomNino.generate
  val dob = RasDate("1", "1", "1999")
  val postData = Json.obj("firstName" -> "Jim", "lastName" -> "McGill", "nino" -> nino, "dateOfBirth" -> dob)
  val customerMatchingResponse = CustomerMatchingResponse(
    List(
      Link("self", s"/customer/matched/${uuid}"),
      Link("ras", s"/relief-at-source/customer/${uuid}/residency-status")
    )
  )

  object TestMemberDetailsController extends MemberDetailsController{
    override val customerMatchingAPIConnector: CustomerMatchingAPIConnector = mock[CustomerMatchingAPIConnector]
    override val residencyStatusAPIConnector: ResidencyStatusAPIConnector = mock[ResidencyStatusAPIConnector]
    // following mocks will run if not specified explicitly in individual tests
    when(customerMatchingAPIConnector.findMemberDetails(any())(any())).thenReturn(Future.successful(customerMatchingResponse))
    when(residencyStatusAPIConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus(SCOTTISH, NON_SCOTTISH)))
  }

  private def doc(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))


  // and finally the tests


  "MemberDetailsController" should {

    "respond to GET /relief-at-source/member-details" in {
      val result = route(fakeApplication, FakeRequest(GET, "/relief-at-source/member-details"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return 200" in {
      val result = TestMemberDetailsController.get(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = TestMemberDetailsController.get(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Find member details page" should {

    "contain correct title and header" in {
      val result = TestMemberDetailsController.get(fakeRequest)
      doc(result).title shouldBe Messages("member.details.page.title")
      doc(result).getElementById("header").text shouldBe Messages("member.details.page.header")
    }

    "contain correct field labels" in {
      val result = TestMemberDetailsController.get(fakeRequest)
      doc(result).getElementById("firstName_label").text shouldBe Messages("first.name").capitalize
      doc(result).getElementById("lastName_label").text shouldBe Messages("last.name").capitalize
      doc(result).getElementById("nino_label").text should include(Messages("nino"))
      doc(result).getElementById("dob-legend").text shouldBe Messages("dob").capitalize
    }

    "contain correct input fields" in {
      val result = TestMemberDetailsController.get(fakeRequest)
      assert(doc(result).getElementById("firstName").attr("input") != null)
      assert(doc(result).getElementById("lastName").attr("input") != null)
      assert(doc(result).getElementById("nino").attr("input") != null)
    }

    "contain continue button" in {
      val result = TestMemberDetailsController.get(fakeRequest)
      doc(result).getElementById("continue").text shouldBe Messages("continue")
    }

    "contain a date field" in {
      val result = TestMemberDetailsController.get(fakeRequest)
      doc(result).getElementById("dob-day_label").text shouldBe Messages("day")
      doc(result).getElementById("dob-month_label").text shouldBe Messages("month")
      doc(result).getElementById("dob-year_label").text shouldBe Messages("year")
    }

    "contain hint text for national insurance and date of birth fields" in {
      val result = TestMemberDetailsController.get(fakeRequest)
      doc(result).getElementById("nino_hint").text shouldBe Messages("nino.hint")
      doc(result).getElementById("dob_hint").text shouldBe Messages("dob.hint")
    }
  }

  "Member details controller form submission" should {

    "respond to POST /relief-at-source/member-details" in {
      val result = route(fakeApplication, FakeRequest(POST, "/relief-at-source/member-details"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return bad request when form error present" in {
      val postData = Json.obj(
        "firstName" -> "",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1", "1", "1999"))

      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) should equal(BAD_REQUEST)
    }

    "return ok" in {
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) should equal(OK)
    }

    "return residency status for scottish taxpayer" in {
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      doc(result).getElementById("cy-residency-status").text() shouldBe Messages("scottish.taxpayer")
    }

    "return residency status for non scottish taxpayer" in {
      when(TestMemberDetailsController.residencyStatusAPIConnector.getResidencyStatus(any())(any())).
        thenReturn(Future.successful(ResidencyStatus(NON_SCOTTISH, SCOTTISH)))
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      doc(result).getElementById("cy-residency-status").text() shouldBe Messages("non.scottish.taxpayer")
    }

    "contain this current year's date and period" in {
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      doc(result).getElementById("this-tax-year").text() shouldBe Messages("this.tax.year")
      doc(result).getElementById("tax-year-period").text() shouldBe Messages("tax.year.period", currentTaxYear, currentTaxYear + 1)
    }


  }

}
