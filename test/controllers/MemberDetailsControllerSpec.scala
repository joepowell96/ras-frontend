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
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, _}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class MemberDetailsControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper with MockitoSugar {

  val fakeGetRequest = FakeRequest("GET", "/")
  val fakePostRequest = FakeRequest("POST", "/")

  val mockCustomerMatchingConnector = mock[CustomerMatchingAPIConnector]
  val mockRasConnector = mock[ResidencyStatusAPIConnector]

  implicit val headerCarrier = HeaderCarrier()

  object TestMemberDetailsController extends MemberDetailsController{
    override val customerMatchingAPIConnector: CustomerMatchingAPIConnector = mockCustomerMatchingConnector
    override val residencyStatusAPIConnector: ResidencyStatusAPIConnector = mockRasConnector
  }

  "MemberDetailsController" should {

    "respond to GET /relief-at-source/member-details" in {
      val result = route(fakeApplication, FakeRequest(GET, "/relief-at-source/member-details"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return 200" in {
      val result = TestMemberDetailsController.get(fakeGetRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = TestMemberDetailsController.get(fakeGetRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Find member details page" should {

    "contain correct title and header" in {
      val result = TestMemberDetailsController.get(fakeGetRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe Messages("member.details.page.title")
      doc.getElementById("header").text shouldBe Messages("member.details.page.header")
    }

    "contain correct field labels" in {
      val result = TestMemberDetailsController.get(fakeGetRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("firstName_label").text shouldBe Messages("first.name").capitalize
      doc.getElementById("lastName_label").text shouldBe Messages("last.name").capitalize
      doc.getElementById("nino_label").text should include(Messages("nino"))
      doc.getElementById("dob-legend").text shouldBe Messages("dob").capitalize
    }

    "contain correct input fields" in {
      val result = TestMemberDetailsController.get(fakeGetRequest)
      val doc = Jsoup.parse(contentAsString(result))
      assert(doc.getElementById("firstName").attr("input") != null)
      assert(doc.getElementById("lastName").attr("input") != null)
      assert(doc.getElementById("nino").attr("input") != null)
    }

    "contain continue button" in {
      val result = TestMemberDetailsController.get(fakeGetRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("continue").text shouldBe Messages("continue")
    }

    "contain a date field" in {
      val result = TestMemberDetailsController.get(fakeGetRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("dob-day_label").text shouldBe Messages("day")
      doc.getElementById("dob-month_label").text shouldBe Messages("month")
      doc.getElementById("dob-year_label").text shouldBe Messages("year")
    }

    "contain hint text for national insurance and date of birth fields" in {
      val result = TestMemberDetailsController.get(fakeGetRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("nino_hint").text shouldBe Messages("nino.hint")
      doc.getElementById("dob_hint").text shouldBe Messages("dob.hint")
    }
  }

  "Member details controller form submission" should {

    "respond to POST /relief-at-source/member-details" in {
      val result = route(fakeApplication, FakeRequest(POST, "/relief-at-source/member-details"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return residency status" in {

      val postData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1", "1", "1999"))

      val uuid = "b5a4c95d-93ff-4054-b416-79c8a7e6f712"
      val customerMatchingResponse = CustomerMatchingResponse(
        List(
          Link("self", s"/customer/matched/${uuid}"),
          Link("ras", s"/relief-at-source/customer/${uuid}/residency-status")
        )
      )

      val residencyStatus = ResidencyStatus("scotResident","otherUKResident")

      when(mockCustomerMatchingConnector.findMemberDetails(any())(any())).thenReturn(Future.successful(customerMatchingResponse))
      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(residencyStatus))

      val result = TestMemberDetailsController.post.apply(fakePostRequest.withJsonBody(Json.toJson(postData)))

      status(result) should equal(OK)
      contentAsString(result) should include(Messages("scottish.taxpayer"))

    }

    "return bad request when errors present" in {
      val postData = Json.obj(
        "firstName" -> "",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1", "1", "1999"))

      val result = TestMemberDetailsController.post.apply(fakePostRequest.withJsonBody(Json.toJson(postData)))
      status(result) should equal(BAD_REQUEST)
    }
  }
}
