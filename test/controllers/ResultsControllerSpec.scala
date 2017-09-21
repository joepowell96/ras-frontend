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

import connectors.UserDetailsConnector
import helpers.RandomNino
import helpers.helpers.I18nHelper
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment, Mode}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import services.SessionService
import uk.gov.hmrc.auth.core.{AuthConnector, ~}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.time.TaxYearResolver

import scala.concurrent.Future

class ResultsControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper with MockitoSugar{

  val fakeRequest = FakeRequest("GET", "/")
  val currentTaxYear = TaxYearResolver.currentTaxYear

  val SCOTTISH = "Scottish taxpayer"
  val NON_SCOTTISH = "Non-Scottish taxpayer"
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockAuthConnector = mock[AuthConnector]
  val mockUserDetailsConnector = mock[UserDetailsConnector]
  val successfulRetrieval: Future[~[Option[String], Option[String]]] = Future.successful(new ~(Some("1234"), Some("/")))
  val mockSessionService = mock[SessionService]
  val nino = RandomNino.generate
  val dob = RasDate(Some("1"), Some("1"), Some("1999"))
  val memberDetails = MemberDetails("Jim", "McGill", nino, dob)
  val residencyStatusResult = ResidencyStatusResult("","","","","","","")
  val postData = Json.obj("firstName" -> "Jim", "lastName" -> "McGill", "nino" -> nino, "dateOfBirth" -> dob)


  val rasSession = RasSession(memberDetails,residencyStatusResult)



  object TestResultsController extends ResultsController
{
  val authConnector: AuthConnector = mockAuthConnector
  override val userDetailsConnector: UserDetailsConnector = mockUserDetailsConnector

  override val config: Configuration = mockConfig
  override val env: Environment = mockEnvironment

  override val sessionService = mockSessionService

  when(mockSessionService.fetchRasSession()(Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(rasSession)))
}
  private def doc(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))

  "Results Controller" should {
    when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
      thenReturn(successfulRetrieval)

    when(mockUserDetailsConnector.getUserDetails(any())(any())).
      thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

    "return 200 when match found" in {
      val result = TestResultsController.matchFound(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 200 when match not found" in {
      val result = TestResultsController.noMatchFound(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML when match found" in {
      val result = TestResultsController.matchFound(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return HTML when match not found" in {
      val result = TestResultsController.noMatchFound(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "contain correct title when match found" in {
      val result = TestResultsController.matchFound(fakeRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe Messages("match.found.page.title")
    }

    "contain correct title when match not found" in {
      val result = TestResultsController.noMatchFound(fakeRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe Messages("match.not.found.page.title")
      doc.getElementById("match-not-found").text shouldBe Messages("member.details.not.found")
    }

    "contain customer details and residency status when match found" in {
      when(mockSessionService.fetchRasSession()(any(),any())).thenReturn(Future.successful(
        Some(
          RasSession(memberDetails,
          ResidencyStatusResult(
            NON_SCOTTISH,SCOTTISH,
            currentTaxYear.toString,(currentTaxYear + 1).toString,
            memberDetails.firstName +" " + memberDetails.lastName,
            memberDetails.dateOfBirth.asLocalDate.toString("d MMMM yyyy"),
            memberDetails.nino)))
      ))
      val result = TestResultsController.matchFound.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
      doc(result).getElementById("cy-residency-status").text() shouldBe Messages("non.scottish.taxpayer")
      doc(result).getElementById("this-tax-year").text() shouldBe Messages("this.tax.year")
      doc(result).getElementById("tax-year-period").text() shouldBe Messages("tax.year.period", currentTaxYear.toString, (currentTaxYear + 1).toString)
      doc(result).getElementById("name-label").text() shouldBe Messages("name").capitalize
      doc(result).getElementById("name").text() shouldBe (memberDetails.firstName + " " + memberDetails.lastName)
      doc(result).getElementById("dob-label").text() shouldBe Messages("dob").capitalize
      doc(result).getElementById("dob").text() shouldBe memberDetails.dateOfBirth.asLocalDate.toString("d MMMM yyyy")
      doc(result).getElementById("nino-label").text() shouldBe Messages("nino")
      doc(result).getElementById("nino").text() shouldBe memberDetails.nino
      doc(result).getElementById("nty-status-label").text() shouldBe Messages("status.for.next.tax.year", currentTaxYear.toString, (currentTaxYear + 1).toString)
      doc(result).getElementById("nty-status").text() shouldBe Messages("scottish.taxpayer")
      doc(result).getElementById("print-this-page").text() shouldBe Messages("print.this.page")
      doc(result).getElementById("print-this-page").attr("href") shouldBe "javascript:window.print();"
      doc(result).getElementById("find-another-member").text() shouldBe Messages("find.another.member")
      doc(result).getElementById("finish").text() shouldBe Messages("finish")

    }
  }

  "contain customer details and residency status when match not found" in {
    when(mockSessionService.fetchRasSession()(any(), any())).thenReturn(Future.successful(
      Some(
        RasSession(memberDetails,
          ResidencyStatusResult(
            "", "",
            currentTaxYear.toString, (currentTaxYear + 1).toString,
            memberDetails.firstName + " " + memberDetails.lastName,
            memberDetails.dateOfBirth.asLocalDate.toString("d MMMM yyyy"),
            memberDetails.nino)))
    ))
    val result = TestResultsController.noMatchFound.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
    doc(result).getElementById("match-not-found").text shouldBe Messages("member.details.not.found")
    doc(result).getElementById("subheader").text shouldBe Messages("match.not.found.subheader")
    doc(result).getElementById("name-label").text() shouldBe Messages("name").capitalize
    doc(result).getElementById("name").text() shouldBe (memberDetails.firstName + " " + memberDetails.lastName)
    doc(result).getElementById("dob-label").text() shouldBe Messages("dob").capitalize
    doc(result).getElementById("dob").text() shouldBe memberDetails.dateOfBirth.asLocalDate.toString("d MMMM yyyy")
    doc(result).getElementById("nino-label").text() shouldBe Messages("nino")
    doc(result).getElementById("nino").text() shouldBe memberDetails.nino
    doc(result).getElementById("try-again").text() shouldBe Messages("try.again")
  }

  "redirect to global error page when no session data is returned on match found" in {
    when(mockSessionService.fetchRasSession()(any(), any())).thenReturn(Future.successful(None))
    val result = TestResultsController.matchFound.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
    status(result) shouldBe SEE_OTHER
    redirectLocation(result).get should include("global-error")
  }

  "redirect to global error page when no session data is returned on match not found" in {
    when(mockSessionService.fetchRasSession()(any(), any())).thenReturn(Future.successful(None))
    val result = TestResultsController.noMatchFound.apply(fakeRequest.withJsonBody(Json.toJson(postData)))
    status(result) shouldBe SEE_OTHER
    redirectLocation(result).get should include("global-error")
  }
}
