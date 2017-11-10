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

package services

import connectors.FileUploadConnector
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class UploadServiceSpec extends UnitSpec with OneServerPerSuite with ScalaFutures with MockitoSugar {

  val mockFileUploadConnector = mock[FileUploadConnector]

  object TestUploadService extends UploadService {
    override val fileUploadConnector = mockFileUploadConnector
  }

  "Upload service" when {

    "calling createFileUploadUrl" should {

      "return a url" in {

        val fileUploadConnectorResponse = HttpResponse(201,None,Map("Location" -> List("localhost:8898/file-upload/envelopes/0b215e97-11d4-4006-91db-c067e74fc653")),None)

        when(TestUploadService.fileUploadConnector.createEnvelope()(any())).thenReturn(Future.successful(fileUploadConnectorResponse))

        val result = await(TestUploadService.createFileUploadUrl)

        result.getOrElse("") should include("file-upload/upload/envelopes/0b215e97-11d4-4006-91db-c067e74fc653")

      }

      "return none if envelope id could not be extracted from the header" in {
        val fileUploadConnectorResponse = HttpResponse(201,None,Map("Location" -> List("localhost:8898/file-upload/envelopes/")),None)
        when(TestUploadService.fileUploadConnector.createEnvelope()(any())).thenReturn(Future.successful(fileUploadConnectorResponse))
        val result = await(TestUploadService.createFileUploadUrl)
        result shouldBe None
      }

      "return none if connector fails to return a response" in {
        val fileUploadConnectorResponse = HttpResponse(400,None,Map(),None)
        when(TestUploadService.fileUploadConnector.createEnvelope()(any())).thenReturn(Future.successful(fileUploadConnectorResponse))
        val result = await(TestUploadService.createFileUploadUrl)
        result shouldBe None
      }
    }

  }

}
