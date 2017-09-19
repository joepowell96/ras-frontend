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

import config.FrontendAuthConnector
import connectors.UserDetailsConnector
import play.api.{Configuration, Environment, Play}
import play.api.mvc.Action
import uk.gov.hmrc.auth.core.AuthConnector

object SessionController extends SessionController {
  val authConnector: AuthConnector = FrontendAuthConnector
  override val userDetailsConnector: UserDetailsConnector = UserDetailsConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
}

trait SessionController extends RasController {

  def cleanAndRedirect(target: String) = Action.async {
    implicit request =>
      sessionService.resetRasSession().map {
        case Some(session) => Redirect(routes.MemberDetailsController.get())
        case _ => Redirect(routes.MemberDetailsController.get())
      }
  }

}
