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

import helpers.RandomNino
import models._
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => meq, _}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.Helpers._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}

import scala.concurrent.Future

class CustomerMatchingAPIConnectorSpec @Inject() (customerMatchingAPIConnector: CustomerMatchingAPIConnector)extends PlaySpec with OneAppPerSuite with MockitoSugar with ServicesConfig {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockRequest = mock[WSRequest]

//  "Customer Matching API connector" should {
//
//    lazy val serviceBase = s"${baseUrl("customer-matching")}/match"
//
//    "send a post request to customer matching service" in {
//
//      val memberDetails = MemberDetails(RandomNino.generate, "Ramin", "Esfandiari", RasDate("1","1","1999"))
//
//      val expectedResponse = CustomerMatchingResponse(List(
//        Link("self","/customer/matched/633e0ee7-315b-49e6-baed-d79c3dffe467"),
//        Link("relief-at-source","/relief-at-source/customer/633e0ee7-315b-49e6-baed-d79c3dffe467/residency-status")))
//
//
//      when(mockRequest.post(memberDetails)).thenReturn(Future.successful(expectedResponse))
//
//      val result = await(customerMatchingAPIConnector.findMemberDetails(memberDetails))
//
////      verify(TestConnector.http).POST(meq(serviceBase),meq(memberDetails))(any(), any(), any())
//      result mustBe expectedResponse
//
//    }
//
//
//  }

}
