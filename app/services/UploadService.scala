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

import java.util.UUID

import connectors.FileUploadConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait  UploadService extends ServicesConfig {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val serviceUrl = baseUrl("file-upload")
  lazy val serviceUrlSuffix = getString("file-upload-url-suffix")

  val fileUploadConnector: FileUploadConnector

  def createFileUploadUrl: Future[Option[String]] = {
    obtainUploadEnvelopeId.flatMap { envelopeIdOption =>
      envelopeIdOption match {
        case Some(envelopeId) => Future.successful(Some(s"$serviceUrl/${envelopeId}/files/${createFileId}"))
        case _ => Future.successful(None)
      }
    }
  }

  def obtainUploadEnvelopeId: Future[Option[String]] = {
    val envelopeIdPattern = "envelopes/([\\w\\d-]+)$".r.unanchored
    fileUploadConnector.getEnvelope().map { response =>
      response.header("Location") match {
        case Some(locationHeader) =>
          locationHeader match {
            case envelopeIdPattern(id) => Some(id)
            case _ => None
          }
        case _ => None
      }
    }
  }

  def createFileId: String = { UUID.randomUUID().toString }
}

object UploadService extends UploadService {
  override val fileUploadConnector = FileUploadConnector
}
