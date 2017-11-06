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

import connectors.{FileUploadConnector, FileUploadFrontendConnector}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UploadService {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val envelopeIdPattern = "envelopes/([\\w\\d-]+)$".r.unanchored
  val fileUploadConnector: FileUploadConnector
  val fileUploadFrontendConnector: FileUploadFrontendConnector

  def uploadFile(): Future[Boolean] = {
    obtainUploadEnvelopeId.map { envelopeId =>
      envelopeId match {
        case Some(id) => true
        case _ => false
      }
    }
  }

  def obtainUploadEnvelopeId: Future[Option[String]] = {
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
  override val fileUploadFrontendConnector = FileUploadFrontendConnector
}