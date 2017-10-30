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

package connectors

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.JsValue
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost, HttpResponse}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.Future

class FileUploadConnectorSpec extends PlaySpec with OneAppPerSuite with MockitoSugar with ServicesConfig {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestConnector extends FileUploadConnector {
    override val http: HttpPost = mock[HttpPost]
  }

  "File upload connector" should {

    "should handle 201 responses returned from file upload service when create envelope is called" in {
      val response = HttpResponse(201,None,Map("Location" -> List("localhost:8898/file-upload/envelopes/0b215e97-11d4-4006-91db-c067e74fc653")),None)
      when(TestConnector.http.POST[JsValue,HttpResponse](any(),any(),any())(any(),any(),any(),any())).thenReturn(Future.successful(response))

      val result = await(TestConnector.getEnvelope())
      result mustBe response
    }

  }

}
