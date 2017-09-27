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

import connectors.{CustomerMatchingAPIConnector, ResidencyStatusAPIConnector, UserDetailsConnector}
import helpers.helpers.I18nHelper
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import play.api.{Configuration, Environment}
import services.SessionService
import uk.gov.hmrc.auth.core.{AuthConnector, ~}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class MemberDOBControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper with MockitoSugar{

  val fakeRequest = FakeRequest()
  val mockAuthConnector = mock[AuthConnector]
  val mockUserDetailsConnector = mock[UserDetailsConnector]
  val mockSessionService = mock[SessionService]
  val mockConfig = mock[Configuration]
  val mockEnvironment = mock[Environment]
  val mockRasConnector = mock[ResidencyStatusAPIConnector]
  val mockMatchingConnector = mock[CustomerMatchingAPIConnector]

  val memberName = MemberName("Jackie","Chan")
  val memberNino = MemberNino("AB123456C")
  val memberDob = MemberDateOfBirth(RasDate(Some("12"),Some("12"),Some("2012")))
  val rasSession = RasSession(memberName, memberNino, memberDob, ResidencyStatusResult("","","","","","",""))
  val postData = Json.obj("firstName" -> "Jim", "lastName" -> "McGill")

  object TestMemberDobController extends MemberDOBController{
    val authConnector: AuthConnector = mockAuthConnector
    override val userDetailsConnector: UserDetailsConnector = mockUserDetailsConnector
    override val sessionService = mockSessionService
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val residencyStatusAPIConnector: ResidencyStatusAPIConnector = mockRasConnector
    override val customerMatchingAPIConnector: CustomerMatchingAPIConnector = mockMatchingConnector

    when(mockSessionService.cacheDob(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(rasSession)))
    when(mockSessionService.fetchDob()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(memberDob)))
    when(mockSessionService.fetchRasSession()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(rasSession)))
  }

  val successfulRetrieval: Future[~[Option[String], Option[String]]] = Future.successful(new ~(Some("1234"), Some("/")))

  "MemberDobController get" should {
    when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
      thenReturn(successfulRetrieval)

    when(mockUserDetailsConnector.getUserDetails(any())(any())).
      thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))


    "return ok" when {
      "called" in {
        val result = TestMemberDobController.get(fakeRequest)
        status(result) shouldBe OK
      }
    }

    "contain correct page elements and content" when {
      "rendered" in {
        val result = TestMemberDobController.get(fakeRequest)
        doc(result).title shouldBe Messages("member.dob.page.title")
        doc(result).getElementById("header").text shouldBe Messages("member.dob.page.header","Jackie")
        doc(result).getElementById("dob_hint").text shouldBe Messages("dob.hint")
        doc(result).getElementById("continue").text shouldBe Messages("continue")
        doc(result).getElementById("dob-day_label").text shouldBe Messages("day")
        doc(result).getElementById("dob-month_label").text shouldBe Messages("month")
        doc(result).getElementById("dob-year_label").text shouldBe Messages("year")
      }
    }

    "fill in form" when {
      "details returned from session cache" in {
        when(mockSessionService.fetchDob()(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(memberDob)))
        val result = TestMemberDobController.get(fakeRequest)
        doc(result).getElementById("dob-year").value.toString should include(memberDob.dateOfBirth.year.getOrElse("0"))
        doc(result).getElementById("dob-month").value.toString should include(memberDob.dateOfBirth.month.getOrElse("0"))
        doc(result).getElementById("dob-day").value.toString should include(memberDob.dateOfBirth.day.getOrElse("0"))
      }
    }
  }

  private def doc(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))

}
