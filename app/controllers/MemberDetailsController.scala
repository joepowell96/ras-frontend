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

import config.RasContextImpl
import connectors.{CustomerMatchingAPIConnector, ResidencyStatusAPIConnector}
import forms.MemberDetailsForm._
import models.{CustomerMatchingResponse, ResidencyStatus, ResidencyStatusResult}
import play.api.Logger
import play.api.mvc.Action
import uk.gov.hmrc.play.http.Upstream4xxResponse
import uk.gov.hmrc.time.TaxYearResolver

import scala.concurrent.Future

object MemberDetailsController extends MemberDetailsController{
  override val customerMatchingAPIConnector = CustomerMatchingAPIConnector
  override val residencyStatusAPIConnector = ResidencyStatusAPIConnector
}

trait MemberDetailsController extends RasController {

  val customerMatchingAPIConnector: CustomerMatchingAPIConnector
  val residencyStatusAPIConnector : ResidencyStatusAPIConnector

  val SCOTTISH = "scotResident"
  val NON_SCOTTISH = "otherUKResident"

  implicit val context: config.RasContext = RasContextImpl

  def get = Action.async { implicit request =>
    Future.successful(Ok(views.html.member_details(form)))
  }

  def post = Action.async { implicit request =>

    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.debug("[MemberDetailsController][post] Invalid form field passed")
        Future.successful(BadRequest(views.html.member_details(formWithErrors)))
      },
      memberDetails => {

        for {
          customerMatchingResponse <- customerMatchingAPIConnector.findMemberDetails(memberDetails)
            .recover{
              case e:Throwable =>
                Logger.error("[MemberDetailsController][getResult] Customer Matching failed")
                CustomerMatchingResponse(List())
            }

          rasResponse <- residencyStatusAPIConnector.getResidencyStatus(getResidencyStatusLink(customerMatchingResponse))
            .recover{
              case e:Throwable =>
                Logger.error("[MemberDetailsController][getResult] Residency status failed")
                ResidencyStatus("","")
            }
        } yield {

          if(customerMatchingResponse._links.isEmpty)
            Future.successful(Ok(views.html.global_error()))
          else if (rasResponse.currentYearResidencyStatus=="")
            Future.successful(Ok(views.html.global_error()))
          else {
            Logger.info("[MemberDetailsController][post] Match found")

            val currentYearResidencyStatus = getResidencyStatus(rasResponse.currentYearResidencyStatus)
            val nextYearResidencyStatus = getResidencyStatus(rasResponse.nextYearForecastResidencyStatus)
            val name = memberDetails.firstName + " " + memberDetails.lastName

            val residencyStatusResult = ResidencyStatusResult(
              currentYearResidencyStatus,
              nextYearResidencyStatus,
              TaxYearResolver.currentTaxYear.toString,
              (TaxYearResolver.currentTaxYear + 1).toString,
              name,
              memberDetails.dateOfBirth.asLocalDate.toString("d MMMM yyyy"),
              memberDetails.nino
            )

            //Redirect(routes.MatchFoundController.get())
            Future.successful(Ok(views.html.match_found(residencyStatusResult)))
          }

        }

      }.flatMap(identity)
    )
  }

  private def getResidencyStatusLink(customerMatchingResponse: CustomerMatchingResponse): String ={
    try{
      customerMatchingResponse._links.filter( _.name == "ras").head.href
    }catch{
      case e:Exception =>
        Logger.error("[MemberDetailsController][getResidencyStatusLink] " +
          "Problem with retrieving results from customer matching")
        ""
    }
  }

  private def getResidencyStatus(residencyStatus: String) : String = {
    if(residencyStatus == SCOTTISH)
      Messages("scottish.taxpayer")
    else if(residencyStatus == NON_SCOTTISH)
      Messages("non.scottish.taxpayer")
    else
      ""
  }
}

