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
import models.UploadResponse
import play.Logger
import play.api.mvc.Action
import play.api.{Configuration, Environment, Play}
import services.UploadService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

trait FileUploadController extends RasController with PageFlowController {

  implicit val context: RasContext = RasContextImpl

  val fileUploadService: UploadService

  def get = Action.async {
    implicit request =>
      isAuthorised.flatMap {
        case Right(_) =>
          fileUploadService.createFileUploadUrl.flatMap { urlOption =>
            urlOption match {
              case Some(url) =>
                Logger.debug("[FileUploadController][get] successfully obtained a form url")
                sessionService.fetchRasSession().map {
                  case Some(session) =>
                    Logger.debug("[FileUploadController][get] session retrieved")
                    val errorReason = session.uploadResponse.getOrElse(UploadResponse("",None)).reason.getOrElse("")
                    Ok(views.html.file_upload(url,errorReason))
                  case _ =>
                    Logger.debug("[FileUploadController][get] no session retrieved")
                    Ok(views.html.file_upload(url,""))
                }
              case _ =>
                Logger.debug("[FileUploadController][get] failed to obtain a form url")
                Future.successful(Redirect(routes.GlobalErrorController.get()))
            }
          }.recover {
            case e: Throwable =>
              Logger.error("[FileUploadController][get] failed to obtain an envelope")
              Redirect(routes.GlobalErrorController.get)
          }
        case Left(resp) =>
          Logger.debug("[FileUploadController][get] user not authorised")
          resp
      }
  }

  def back = Action.async {
    implicit request =>
      isAuthorised.flatMap {
        case Right(userInfo) => Future.successful(previousPage("FileUploadController"))
        case Left(res) => res
      }
  }

  def uploadSuccess = Action.async { implicit request =>
    isAuthorised.flatMap {
      case Right(_) =>
        Logger.debug("[FileUploadController][uploadSuccess] upload has been successful")
        Future.successful(Ok(views.html.file_upload_successful()))
      case Left(resp) =>
        Logger.debug("[FileUploadController][uploadSuccess] user not authorised")
        resp
    }
  }

  def uploadError = Action.async { implicit request =>
    isAuthorised.flatMap {
      case Right(_) =>

        val errorCode = request.getQueryString("errorCode").getOrElse("")
        val errorResponse = errorCode match {
          case "400" => UploadResponse(errorCode,Some("File is empty"))
          case "413" => UploadResponse(errorCode,Some("File size exceeds limit to upload"))
          case _ => UploadResponse(errorCode,Some("File is corrupt"))
        }

        sessionService.cacheUploadResponse(errorResponse).flatMap {
          case Some(session) => Future.successful(Redirect(routes.FileUploadController.get()))
          case _ => Future.successful(Redirect(routes.GlobalErrorController.get()))
        }
      case Left(resp) =>
        Logger.debug("[FileUploadController][uploadError] user not authorised")
        resp
    }
  }

}

object FileUploadController extends FileUploadController {
  val authConnector: AuthConnector = FrontendAuthConnector
  override val userDetailsConnector: UserDetailsConnector = UserDetailsConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  val fileUploadService = UploadService
}
