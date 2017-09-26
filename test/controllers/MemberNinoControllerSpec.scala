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

import connectors.UserDetailsConnector
import helpers.helpers.I18nHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import play.api.{Configuration, Environment}
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future


class MemberNinoControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper with MockitoSugar{

  val fakeRequest = FakeRequest()
  val mockAuthConnector = mock[AuthConnector]
  val mockUserDetailsConnector = mock[UserDetailsConnector]
  val mockSessionService = mock[SessionService]
  val mockConfig = mock[Configuration]
  val mockEnvironment = mock[Environment]

  object TestMemberNinoController extends MemberNinoController{
    val authConnector: AuthConnector = mockAuthConnector
    override val userDetailsConnector: UserDetailsConnector = mockUserDetailsConnector
    override val sessionService = mockSessionService
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
  }

  "member nino controller get" should {

    "return ok" when {
      "called" in {
        val result = await(TestMemberNinoController.get(fakeRequest))
        status(result) shouldBe OK
      }
    }

    "contain correct page elements and content" when {
      "rendered" in {
        val result = await(TestMemberNinoController.get(fakeRequest))
        doc(result).title shouldBe Messages("member.nino.page.title")
        doc(result).getElementById("header").text shouldBe Messages("member.nino.page.header")
        doc(result).getElementById("nino_hint").text shouldBe Messages("nino.hint")
        doc(result).getElementById("nino_label").text should include(Messages("nino"))
        assert(doc(result).getElementById("nino").attr("input") != null)
        doc(result).getElementById("continue").text shouldBe Messages("continue")
      }
    }



  }

  private def doc(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))


}
