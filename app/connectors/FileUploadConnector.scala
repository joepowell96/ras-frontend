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

import config.WSHttp
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext

import scala.concurrent.Future

trait FileUploadConnector extends ServicesConfig {

  val http: HttpPost = WSHttp

  lazy val serviceUrl = baseUrl("file-upload")
  lazy val serviceUrlSuffix = getString("file-upload-url-suffix")

  def getEnvelope()(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val fileUploadUri = s"$serviceUrl/$serviceUrlSuffix"
    val requestBody = Json.parse("""{"callbackUrl": "ourCallbackUrl"}""".stripMargin)

    http.POST[JsValue, HttpResponse](fileUploadUri, requestBody,Seq())(implicitly, implicitly, hc, MdcLoggingExecutionContext.fromLoggingDetails(hc))
  }

//  private val responseHandler = new HttpReads[Option[String]] {
//
//    override def read(method: String, url: String, response: HttpResponse): Option[String] = {
//      response.status match {
//        case 201 => response.header("Location").map{ locationHeader =>Some(locationHeader)}.getOrElse(None)
//        case 400 => throw new Upstream4xxResponse("Envelope not created, with some reason message", 400, 400, response.allHeaders)
//        case _ => None
//      }
//    }
//
//  }

}

object FileUploadConnector extends FileUploadConnector
