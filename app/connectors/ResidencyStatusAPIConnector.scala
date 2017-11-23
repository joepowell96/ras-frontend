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
import models.ResidencyStatus
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpReads}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext

trait ResidencyStatusAPIConnector extends ServicesConfig {

  val http: HttpGet = WSHttp

  lazy val serviceUrl = baseUrl("relief-at-source")
  lazy val residencyStatusUrl = getString("residency-status-url")

  def getResidencyStatus(uuid: String)(implicit hc: HeaderCarrier): Future[ResidencyStatus] = {

    val rasUri = s"$serviceUrl/${String.format(residencyStatusUrl, uuid)}"

    val headerCarrier = hc.withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json" )

    Logger.debug(s"[ResidencyStatusAPIConnector][getResidencyStatus] Calling Residency Status api")

    http.GET[ResidencyStatus](rasUri)(implicitly[HttpReads[ResidencyStatus]],hc = headerCarrier, MdcLoggingExecutionContext.fromLoggingDetails(headerCarrier))
  }

}

object ResidencyStatusAPIConnector extends ResidencyStatusAPIConnector
