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
import validators.{DateValidator, NinoValidator}

object MemberDetailsForm extends I18nHelper{

  val MAX_LENGTH = 35
  val NAME_REGEX = """^[a-zA-Z &`\-\'^]{1,35}$"""
  val NINO_SUFFIX_REGEX = "[A-D]"
  val TEMP_NINO = "TN"

  val ninoConstraint : Constraint[String] = Constraint("nino") ({
    text =>
      val ninoText = text.replaceAll("\\s", "")
      if (ninoText.length == 0)
        Invalid(Seq(ValidationError(Messages("error.mandatory", Messages("nino")))))
      else if (ninoText.length < 8 || ninoText.length > 9)
        Invalid(Seq(ValidationError(Messages("error.nino.length"))))
      else if (!NinoValidator.containsNoSpecialCharacters(ninoText.toUpperCase()))
        Invalid(Seq(ValidationError(Messages("error.nino.special.character"))))
      else if (!NinoValidator.isValid(ninoText.toUpperCase()))
        Invalid(Seq(ValidationError(Messages("error.nino.invalid"))))
      else
        Valid
  })

  val rasDateConstraint : Constraint[RasDate] = Constraint("dateOfBirth") ({
    dob => {
      try{
        if(dob.isInFuture)
          Invalid(Seq(ValidationError(Messages("error.dob.invalid.future"))))
        else
          Valid
      }
      catch { case e: Exception => Valid }
    }
  })

  val form = Form(
    mapping(
      "firstName" -> text
        .verifying(Messages("error.mandatory", Messages("first.name")), _.length > 0)
        .verifying(Messages("error.length", Messages("first.name"), MAX_LENGTH), _.length <= MAX_LENGTH)
        .verifying(Messages("error.name.invalid", Messages("first.name")), x => x.length == 0 || x.matches(NAME_REGEX)),
      "lastName" -> text
        .verifying(Messages("error.mandatory", Messages("last.name")), _.length > 0)
        .verifying(Messages("error.length", Messages("last.name"), MAX_LENGTH), _.length <= MAX_LENGTH)
        .verifying(Messages("error.name.invalid", Messages("last.name")), x => x.length == 0 || x.matches(NAME_REGEX)),
      "nino" -> text
        .verifying(ninoConstraint),
      "dateOfBirth" -> mapping(
        "day" -> text,
        "month" -> text,
        "year" -> text
      )(RasDate.apply)(RasDate.unapply)
        .verifying(Messages("error.date.non.number"), x =>
          DateValidator.checkForNumber(x.day) &&
          DateValidator.checkForNumber(x.month) &&
          DateValidator.checkForNumber(x.year))
        .verifying(Messages("error.day.invalid"), x => DateValidator.checkDayRange(x.day))
        .verifying(Messages("error.month.invalid"), x => DateValidator.checkMonthRange(x.month))
        .verifying(Messages("error.year.invalid.format"), x => DateValidator.checkYearLength(x.year))
        .verifying(rasDateConstraint)
    )
    (MemberDetails.apply)(MemberDetails.unapply)
  )
}


