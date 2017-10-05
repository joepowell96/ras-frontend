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

import models.RasSession
import play.api.mvc.Result

trait PageFlowController extends RasController{

  val MEMBER_NAME = "MemberNameController"
  val MEMBER_NINO = "MemberNinoController"
  val MEMBER_DOB = "MemberDOBController"
  val RESULTS = "ResultsController"


  def nextPage(from: String, session: RasSession): Result = {
    forwardNavigation.get(from) match {
      case Some(redirect) => redirect(session)
      case None => NotFound
    }
  }

  val forwardNavigation: Map[String, RasSession => Result] = Map(
    MEMBER_NAME    -> { (session: RasSession) => Redirect(routes.MemberNinoController.get) },
    MEMBER_NINO    -> { (session: RasSession) => Redirect(routes.MemberDOBController.get) }
  )

  def previousPage(from: String, session: RasSession): Result = {
    backNavigation.get(from) match {
      case Some(redirect) => redirect(session)
      case None => NotFound
    }
  }

  val backNavigation: Map[String, RasSession => Result] = Map(
    MEMBER_NAME    -> {
      (session: RasSession) =>
        if(session.residencyStatusResult.currentYearResidencyStatus.isEmpty)
          Redirect(routes.ResultsController.noMatchFound())
        else
          Redirect(routes.ResultsController.matchFound())
    },
    MEMBER_NINO    -> { (session: RasSession) => Redirect(routes.MemberNameController.get) },
    MEMBER_DOB     -> { (session: RasSession) => Redirect(routes.MemberNinoController.get) },
    RESULTS        -> { (session: RasSession) => Redirect(routes.MemberDOBController.get) }
  )

}
