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

import config.{ApplicationConfig, RasContextImpl}
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future
import forms.Forms.memberForm
import helpers.I18nHelper
import play.api.Logger

object StartPageController extends StartPageController

trait StartPageController extends FrontendController with I18nHelper {

  implicit val context: config.RasContext = RasContextImpl

  def get = Action.async { implicit request =>
		Future.successful(Ok(views.html.start_page()))
  }

  def present = Action.async { implicit request =>
    Future.successful(Ok(views.html.Member_details_page(memberForm)))
  }

  def submit = Action.async { implicit request =>
    memberForm.bindFromRequest.fold(
     hasErrors => {
       Future.successful(BadRequest(views.html.Member_details_page(hasErrors)))
     },
      memberDetails => {
        Logger.debug(memberDetails.firstName)
        println(Console.YELLOW, memberDetails.firstName)
        println(Console.WHITE)
        Future.successful(Redirect(routes.StartPageController.get()))
      }
    )
    Future.successful(Ok(views.html.start_page()))
  }
}
