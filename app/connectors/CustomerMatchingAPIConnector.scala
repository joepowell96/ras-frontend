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

import config.WSHttp
import models._
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}

import scala.concurrent.Future

trait CustomerMatchingAPIConnector extends ServicesConfig{

  val http: HttpPost = WSHttp

  lazy val serviceUrl = baseUrl("customer-matching")
  lazy val environmentSuffix = getString("customer-matching-environment-suffix")

  def findMemberDetails(memberDetails: MemberDetails)(implicit hc: HeaderCarrier): Future[CustomerMatchingResponse] = {

    val matchingUri = s"$serviceUrl/$environmentSuffix"

    Logger.debug(s"[CustomerMatchingAPIConnector][findMemberDetails] Calling Customer Matching api at ${matchingUri}")

    http.POST[MemberDetails,CustomerMatchingResponse](matchingUri, memberDetails, Seq("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json" ))

  }

}

object CustomerMatchingAPIConnector extends CustomerMatchingAPIConnector