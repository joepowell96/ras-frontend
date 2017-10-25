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
import models.UserDetails
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import play.api.{Configuration, Environment}
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class DashboardControllerSpec extends UnitSpec with OneServerPerSuite with MockitoSugar {

  implicit val headerCarrier = HeaderCarrier()

  val fakeRequest = FakeRequest()
  val mockAuthConnector = mock[AuthConnector]
  val mockUserDetailsConnector = mock[UserDetailsConnector]
  val mockSessionService = mock[SessionService]
  val mockConfig = mock[Configuration]
  val mockEnvironment = mock[Environment]
  val successfulRetrieval: Future[~[Option[String], Option[String]]] = Future.successful(new ~(Some("1234"), Some("/")))

  object TestDashboardController extends DashboardController {
    val authConnector: AuthConnector = mockAuthConnector
    override val userDetailsConnector: UserDetailsConnector = mockUserDetailsConnector
    override val sessionService = mockSessionService
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment

    when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any(),any())).
      thenReturn(successfulRetrieval)

    when(mockUserDetailsConnector.getUserDetails(any())(any())).
      thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))
  }

  "DashboardController" must {

    "respond to GET /dashboard" in {
      val result = TestDashboardController.get(fakeRequest)
      status(result) shouldBe OK
    }

  }

}
