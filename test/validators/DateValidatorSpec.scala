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

package validators

import models.RasDate
import org.scalatest.Matchers
import uk.gov.hmrc.play.test.UnitSpec

class DateValidatorSpec extends UnitSpec with Matchers {

  "date validator" should{

    "return false when day is non digit" in {
      DateValidator.checkDayRange(RasDate(Some("a"),Some("1"),Some("1999"))) shouldBe false
    }

    "return false when month is non digit" in {
      DateValidator.checkMonthRange("a") shouldBe false
    }

    "return false when year is non digit" in {
      DateValidator.checkYearLength("a") shouldBe false
    }

    "return false when year is a non digit" in {
      DateValidator.checkDayRange(RasDate(Some("1"),Some("1"),Some("C"))) shouldBe false
    }

  }

}
