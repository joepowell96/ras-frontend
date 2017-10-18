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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


import scala.concurrent.Future

class GlobalErrorControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper {

  val fakeRequest = FakeRequest("GET", "/")

  object TestGlobalErrorController extends GlobalErrorController

  private def doc(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))

  "GlobalErrorController" should {

    "respond to GET /relief-at-source/global-error" in {
      val result = route(fakeApplication, FakeRequest(GET, "/relief-at-source/global-error"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return 200" in {
      val result = TestGlobalErrorController.get(fakeRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return HTML" in {
      val result = TestGlobalErrorController.get(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "contain correct title and header" in {
      val result = TestGlobalErrorController.get(fakeRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe Messages("global.error.page.title")
      doc.getElementById("header").text shouldBe Messages("service.unavailable")
      doc.getElementById("description").text shouldBe Messages("technical.difficulty")
    }
  }

}
