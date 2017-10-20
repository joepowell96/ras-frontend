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

package controllers

import config.{FrontendAuthConnector, RasContext, RasContextImpl}
import connectors.UserDetailsConnector
import play.Logger
import play.api.{Configuration, Environment, Play}
import play.api.mvc.Action
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

trait FileUploadController extends RasController {

  implicit val context: RasContext = RasContextImpl

  def get = Action.async {
    implicit request =>
      isAuthorised.flatMap {
        case Right(_) =>
          Future.successful(Ok(views.html.file_upload()))
        case Left(resp) =>
          Logger.debug("[FileUploadController][get] user Not authorised")
          resp
      }
  }

}

object FileUploadController extends FileUploadController {
  val authConnector: AuthConnector = FrontendAuthConnector
  override val userDetailsConnector: UserDetailsConnector = UserDetailsConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
}
