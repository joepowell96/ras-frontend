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

import connectors.{FileUploadConnector, FileUploadFrontendConnector}

import scala.concurrent.Future

trait FileUploadService {

  val fileUploadConnector: FileUploadConnector
  val fileUploadFrontendConnector: FileUploadFrontendConnector

  //pass in a multipart form to this
  def uploadFile(): Future[Boolean] = {
    ???
  }


}

object FileUploadService extends FileUploadService {
  override val fileUploadConnector = FileUploadConnector
  override val fileUploadFrontendConnector = FileUploadFrontendConnector
}
