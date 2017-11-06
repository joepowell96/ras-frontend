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
import play.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UploadService {

  implicit val hc: HeaderCarrier = HeaderCarrier()


  val fileUploadConnector: FileUploadConnector
  val fileUploadFrontendConnector: FileUploadFrontendConnector

  def uploadFile(data: Array[Byte]): Future[Boolean] = {
    obtainUploadEnvelopeId.flatMap { envelopeIdOption =>
      envelopeIdOption match {
        case Some(envelopeId) =>
          fileUploadFrontendConnector.uploadFile(data, envelopeId, createFileId).map { result =>
            result.status match {
              case 200 =>
                Logger.debug("[UploadService][uploadFile] File uploaded successfully")
                true
              case _ =>
                Logger.debug("[UploadService][uploadFile] File upload failed")
                false
            }
          }
        case _ => Future.successful(false)
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
  override val fileUploadFrontendConnector = FileUploadFrontendConnector
}
