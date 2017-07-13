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

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current


class StartPageControllerSpec extends UnitSpec with WithFakeApplication {

  val fakeRequest = FakeRequest("GET", "/")

  object TestStartPageController extends StartPageController

  "StartPageController" should {

    "respond to GET /relief-at-source/start" in {
      val result = route(fakeApplication,FakeRequest(GET, "/relief-at-source/start"))
      status(result.get) should not equal (NOT_FOUND)
    }

    "return 200" in {
      val result = TestStartPageController.get(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = TestStartPageController.get(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "contain correct title and header" in {
      val result = TestStartPageController.get(fakeRequest)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe Messages("start.page.title")
      doc.getElementById("header").text shouldBe Messages("start.page.header")
    }

    "contain introduction paragraphs and help link" in {
      val result = TestStartPageController.get(fakeRequest)
      val doc = Jsoup.parse(contentAsString(result))

      doc.getElementById("introduction-p1").text shouldBe Messages("introduction.paragraph.1")
      doc.getElementById("introduction-p2").text shouldBe Messages("introduction.paragraph.2")
      doc.getElementById("introduction-p3").text should startWith(Messages("introduction.paragraph.3"))

      doc.getElementById("legislation").attr("href") shouldBe "https://www.gov.uk/hmrc-internal-manuals/pensions-tax-manual/ptm044220"
    }



  }


}
