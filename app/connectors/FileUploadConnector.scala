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

import java.io.InputStream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import config.WSHttp
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext

import scala.concurrent.Future

trait FileUploadConnector extends ServicesConfig {

  val http: HttpPost
  val wsHttp: WSHttp

  lazy val serviceUrl = baseUrl("file-upload")
  lazy val serviceUrlSuffix = getString("file-upload-url-suffix")

  def createEnvelope()(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val requestBody = Json.parse("""{"callbackUrl": "http://localhost:9673/relief-at-source/bulk/upload-callback"}""".stripMargin)

    http.POST[JsValue, HttpResponse](
      s"$serviceUrl/$serviceUrlSuffix", requestBody, Seq()
    )(implicitly, implicitly, hc, MdcLoggingExecutionContext.fromLoggingDetails(hc))

  }

  def getFile(envelopeId: String, fileId: String)(implicit hc: HeaderCarrier): Future[Option[InputStream]] = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    Logger.debug(s"Get to file-upload with URI : /file-upload/envelopes/${envelopeId}/files/${fileId}/content")
    wsHttp.buildRequestWithStream(s"$serviceUrl/$serviceUrlSuffix/${envelopeId}/files/${fileId}/content").map { res =>
      Some(res.body.runWith(StreamConverters.asInputStream()))
    } recover {
      case ex: Throwable => {
        Logger.error("Exception thrown while retrieving file / converting to InputStream.", ex)
        None
      }
    }
  }
}

object FileUploadConnector extends FileUploadConnector {

  override val http: HttpPost = WSHttp
  override val wsHttp: WSHttp = WSHttp
}
