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
import connectors.{CustomerMatchingAPIConnector, ResidencyStatusAPIConnector, UserDetailsConnector}
import forms.MemberDetailsForm._
import models._
import play.api.{Configuration, Environment, Logger, Play}
import play.api.mvc.Action
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.Upstream4xxResponse
import uk.gov.hmrc.time.TaxYearResolver

import scala.concurrent.Future

object MemberDetailsController extends MemberDetailsController {
  override val customerMatchingAPIConnector = CustomerMatchingAPIConnector
  override val residencyStatusAPIConnector = ResidencyStatusAPIConnector
  val authConnector: AuthConnector = FrontendAuthConnector
  override val userDetailsConnector: UserDetailsConnector = UserDetailsConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
}

trait MemberDetailsController extends RasController {

  implicit val context: RasContext = RasContextImpl
  val customerMatchingAPIConnector: CustomerMatchingAPIConnector
  val residencyStatusAPIConnector : ResidencyStatusAPIConnector

  val SCOTTISH = "scotResident"
  val NON_SCOTTISH = "otherUKResident"
  val RAS = "ras"
  val NO_MATCH = "noMatch"
  val EMPTY_CMR = CustomerMatchingResponse(List(Link(RAS,"")))
  val NO_MATCH_CMR = CustomerMatchingResponse(List(Link(RAS,NO_MATCH)))

  def get = Action.async {
    implicit request =>
      isAuthorised.flatMap {
        case Right(userInfo) =>
          Logger.debug("[MemberDetailsController][get] user authorised")
          sessionService.fetchMemberDetails() map {
            case Some(md) => Ok(views.html.member_details(form.fill(md)))
            case _ => Ok(views.html.member_details(form))
          }
        case Left(resp) =>
          Logger.debug("[MemberDetailsController][get] user Not authorised")
          resp
      }
  }

  def post = Action.async { implicit request =>
    isAuthorised.flatMap{ case Right(userInfo) =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.debug("[MemberDetailsController][post] Invalid form field passed")
        Future.successful(BadRequest(views.html.member_details(formWithErrors)))
      },
      memberDetails => {
        Logger.debug("[MemberDetailsController][post] valid form")

        sessionService.cacheMemberDetails(memberDetails.asMemberDetailsWithLocalDate) flatMap {
          case Some(session) => {

            customerMatchingAPIConnector.findMemberDetails(memberDetails).flatMap { uuid =>

              if (!uuid.isDefined) {
                Logger.info("[MemberDetailsController][post] UUID not contained in the Location header")
                Future.successful(Redirect(routes.GlobalErrorController.get))
              }

              residencyStatusAPIConnector.getResidencyStatus(uuid.get).map { rasResponse =>

                val name = memberDetails.firstName + " " + memberDetails.lastName
                val dateOfBirth = memberDetails.asMemberDetailsWithLocalDate.dateOfBirth.toString("d MMMM yyyy")
                val cyResidencyStatus = extractResidencyStatus(rasResponse.currentYearResidencyStatus)
                val nyResidencyStatus = extractResidencyStatus(rasResponse.nextYearForecastResidencyStatus)

                if (cyResidencyStatus.isEmpty || nyResidencyStatus.isEmpty) {
                  Logger.info("[MemberDetailsController][post] An unknown residency status was returned")
                  Redirect(routes.GlobalErrorController.get)
                }
                else {

                  Logger.info("[MemberDetailsController][post] Match found")

                  val residencyStatusResult = ResidencyStatusResult(cyResidencyStatus, nyResidencyStatus,
                    TaxYearResolver.currentTaxYear.toString, (TaxYearResolver.currentTaxYear + 1).toString,
                    name, dateOfBirth, memberDetails.nino)

                  sessionService.cacheResidencyStatusResult(residencyStatusResult)

                  Redirect(routes.ResultsController.matchFound())
                }
              }.recover {
                case e: Throwable =>
                  Logger.error("[MemberDetailsController][getResult] Residency status failed")
                  Redirect(routes.GlobalErrorController.get)
              }
            }.recover {
              case e: Upstream4xxResponse if (e.upstreamResponseCode == FORBIDDEN) =>
                Logger.info("[MemberDetailsController][getResult] No match found from customer matching")
                Redirect(routes.ResultsController.noMatchFound())
              case e: Throwable =>
                Logger.error(s"[MemberDetailsController][getResult] Customer Matching failed: ${e.getMessage}")
                Redirect(routes.GlobalErrorController.get)
            }
          }
          case _ => Future.successful(Redirect(routes.GlobalErrorController.get()))
        }
      }
    )
    case Left(res) => res
  }
  }



  private def extractResidencyStatusLink(customerMatchingResponse: CustomerMatchingResponse): String ={
    try{
      customerMatchingResponse._links.filter( _.name == RAS).head.href
    } catch { case e:Exception => ""}
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

