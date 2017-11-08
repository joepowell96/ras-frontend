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

import models.CallbackData
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.{Action, AnyContent}
import play.api.mvc.Results.{Accepted, BadRequest}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait FileProcessingController {

  def downloadFile(): Action[AnyContent] = Action.async {
    request =>
     /* val callbackData: Option[CallbackData] = request.body.asJson match {
        case Some(json) => Try(json.validate[CallbackData]) match {
          case Success(JsSuccess(payload, _)) => Some(payload)
          case Success(JsError(errors)) => None // Log errors
          case Failure(e) => None // Log Exception
        }
        case None => None // Log failure
      }
*/
//      if (!callbackData.isDefined)
        Future.successful(BadRequest(""))

      // Call connector to download file with envelopeId and fileId

      Future.successful(Accepted(""))
  }
}
