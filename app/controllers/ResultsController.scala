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
import models.ResidencyStatusResult
import play.Logger
import play.api.{Configuration, Environment, Play}
import play.api.mvc.Action
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

object ResultsController extends ResultsController
{
  val authConnector: AuthConnector = FrontendAuthConnector
  override val userDetailsConnector: UserDetailsConnector = UserDetailsConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)

}

trait ResultsController extends RasController {

  implicit val context: RasContext = RasContextImpl

  def matchFound = Action.async {
    implicit request =>
      isAuthorised.flatMap {
        case Right(userInfo) =>
          sessionService.fetchRasSession() map { session =>
            session match {
              case Some(s) =>
                Logger.debug("[ResultsController][matchFound] Successfully retrieved ras session")
                Ok(views.html.match_found(s.residencyStatusResult))
              case _ =>
                Logger.error("[ResultsController][matchFound] failed to retrieve ras session")
                Redirect(routes.GlobalErrorController.get())
            }
          }
        case Left(res) => res
      }
  }

  def noMatchFound = Action.async {
    implicit request =>
      isAuthorised.flatMap {
        case Right(userInfo) =>
          sessionService.fetchRasSession() map { session =>
            session match {
              case Some(s) =>
                val name = s.memberDetails.firstName + " " + s.memberDetails.lastName
                val dateOfBirth = s.memberDetails.dateOfBirth.asLocalDate.toString("d MMMM yyyy")
                Logger.debug("[ResultsController][noMatchFound] Successfully retrieved ras session")
                Ok(views.html.match_not_found(name,dateOfBirth,s.memberDetails.nino))
              case _ =>
                Logger.error("[ResultsController][noMatchFound] failed to retrieve ras session")
                Redirect(routes.GlobalErrorController.get())
            }

          }

      case Left(res) => res
      }
  }

}
