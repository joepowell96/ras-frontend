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

import helpers.helpers.I18nHelper
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class MatchFoundControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper {

  val fakeRequest = FakeRequest("GET", "/")

  object TestMatchFoundController extends MatchFoundController

  "MatchFoundController" should {

    "respond to GET /relief-at-source/match-found" in {
      val result = route(fakeApplication, FakeRequest(GET, "/relief-at-source/match-found"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return 200" in {
      val result = TestMatchFoundController.get(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = TestMatchFoundController.get(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

}
