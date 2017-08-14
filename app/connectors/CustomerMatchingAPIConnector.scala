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

package connectors

import javax.inject.Inject

import models._
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomerMatchingAPIConnector @Inject() (ws: WSClient) extends ServicesConfig {

  lazy val serviceUrl = baseUrl("customer-matching")
  lazy val environmentSuffix = getString("customer-matching-environment-suffix")

  def findMemberDetails(memberDetails: MemberDetails)(implicit hc: HeaderCarrier): Future[CustomerMatchingResponse] = {

    val matchingUri = s"$serviceUrl/$environmentSuffix"

    val data = Json.toJson(memberDetails)

    val request: WSRequest = ws.url(matchingUri).withHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json")

    Logger.debug(s"[CustomerMatchingAPIConnector][findMemberDetails] Submitting request to Customer Matching.")

    request.post(data).map( response =>

      response.status match {

        case 200 =>
          Logger.debug(s"[CustomerMatchingAPIConnector][findMemberDetails] Match found.")
          response.json.as[MatchedResponse](MatchedResponse.ccmrReads)
        case 403 =>
          Logger.debug(s"[CustomerMatchingAPIConnector][findMemberDetails] Match not found.")
          NoMatchResponse
        case _ =>
          Logger.debug(s"[CustomerMatchingAPIConnector][findMemberDetails] Customer Matching failed ${response.status}" )
          ErrorResponse
      }

    )

  }



}

