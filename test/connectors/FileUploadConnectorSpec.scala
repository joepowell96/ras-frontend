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

import java.io.{BufferedReader, InputStreamReader}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.util.ByteString
import config.WSHttp
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.JsValue
import play.api.libs.ws.{DefaultWSResponseHeaders, StreamedResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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

    "return an StreamedResponse from File-Upload service" in {

      implicit val system = ActorSystem()
      implicit val materializer = ActorMaterializer()

      val envelopeId: String = "0b215e97-11d4-4006-91db-c067e74fc653"
      val fileId: String = "file-id-1"

      val streamResponse:StreamedResponse = StreamedResponse(DefaultWSResponseHeaders(200, Map("CONTENT_TYPE" -> Seq("application/octet-stream"))),
        Source.apply[ByteString](Seq(ByteString("Test"),  ByteString("\r\n"), ByteString("Passed")).to[scala.collection.immutable.Iterable]) )

      when(mockWsHttp.buildRequestWithStream(any())(any())).thenReturn(Future.successful(streamResponse))

      val values = List("Test", "Passed")

      val result = await(TestConnector.getFile(envelopeId, fileId))

      val reader = new BufferedReader(new InputStreamReader(result.get))

      (Iterator continually reader.readLine takeWhile (_ != null) toList) should contain theSameElementsAs List("Test", "Passed")

      //for(i <- 0 until 2) values += reader.readLine()

//      reader.lines()

//      await(result.runWith(Sink.foreach[ByteString]{(string) =>
//        values += string.utf8String
//      }))

     // values.toList should contain theSameElementsAs List("Test", "Passed")
    }

//    "return a 404 exception from File-Upload service when a file has not been found with the fileId provided" in {
//      val envelopeId: String = "0b215e97-11d4-4006-91db-c067e74fc653"
//      val fileId: String = "file-id-1"
//
//      when(mockWsHttp.buildRequestWithStream(any())(any())).thenReturn(Future.failed(Http))
//
//      TestConnector.getFile(envelopeId, fileId)
//    }


  }

}
