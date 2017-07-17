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

package forms

import forms.MemberDetailsForm._
import helpers.helpers.I18nHelper
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class MemberDetailsFormSpec extends UnitSpec with I18nHelper {

  "Find member derails form" should {

    "return no error when valid data is entered" in {

      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB123456C",
        "dateOfBirth" -> "1989-09-29"
      )

      val validatedForm = form.bind(formData)
    }

  }

}
