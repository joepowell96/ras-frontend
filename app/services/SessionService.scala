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

package services


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

package services

import config.SessionCacheWiring
import models._
import play.api.Logger
import play.api.mvc.Request
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object SessionService extends SessionService

trait SessionService extends SessionCacheWiring {

  val RAS_SESSION_KEY = "ras_session"
  val cleanSession = RasSession(MemberDetails("","","",RasDate("","","")),ResidencyStatusResult("","","","","","",""))

  def getSession()(implicit request: Request[_], hc: HeaderCarrier): Future[Option[RasSession]] = {

    Logger.debug(s"[SessionService][getSession]")

    sessionCache.fetchAndGetEntry[RasSession](RAS_SESSION_KEY) map (rasSession => {
      rasSession
    })
  }



}
