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
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.time.TaxYearResolver

import scala.concurrent.Future

class MemberNameControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper with MockitoSugar {

  implicit val headerCarrier = HeaderCarrier()

  val fakeRequest = FakeRequest()
  val mockAuthConnector = mock[AuthConnector]
  val mockUserDetailsConnector = mock[UserDetailsConnector]
  val mockSessionService = mock[SessionService]
  val mockConfig = mock[Configuration]
  val mockEnvironment = mock[Environment]

  val rasSession = RasSession(MemberName("Jackie","Chan"),ResidencyStatusResult("","","","","","",""))

  object TestMemberNameController extends MemberNameController{
    val authConnector: AuthConnector = mockAuthConnector
    override val userDetailsConnector: UserDetailsConnector = mockUserDetailsConnector
    override val sessionService = mockSessionService
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    when(mockSessionService.cacheResidencyStatusResult(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(rasSession)))
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
      val result = TestMemberNameController.get(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = TestMemberNameController.get(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "member name page" should {

    "contain correct title and header" in {
      val result = TestMemberNameController.get(fakeRequest)
      doc(result).title shouldBe Messages("member.name.page.title")
      doc(result).getElementById("header").text shouldBe Messages("member.details.page.header")
    }

//    "contain correct field labels" in {
//      val result = TestMemberNameController.get(fakeRequest)
//      doc(result).getElementById("firstName_label").text shouldBe Messages("first.name").capitalize
//      doc(result).getElementById("lastName_label").text shouldBe Messages("last.name").capitalize
//      doc(result).getElementById("nino_label").text should include(Messages("nino"))
//      doc(result).getElementById("dob-legend").text shouldBe Messages("dob").capitalize
//    }
//
//    "contain correct input fields" in {
//      val result = TestMemberNameController.get(fakeRequest)
//      assert(doc(result).getElementById("firstName").attr("input") != null)
//      assert(doc(result).getElementById("lastName").attr("input") != null)
//      assert(doc(result).getElementById("nino").attr("input") != null)
//    }
//
//    "contain continue button" in {
//      val result = TestMemberNameController.get(fakeRequest)
//      doc(result).getElementById("continue").text shouldBe Messages("continue")
//    }
//
//    "contain a date field" in {
//      val result = TestMemberNameController.get(fakeRequest)
//      doc(result).getElementById("dob-day_label").text shouldBe Messages("day")
//      doc(result).getElementById("dob-month_label").text shouldBe Messages("month")
//      doc(result).getElementById("dob-year_label").text shouldBe Messages("year")
//    }
//
//    "contain hint text for national insurance and date of birth fields" in {
//      val result = TestMemberNameController.get(fakeRequest)
//      doc(result).getElementById("nino_hint").text shouldBe Messages("nino.hint")
//      doc(result).getElementById("dob_hint").text shouldBe Messages("dob.hint")
//    }
//
//    "fill in form if cache data is returned" in {
//      when(mockSessionService.fetchMemberDetails()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(memberDetails)))
//      val result = TestMemberNameController.get(fakeRequest)
//      doc(result).getElementById("firstName").value.toString should include(memberDetails.firstName)
//      doc(result).getElementById("lastName").value.toString should include(memberDetails.lastName)
//      doc(result).getElementById("nino").value.toString should include(memberDetails.nino)
//      doc(result).getElementById("dob-year").value.toString should include(memberDetails.dateOfBirth.year.getOrElse("0"))
//      doc(result).getElementById("dob-month").value.toString should include(memberDetails.dateOfBirth.month.getOrElse("0"))
//      doc(result).getElementById("dob-day").value.toString should include(memberDetails.dateOfBirth.day.getOrElse("0"))
//    }
  }

//  "Member details controller form submission" should {
//
//    "respond to POST /relief-at-source/member-details" in {
//      val result = route(fakeApplication, FakeRequest(POST, "/relief-at-source/member-details"))
//      status(result.get) should not equal (NOT_FOUND)
//    }
//
//    "return bad request when form error present" in {
//      val postData = Json.obj(
//        "firstName" -> "",
//        "lastName" -> "Esfandiari",
//        "nino" -> RandomNino.generate,
//        "dateOfBirth" -> RasDate(Some("1"), Some("1"), Some("1999")))
//
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      status(result) should equal(BAD_REQUEST)
//    }
//
//    "redirect" in {
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      status(result) should equal(SEE_OTHER)
//    }
//
//    "redirect if current year residency status is empty" in {
//      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus("", NON_SCOTTISH)))
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      status(result) should equal(SEE_OTHER)
//      redirectLocation(result).get should include("global-error")
//    }
//
//    "redirect if next year residency status is empty" in {
//      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus(NON_SCOTTISH, "")))
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      status(result) should equal(SEE_OTHER)
//      redirectLocation(result).get should include("global-error")
//    }
//
//    "save details to cache" in {
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      when(mockSessionService.cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
//      verify(mockSessionService, atLeastOnce()).cacheMemberDetails(Matchers.any())(Matchers.any(), Matchers.any())
//    }
//
//    "redirect if unknown current year residency status is returned" in {
//      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus("blah", NON_SCOTTISH)))
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      status(result) shouldBe 303
//    }
//
//    "redirect if unknown next year residency status is returned" in {
//      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.successful(ResidencyStatus(SCOTTISH, "")))
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      status(result) shouldBe 303
//    }
//
//    "redirect to technical error page if customer matching fails to return a response" in {
//      when(mockMatchingConnector.findMemberDetails(any())(any())).thenReturn(Future.failed(new Exception()))
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      status(result) shouldBe 303
//      redirectLocation(result).get should include("global-error")
//    }
//
//    "redirect to technical error page if ras fails to return a response" in {
//      when(mockMatchingConnector.findMemberDetails(any())(any())).thenReturn(Future.successful(Some(uuid)))
//      when(mockRasConnector.getResidencyStatus(any())(any())).thenReturn(Future.failed(new Exception()))
//      val result = TestMemberNameController.post.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
//      status(result) shouldBe 303
//      redirectLocation(result).get should include("global-error")
//    }
//
//  }

}
