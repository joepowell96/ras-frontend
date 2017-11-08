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

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.util.ByteString
import config.WSHttp
import models.CallbackData
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.JsValue
import play.api.libs.ws.{DefaultWSResponseHeaders, StreamedResponse, WSResponse, WSResponseHeaders}
import play.api.test.Helpers._
import shapeless.Lazy.apply
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost, HttpResponse}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class FileUploadConnectorSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with ServicesConfig with WSHttp{

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockWsHttp = mock[WSHttp]

  object TestConnector extends FileUploadConnector {
    override val http: HttpPost = mock[HttpPost]
    override val httpGet: WSHttp = mockWsHttp
  }

  "File upload connector" should {

    "send a post request to file upload service" in {
      when(TestConnector.http.POST[JsValue, Option[String]](any(),any(),any())(any(),any(),any(),any())).thenReturn(Future.successful(Some("")))
      val result = await(TestConnector.getEnvelope)
      result shouldBe Some("")
    }

  }

  ///file-upload/envelopes/{ENVELOPEID}/files/{FILEID}/content

  "getFile" should {

    "return an InputStream from File-Upload service" in {

      val envelopeId: String = "0b215e97-11d4-4006-91db-c067e74fc653"
      val fileId: String = "file-id-1"



      val streamResponse:StreamedResponse = StreamedResponse(DefaultWSResponseHeaders(200, Map("CONTENT_TYPE" -> Seq("application/octet-stream"))),
        Source.apply[ByteString](Seq(ByteString("Test"), ByteString("Passed")).to[scala.collection.immutable.Iterable]) )

      when(mockWsHttp.buildRequestWithStream(any())(any())).thenReturn(Future.successful(streamResponse))

      TestConnector.getFile(envelopeId, fileId).body

//     result should contain theSameElementsAs List("Boo", "hoo")// productIterator.toList should contain theSameElementsAs List("Test", "Passed")

    }

  }

}
