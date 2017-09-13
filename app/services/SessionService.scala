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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SessionService extends SessionService


class SessionService extends SessionCacheWiring {

  val RAS_SESSION_KEY = "ras_session"
  val cleanSession = RasSession(MemberDetails("","","",RasDate("","","")),ResidencyStatusResult("","","","","","",""))

  def fetchRasSession()(implicit request: Request[_], hc: HeaderCarrier): Future[Option[RasSession]] = {
    sessionCache.fetchAndGetEntry[RasSession](RAS_SESSION_KEY) map (rasSession => rasSession)
  }

  def resetRasSession()(implicit request: Request[_], hc: HeaderCarrier): Future[Option[RasSession]] = {
    sessionCache.cache[RasSession](RAS_SESSION_KEY, cleanSession) map (cacheMap => Some(cleanSession))
  }


  def cacheMemberDetails(memberDetails: MemberDetails)(implicit request: Request[_], hc: HeaderCarrier): Future[Option[RasSession]] = {

    val result = sessionCache.fetchAndGetEntry[RasSession](RAS_SESSION_KEY) flatMap { currentSession =>
      sessionCache.cache[RasSession](RAS_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(memberDetails = memberDetails)
          case None => cleanSession.copy(memberDetails = memberDetails)
        }
      )
    }

    result.map(cacheMap => {
      cacheMap.getEntry[RasSession](RAS_SESSION_KEY)
    })
  }

  def fetchMemberDetails()(implicit request: Request[_], hc: HeaderCarrier): Future[Option[MemberDetails]] = {
    sessionCache.fetchAndGetEntry[RasSession](RAS_SESSION_KEY).map { currentSession =>
      currentSession.map {
        _.memberDetails
      }
    }
  }

  def cacheResidencyStatusResult(residencyStatusResult: ResidencyStatusResult)(implicit request: Request[_], hc: HeaderCarrier): Future[Option[RasSession]] = {

    val result = sessionCache.fetchAndGetEntry[RasSession](RAS_SESSION_KEY) flatMap { currentSession =>
      sessionCache.cache[RasSession](RAS_SESSION_KEY,
        currentSession match {
          case Some(returnedSession) => returnedSession.copy(residencyStatusResult = residencyStatusResult)
          case None => cleanSession.copy(residencyStatusResult = residencyStatusResult)
        }
      )
    }

    result.map(cacheMap => {
      cacheMap.getEntry[RasSession](RAS_SESSION_KEY)
    })
  }

  def fetchResidencyStatusResult()(implicit request: Request[_], hc: HeaderCarrier): Future[Option[ResidencyStatusResult]] = {
    sessionCache.fetchAndGetEntry[RasSession](RAS_SESSION_KEY).map { currentSession =>
      currentSession.map {
        _.residencyStatusResult
      }
    }
  }


}

