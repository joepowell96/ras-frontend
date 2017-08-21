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
import helpers.helpers.I18nHelper
import models.UserDetails
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment, Mode}
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.auth.core.{AuthConnector, ~}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ResultsControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper with MockitoSugar{

  val fakeRequest = FakeRequest("GET", "/")

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockAuthConnector = mock[AuthConnector]
  val mockUserDetailsConnector = mock[UserDetailsConnector]
  val successfulRetrieval: Future[~[Option[String], Option[String]]] = Future.successful(new ~(Some("1234"), Some("/")))

  object TestResultsController extends ResultsController
{
  val authConnector: AuthConnector = mockAuthConnector
  override val userDetailsConnector: UserDetailsConnector = mockUserDetailsConnector

  override val config: Configuration = mockConfig
  override val env: Environment = mockEnvironment

}
  private def doc(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))

  "Results Controller" should {
    when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
      thenReturn(successfulRetrieval)

    when(mockUserDetailsConnector.getUserDetails(any())(any())).
      thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

    "respond to GET /relief-at-source/match-found" in {
      val result = route(fakeApplication, FakeRequest(GET, "/relief-at-source/match-found"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "respond to GET /relief-at-source/match-not-found" in {
      val result = route(fakeApplication, FakeRequest(GET, "/relief-at-source/match-not-found"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return 200 when match found" in {
      val result = TestResultsController.matchFound(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML when match found" in {
      val result = TestResultsController.matchFound(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "contain correct title when match found" in {
      val result = TestResultsController.matchFound(fakeRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe Messages("match.found.page.title")
    }

    "return 200 when match not found" in {
      val result = TestResultsController.noMatchFound(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML when match not found" in {
      val result = TestResultsController.noMatchFound(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "contain correct title when match not found" in {
      val result = TestResultsController.noMatchFound(fakeRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe Messages("match.not.found.page.title")
      doc.getElementById("match-not-found").text shouldBe Messages("member.details.not.found")
    }
  }

}
