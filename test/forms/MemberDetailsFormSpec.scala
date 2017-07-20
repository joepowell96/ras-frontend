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
import models.RasDate
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class MemberDetailsFormSpec extends UnitSpec with I18nHelper with OneAppPerSuite {

  val dateOfBirth = RasDate(Some("1"),Some("1"),Some("1984"))
  val MAX_NAME_LENGTH = 35

  "Find member details form" should {

    "return no error when valid data is entered" in {

      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB123456C",
        "dateOfBirth" -> dateOfBirth
      )

      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.isEmpty)
    }

    "return an error when first name field is empty" in {

      val formData = Json.obj(
        "firstName" -> "",
        "lastName" -> "Esfandiari",
        "nino" -> "AB123456C",
        "dateOfBirth" -> dateOfBirth
      )
      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.contains(FormError("firstName", List(Messages("error.mandatory", Messages("first.name"))))))
    }

    "return an error when last name field is empty" in {

      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "",
        "nino" -> "AB123456C",
        "dateOfBirth" -> dateOfBirth
      )
      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.contains(FormError("lastName", List(Messages("error.mandatory", Messages("last.name"))))))
    }

    "return an error when nino field is empty" in {

      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "",
        "dateOfBirth" -> dateOfBirth
      )
      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.contains(FormError("nino", List(Messages("error.mandatory", Messages("nino"))))))
    }

    "return an error when invalid nino is passed" in {

      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "QQ322312B",
        "dateOfBirth" -> dateOfBirth
      )
      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.contains(FormError("nino", List(Messages("error.nino.invalid")))))
      assert(!validatedForm.errors.contains(FormError("nino", List(Messages("error.mandatory")))))
    }

    "return an error when invalid nino suffix is passed" in {

      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB322312E",
        "dateOfBirth" -> dateOfBirth
      )
      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.contains(FormError("nino", List(Messages("error.nino.invalid")))))
      assert(!validatedForm.errors.contains(FormError("nino", List(Messages("error.mandatory")))))
    }

    "return no error when nino with no suffix is passed" in {

      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB123456",
        "dateOfBirth" -> dateOfBirth
      )
      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.isEmpty)
    }

    "return error when first name is longer max allowed length" in {

      val formData = Json.obj(
        "firstName" -> "r" * (MAX_NAME_LENGTH + 1),
        "lastName" -> "Esfandiari",
        "nino" -> "AB123456",
        "dateOfBirth" -> dateOfBirth
      )
      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.contains(FormError("firstName", List(Messages("error.length",Messages("first.name"), MAX_NAME_LENGTH)))))
    }

    "return error when last name is longer max allowed length" in {

      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "e" * (MAX_NAME_LENGTH + 1),
        "nino" -> "AB123456",
        "dateOfBirth" -> dateOfBirth
      )
      val validatedForm = form.bind(formData)

      assert(validatedForm.errors.contains(FormError("lastName", List(Messages("error.length",Messages("last.name"), MAX_NAME_LENGTH)))))
    }


  }

}
