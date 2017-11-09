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

import java.io.{ByteArrayInputStream, InputStream, InputStreamReader}

import akka.stream.scaladsl.Source
import akka.util.ByteString
import connectors.FileUploadConnector
import models.RawMemberDetails
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.ws.{DefaultWSResponseHeaders, StreamedResponse}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class FileProcessingServiceSpec extends UnitSpec with OneServerPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfter {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockFileUploadConnector = mock[FileUploadConnector]

  val SUT = new FileProcessingService {
    override val fileUploadConnector = mockFileUploadConnector
  }

  "FileProcessingService" should {
    "readFile" when {
      "file exists line by line" in {

        val envelopeId: String = "0b215e97-11d4-4006-91db-c067e74fc653"
        val fileId: String = "file-id-1"

        val row1 = "John,Smith,AB123456C,1990-02-21".getBytes
        val inputStream = new ByteArrayInputStream(row1)

        val streamResponse:StreamedResponse = StreamedResponse(DefaultWSResponseHeaders(200, Map("CONTENT_TYPE" -> Seq("application/octet-stream"))),
          Source.apply[ByteString](Seq(ByteString("John, "), ByteString("Smith, "),
            ByteString("AB123456C, "), ByteString("1990-02-21")).to[scala.collection.immutable.Iterable]) )

        when(mockFileUploadConnector.getFile(any(), any())(any())).thenReturn(Future.successful(Some(inputStream)))

        val result = await(SUT.readFile(envelopeId, fileId))

        result should contain theSameElementsAs List("John,Smith,AB123456C,1990-02-21")
      }
    }
  }
}
