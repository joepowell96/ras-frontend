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
import helpers.RandomNino
import helpers.helpers.I18nHelper
import models.RasDate
import org.joda.time.LocalDate
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class MemberDetailsFormSpec extends UnitSpec with I18nHelper with OneAppPerSuite {

  val dateOfBirth = RasDate("1","1","1984")
  val MAX_NAME_LENGTH = 35

  "Find member details form" should {

    "return no error when valid data is entered" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "return an error when first name field is empty" in {
      val formData = Json.obj(
        "firstName" -> "",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("firstName", List(Messages("error.mandatory", Messages("first.name"))))))
    }

    "return an error when last name field is empty" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("lastName", List(Messages("error.mandatory", Messages("last.name"))))))
    }

    "return an error when nino field is empty" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "",
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("nino", List(Messages("error.mandatory", Messages("nino"))))))
    }

    "return an error when invalid nino is passed" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "QQ322312B",
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("nino", List(Messages("error.nino.invalid")))))
      assert(!validatedForm.errors.contains(FormError("nino", List(Messages("error.mandatory")))))
    }

    "return an error when invalid nino suffix is passed" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB322312E",
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("nino", List(Messages("error.nino.invalid")))))
      assert(!validatedForm.errors.contains(FormError("nino", List(Messages("error.mandatory")))))
    }

    "return no error when nino with no suffix is passed" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB123456",
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "return error when first name is longer max allowed length" in {
      val formData = Json.obj(
        "firstName" -> "r" * (MAX_NAME_LENGTH + 1),
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("firstName", List(Messages("error.length",Messages("first.name"), MAX_NAME_LENGTH)))))
    }

    "return error when last name is longer max allowed length" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "e" * (MAX_NAME_LENGTH + 1),
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("lastName", List(Messages("error.length",Messages("last.name"), MAX_NAME_LENGTH)))))
    }

    "return no error when first name is of minimum allowed length" in {
      val formData = Json.obj(
        "firstName" -> "r",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "return no error when last name is of minimum allowed length" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "E",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "return no error when first name max allowed length" in {
      val formData = Json.obj(
        "firstName" -> "r" * MAX_NAME_LENGTH,
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "return no error when last name max allowed length" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "E" * MAX_NAME_LENGTH,
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "return an error when name contains a digit" in {
      val formData1 = Json.obj(
        "firstName" -> "Ramin1",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm1 = form.bind(formData1)
      val formData2 = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiar3i",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm2 = form.bind(formData2)
      assert(validatedForm1.errors.contains(FormError("firstName", List(Messages("error.name.invalid", Messages("first.name"))))))
      assert(validatedForm2.errors.contains(FormError("lastName", List(Messages("error.name.invalid", Messages("last.name"))))))
    }

    "allow apostrophes" in {
      val formData1 = Json.obj(
        "firstName" -> "R'n",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm1 = form.bind(formData1)
      val formData2 = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfa'ndiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm2 = form.bind(formData2)
      assert(validatedForm1.errors.isEmpty)
      assert(validatedForm2.errors.isEmpty)
    }

    "allow hyphens" in {
      val formData = Json.obj(
        "firstName" -> "Ram-in",
        "lastName" -> "Esfa-ndiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "disallow other special characters" in {
      val formData1 = Json.obj(
        "firstName" -> "Ra$min",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm1 = form.bind(formData1)
      val formData2 = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfan@diari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm2 = form.bind(formData2)
      assert(validatedForm1.errors.contains(FormError("firstName", List(Messages("error.name.invalid", Messages("first.name"))))))
      assert(validatedForm2.errors.contains(FormError("lastName", List(Messages("error.name.invalid", Messages("last.name"))))))
    }

    "allow whitespace" in {
      val formData = Json.obj(
        "firstName" -> "Ra min",
        "lastName" -> "Esfand iari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> dateOfBirth)
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "return an error when date is empty" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("","",""))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.mandatory", Messages("dob"))))))
    }

    "return an error when day field is empty" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("","1","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.mandatory", Messages("day"))))))
    }

    "return an error when month field is empty" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1","","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.mandatory", Messages("month"))))))
    }

    "return an error when year field is empty" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1","1",""))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.mandatory", Messages("year"))))))
    }

    "return an error when day is a non number" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("a","1","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.date.non.number",Messages("day"))))))
    }

    "return an error when month is a non number" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1","a","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.date.non.number",Messages("month"))))))
    }

    "return an error when year is a non number" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1","2","198asasas4"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.date.non.number", Messages("year"))))))
    }

    "return an error when day is smaller than range" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("0","1","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.day.invalid")))))
    }

    "return an error when day is greater than range" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("32","1","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.day.invalid")))))
    }

    "return no error when day is in range" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("15","1","1984"))
      val validatedForm = form.bind(formData)
      assert(!validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.day.invalid")))))
    }

    "return an error when month is smaller than range" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1","0","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.month.invalid")))))
    }

    "return an error when month is greater than range" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1","13","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.month.invalid")))))
    }

    "return no error when month is in range" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("15","1","1984"))
      val validatedForm = form.bind(formData)
      assert(!validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.month.invalid")))))
    }

    "return an error when year has invalid format" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1","2","19842"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.year.invalid.format")))))
    }

    "return an error when year has only 3 digits" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("1","2","142"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.year.invalid.format")))))
    }

    "return no error when nino with spaces is passed" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB 12 34 56 C",
        "dateOfBirth" -> RasDate("1","2","1422"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

    "return an error when date of birth is in the future" in {
      val futureDate = LocalDate.now()
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate(futureDate.getDayOfMonth.toString,
                                 futureDate.getMonthOfYear.toString,
                                 (futureDate.getYear + 1).toString))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.dob.invalid.future")))))
    }

    "return an error when nino with wrong number of characters and digits is passed" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB1234 56 56 C",
        "dateOfBirth" -> RasDate("1","2","1422"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("nino", List(Messages("error.nino.length")))))
    }

    "return an error when nino with special character is passed" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> "AB£56 56 C",
        "dateOfBirth" -> RasDate("1","2","1422"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("nino", List(Messages("error.nino.special.character")))))
    }

    "return error when february and day larger than 29" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("30","2","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.day.invalid.feb")))))
    }

    "return error when April and day larger than 30" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("31","4","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.day.invalid.thirty")))))
    }

    "return error when June and day larger than 30" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("31","6","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.day.invalid.thirty")))))
    }

    "return error when September and day larger than 30" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("31","9","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.day.invalid.thirty")))))
    }

    "return error when November and day larger than 30" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("31","11","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.contains(FormError("dateOfBirth", List(Messages("error.day.invalid.thirty")))))
    }

    "return no error when January and day larger than 30" in {
      val formData = Json.obj(
        "firstName" -> "Ramin",
        "lastName" -> "Esfandiari",
        "nino" -> RandomNino.generate,
        "dateOfBirth" -> RasDate("31","1","1984"))
      val validatedForm = form.bind(formData)
      assert(validatedForm.errors.isEmpty)
    }

  }

}
