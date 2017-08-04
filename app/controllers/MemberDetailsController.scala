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
import models.{CustomerMatchingResponse, Link, ResidencyStatus, ResidencyStatusResult}
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
  val RAS = "ras"
  val NO_MATCH = "noMatch"
  val EMPTY_CMR = CustomerMatchingResponse(List(Link(RAS,"")))
  val NO_MATCH_CMR = CustomerMatchingResponse(List(Link(RAS,NO_MATCH)))


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
              case e:Upstream4xxResponse if(e.upstreamResponseCode == FORBIDDEN) =>
                Logger.info("[MemberDetailsController][getResult] No match found from customer matching")
                NO_MATCH_CMR
              case e:Throwable =>
                Logger.error("[MemberDetailsController][getResult] Customer Matching failed")
                EMPTY_CMR
            }

          rasResponse <- residencyStatusAPIConnector.getResidencyStatus(extractResidencyStatusLink(customerMatchingResponse))
            .recover{
              case e:Throwable =>
                Logger.error("[MemberDetailsController][getResult] Residency status failed")
                ResidencyStatus("","")
            }
        } yield {

          val name = memberDetails.firstName + " " + memberDetails.lastName
          val dateOfBirth = memberDetails.dateOfBirth.asLocalDate.toString("d MMMM yyyy")

          if(extractResidencyStatusLink(customerMatchingResponse) == NO_MATCH)
            Future.successful(Ok(views.html.match_not_found(name, dateOfBirth, memberDetails.nino)))
          else if(extractResidencyStatusLink(customerMatchingResponse).isEmpty)
            Future.successful(Redirect(routes.GlobalErrorController.get))
          else if (rasResponse.currentYearResidencyStatus.isEmpty)
            Future.successful(Redirect(routes.GlobalErrorController.get))
          else if (extractResidencyStatus(rasResponse.currentYearResidencyStatus).isEmpty ||
                    extractResidencyStatus(rasResponse.nextYearForecastResidencyStatus).isEmpty)
            Future.successful(Redirect(routes.GlobalErrorController.get))
          else {

            Logger.info("[MemberDetailsController][post] Match found")

            val currentYearResidencyStatus = extractResidencyStatus(rasResponse.currentYearResidencyStatus)
            val nextYearResidencyStatus = extractResidencyStatus(rasResponse.nextYearForecastResidencyStatus)

            val residencyStatusResult = ResidencyStatusResult(
              currentYearResidencyStatus,
              nextYearResidencyStatus,
              TaxYearResolver.currentTaxYear.toString,
              (TaxYearResolver.currentTaxYear + 1).toString,
              name,
              dateOfBirth,
              memberDetails.nino
            )

            //Redirect(routes.MatchFoundController.get())
            Future.successful(Ok(views.html.match_found(residencyStatusResult)))
          }

        }

      }.flatMap(identity)
    )
  }

  private def extractResidencyStatusLink(customerMatchingResponse: CustomerMatchingResponse): String ={
    customerMatchingResponse._links.filter( _.name == RAS).head.href
  }

  private def extractResidencyStatus(residencyStatus: String) : String = {
    if(residencyStatus == SCOTTISH)
      Messages("scottish.taxpayer")
    else if(residencyStatus == NON_SCOTTISH)
      Messages("non.scottish.taxpayer")
    else
      ""
  }
}

