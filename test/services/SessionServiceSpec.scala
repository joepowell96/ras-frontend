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

import helpers.RandomNino
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SessionServiceSpec extends UnitSpec with OneServerPerSuite with ScalaFutures with MockitoSugar {

  val mockSessionCache = mock[SessionCache]

  val name = MemberName("John", "Johnson")
  val nino = MemberNino(RandomNino.generate)
  val memberDob = MemberDateOfBirth(RasDate(Some("12"),Some("12"), Some("2012")))
  val memberDetails = MemberDetails(name,RandomNino.generate,RasDate(Some("1"),Some("1"),Some("1999")))
  val rasSession = RasSession(name,nino,memberDob,ResidencyStatusResult("","","","","","",""))

  object TestSessionService extends SessionService {
    override def sessionCache: SessionCache = mockSessionCache
  }

  "Session service" should {

    "fetch Name" when {
      "member name page is loaded" in{
        when(mockSessionCache.fetchAndGetEntry[RasSession](any())(any(), any())).thenReturn(Future.successful(Some(rasSession)))
        val result = Await.result(TestSessionService.fetchName()(FakeRequest(), HeaderCarrier()), 10 seconds)
        result shouldBe Some(memberDetails)
      }
    }

    "cache Name" when {
      "submitted when no session is retrieved" in {
        when(mockSessionCache.fetchAndGetEntry[RasSession](any())(any(), any())).thenReturn(Future.successful(None))
        val json = Json.toJson[RasSession](rasSession.copy(name = name))
        when(mockSessionCache.cache[RasSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("ras_session" -> json))))
        val result = Await.result(TestSessionService.cacheName(name)(FakeRequest(), HeaderCarrier()), 10 seconds)
        result shouldBe Some(rasSession.copy(name = name))
      }
    }

    "cache residency status" when {
      "member details is submitted via the form when no returned session" in {
        when(mockSessionCache.fetchAndGetEntry[RasSession](any())(any(), any())).thenReturn(Future.successful(None))
        val rd = ResidencyStatusResult("uk","uk","2000","2001","Jack","1-1-1999",RandomNino.generate)
        val json = Json.toJson[RasSession](rasSession.copy(residencyStatusResult = rd))
        when(mockSessionCache.cache[RasSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("ras_session" -> json))))
        val result = Await.result(TestSessionService.cacheResidencyStatusResult(rd)(FakeRequest(), HeaderCarrier()), 10 seconds)
        result shouldBe Some(rasSession.copy(residencyStatusResult = rd))
      }
      "member details is submitted via the form when some returned session" in {
        when(mockSessionCache.fetchAndGetEntry[RasSession](any())(any(), any())).thenReturn(Future.successful(Some(rasSession)))
        val rd = ResidencyStatusResult("uk","uk","2000","2001","Jack","1-1-1999",RandomNino.generate)
        val json = Json.toJson[RasSession](rasSession.copy(residencyStatusResult = rd))
        when(mockSessionCache.cache[RasSession](any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("ras_session" -> json))))
        val result = Await.result(TestSessionService.cacheResidencyStatusResult(rd)(FakeRequest(), HeaderCarrier()), 10 seconds)
        result shouldBe Some(rasSession.copy(residencyStatusResult = rd))
      }
    }

    "fetch ras session" when {
      "requested" in {
        when(mockSessionCache.fetchAndGetEntry[RasSession](any())(any(), any())).thenReturn(Future.successful(Some(rasSession)))
        val result = Await.result(TestSessionService.fetchRasSession()(FakeRequest(), HeaderCarrier()), 10 seconds)
        result shouldBe Some(rasSession)
      }
    }

    "reset ras session" when {
      "requested" in {
        val result = Await.result(TestSessionService.resetRasSession()(FakeRequest(), HeaderCarrier()), 10 seconds)
        result  shouldBe Some(SessionService.cleanSession)
      }
    }

  }

}
