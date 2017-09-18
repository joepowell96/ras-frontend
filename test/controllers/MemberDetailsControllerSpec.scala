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

import java.io.File

import connectors.{CustomerMatchingAPIConnector, ResidencyStatusAPIConnector, UserDetailsConnector}
import helpers.RandomNino
import helpers.helpers.I18nHelper
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, _}
import play.api.{Configuration, Environment, Mode}
import services.SessionService
import uk.gov.hmrc.auth.core.{AuthConnector, ~}
import uk.gov.hmrc.play.http.{HeaderCarrier, Upstream4xxResponse}
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
  val memberDetails = MemberDetails("Jim", "McGill", nino, dob)
  val postData = Json.obj("firstName" -> "Jim", "lastName" -> "McGill", "nino" -> nino, "dateOfBirth" -> dob)
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockAuthConnector = mock[AuthConnector]
  val mockUserDetailsConnector = mock[UserDetailsConnector]
  val mockRasConnector = mock[ResidencyStatusAPIConnector]
  val mockMatchingConnector = mock[CustomerMatchingAPIConnector]
  val mockSessionService = mock[SessionService]
  val residencyStatusResult = ResidencyStatusResult("","","","","","","")
  val rasSession = RasSession(memberDetails.asMemberDetailsWithLocalDate,residencyStatusResult)
  val customerMatchingResponse = CustomerMatchingResponse(
    List(
      Link("self", s"/customer/matched/${uuid}"),
      Link("ras", s"/relief-at-source/customer/${uuid}/residency-status")
    )
  )

  object TestMemberDetailsController extends MemberDetailsController{
    override val residencyStatusAPIConnector: ResidencyStatusAPIConnector = mockRasConnector
    override val customerMatchingAPIConnector: CustomerMatchingAPIConnector = mockMatchingConnector
    val authConnector: AuthConnector = mockAuthConnector
    override val userDetailsConnector: UserDetailsConnector = mockUserDetailsConnector
    override val sessionService = mockSessionService

    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment

    when(mockSessionService.cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(rasSession)))
    when(mockSessionService.cacheResidencyStatusResult(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(rasSession)))
    when(mockSessionService.fetchMemberDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(customerMatchingAPIConnector.findMemberDetails(any())(any())).thenReturn(Future.successful(customerMatchingResponse))
    when(residencyStatusAPIConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus(SCOTTISH, NON_SCOTTISH)))
  }

  private def doc(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))

  // and finally the tests
  val successfulRetrieval: Future[~[Option[String], Option[String]]] = Future.successful(new ~(Some("1234"), Some("/")))

  "MemberDetailsController" should {

    when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
      thenReturn(successfulRetrieval)

    when(mockUserDetailsConnector.getUserDetails(any())(any())).
      thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))


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

    "fill in form if cache data is returned" in {
      when(mockSessionService.fetchMemberDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(memberDetails)))
      val result = TestMemberDetailsController.get(fakeRequest)
      doc(result).getElementById("firstName").value.toString should include(memberDetails.firstName)
      doc(result).getElementById("lastName").value.toString should include(memberDetails.lastName)
      doc(result).getElementById("nino").value.toString should include(memberDetails.nino)
      doc(result).getElementById("dob-year").value.toString should include(memberDetails.dateOfBirth.year)
      doc(result).getElementById("dob-month").value.toString should include(memberDetails.dateOfBirth.month)
      doc(result).getElementById("dob-day").value.toString should include(memberDetails.dateOfBirth.day)
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

    "redirect" in {
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) should equal(SEE_OTHER)
    }

    "redirect if current year residency status is empty" in {
      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus("", NON_SCOTTISH)))
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) should equal(SEE_OTHER)
      redirectLocation(result).get should include("global-error")
    }

    "redirect if next year residency status is empty" in {
      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus(NON_SCOTTISH, "")))
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) should equal(SEE_OTHER)
      redirectLocation(result).get should include("global-error")
    }

    "save details to cache" in {
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      when(mockSessionService.cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      verify(mockSessionService, atLeastOnce()).cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())
    }

    "redirect if unknown current year residency status is returned" in {
      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus("blah", NON_SCOTTISH)))
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) shouldBe 303
    }

    "redirect if unknown next year residency status is returned" in {
      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus(SCOTTISH, "")))
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) shouldBe 303
    }

    "redirect to technical error page if customer matching fails to return a response" in {
      when(mockMatchingConnector.findMemberDetails(any())(any())).thenReturn(Future.failed(new Exception()))
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) shouldBe 303
      redirectLocation(result).get should include("global-error")
    }

    "redirect to technical error page if ras fails to return a response" in {
      when(mockMatchingConnector.findMemberDetails(any())(any())).thenReturn(Future.successful(customerMatchingResponse))
      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.failed(new Exception()))
      val result = TestMemberDetailsController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      status(result) shouldBe 303
      redirectLocation(result).get should include("global-error")
    }


  }

}
