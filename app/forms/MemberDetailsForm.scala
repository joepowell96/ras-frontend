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

  val MAX_LENGTH = 35
  val NAME_REGEX = """^[a-zA-Z &`\-\'^]{1,35}$"""
  val NINO_SUFFIX_REGEX = "[A-D]"
  val TEMP_NINO = "TN"
  val YEAR_FIELD_LENGTH: Int = 4

  val ninoConstraint : Constraint[String] = Constraint("nino") ({
    text =>
      val ninoText = text.replaceAll("\\s", "")
      if (ninoText.length == 0)
        Invalid(Seq(ValidationError(Messages("error.mandatory", Messages("nino")))))
      else if (!NinoValidator.isValid(ninoText.toUpperCase()))
        Invalid(Seq(ValidationError(Messages("error.nino.invalid"))))
      else
        Valid
  })

  val rasDateConstraint : Constraint[MemberDetails] = Constraint("dateOfBirth") ({
    memberDetails => {

      if (memberDetails.dateOfBirth.day.isEmpty ||
          memberDetails.dateOfBirth.month.isEmpty ||
          memberDetails.dateOfBirth.year.isEmpty)
        Invalid(ValidationError(Messages("error.mandatory", Messages("dob"))))
      else
        Valid
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
        .verifying(Messages("error.date.non.number"), x => checkForNumber(x.day) && checkForNumber(x.month) && checkForNumber(x.year))
        .verifying(Messages("error.day.invalid"), x => checkDayRange(x.day))
        .verifying(Messages("error.month.invalid"), x => checkMonthRange(x.month))
        .verifying(Messages("error.year.invalid.format"), x => checkYearLength(x.year))
    )
    (MemberDetails.apply)(MemberDetails.unapply)
      .verifying(rasDateConstraint)
  )

  def checkForNumber(value: String): Boolean = {
    value forall Character.isDigit
  }

  def checkDayRange(day: String): Boolean = {
    if(day.isEmpty)
      false
    else if (day forall Character.isDigit)
      day.toInt > 0 && day.toInt < 32
    else
      true
  }

  def checkMonthRange(month: String): Boolean = {
    if(month.isEmpty)
      false
    else if (month forall Character.isDigit)
      month.toInt > 0 && month.toInt < 13
    else
      true
  }

  def checkYearLength(year: String): Boolean = {
    if(year.isEmpty)
      false
    else if (year forall Character.isDigit)
      year.length == YEAR_FIELD_LENGTH
    else
      true
  }


}


