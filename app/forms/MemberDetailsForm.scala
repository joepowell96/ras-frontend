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

import helpers.helpers.I18nHelper
import models.{MemberDetails, RasDate}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import validators.NinoValidator

object MemberDetailsForm extends I18nHelper{

  val MAX_LENGTH = 99
  val NAME_REGEX = "^[a-zA-Z][a-zA-z\\s|'|-]*$"
  val NINO_SUFFIX_REGEX = "[A-D]"
  val TEMP_NINO = "TN"
  val YEAR_FIELD_LENGTH: Int = 4

  val ninoConstraint : Constraint[String] = Constraint("constraints.nino") ({
    text =>
      val ninoText = text.replaceAll("\\s", "")
      if (ninoText.length == 0){
        Invalid(Seq(ValidationError(Messages("error.mandatory", Messages("nino")))))
      }
      else if (ninoText.toUpperCase().startsWith(TEMP_NINO)){
        Invalid(Seq(ValidationError(Messages("error.nino.temporary"))))
      }
      else if (!NinoValidator.isValid(ninoText.toUpperCase())){
        Invalid(Seq(ValidationError(Messages("error.nino.invalid"))))
      }
      else if (!ninoText.takeRight(1).toUpperCase().matches(NINO_SUFFIX_REGEX)){
        Invalid(Seq(ValidationError(Messages("error.nino.invalid"))))
      }
      else {
        Valid
      }
  })

  val form = Form(
    mapping(
      "firstName" -> text
        .verifying(Messages("error.mandatory", Messages("first.name")), _.length > 0),
      "lastName" -> text
        .verifying(Messages("error.mandatory", Messages("last.name")), _.length > 0),
      "nino" -> text
        .verifying(ninoConstraint),
      "dateOfBirth" -> mapping(
        "day" -> optional(text),
        "month" -> optional(text),
        "year" -> optional(text)
      )(RasDate.apply)(RasDate.unapply))
    (MemberDetails.apply)(MemberDetails.unapply)
  )
}


