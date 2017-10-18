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

import java.time.LocalDate

import forms.MemberNameForm.Messages
import models.RasDate
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

trait DateValidator {

  val YEAR_FIELD_LENGTH: Int = 4

  val rasDateConstraint : Constraint[RasDate] = Constraint("dateOfBirth") ({
    x => {

      if (x.day.isEmpty && x.month.isEmpty && x.year.isEmpty)
        Invalid(Seq(ValidationError(Messages("error.mandatory", Messages("dob")))))

      else if (x.day.isEmpty)
        Invalid(Seq(ValidationError(Messages("error.mandatory", Messages("day")))))

      else if (x.month.isEmpty)
        Invalid(Seq(ValidationError(Messages("error.mandatory", Messages("month")))))

      else if (x.year.isEmpty)
        Invalid(Seq(ValidationError(Messages("error.mandatory", Messages("year")))))

      else if (!DateValidator.checkForNumber(x.day.getOrElse("0")))
        Invalid(Seq(ValidationError(Messages("error.date.non.number",Messages("day")))))

      else if (!DateValidator.checkForNumber(x.month.getOrElse("0")))
        Invalid(Seq(ValidationError(Messages("error.date.non.number",Messages("month")))))

      else if (!DateValidator.checkForNumber(x.year.getOrElse("0")))
        Invalid(Seq(ValidationError(Messages("error.date.non.number",Messages("year")))))

      else if (!DateValidator.checkDayRange(x.day.getOrElse("0"), x.month.getOrElse("0"))) {
        if(x.month.getOrElse("0").toInt == 2 && LocalDate.now.isLeapYear)
          Invalid(Seq(ValidationError(Messages("error.day.invalid.feb.leap"))))
        else if(x.month.getOrElse("0").toInt == 2 && !LocalDate.now.isLeapYear)
          Invalid(Seq(ValidationError(Messages("error.day.invalid.feb"))))
        else if(List(4,6,9,11).contains(x.month.getOrElse("0").toInt))
          Invalid(Seq(ValidationError(Messages("error.day.invalid.thirty"))))
        else
          Invalid(Seq(ValidationError(Messages("error.day.invalid"))))
      }

      else if (!DateValidator.checkMonthRange(x.month.getOrElse("0")))
        Invalid(Seq(ValidationError(Messages("error.month.invalid"))))

      else if (!DateValidator.checkYearLength(x.year.getOrElse("0")))
        Invalid(Seq(ValidationError(Messages("error.year.invalid.format"))))

      else {
        try {
          if (x.isInFuture)
            Invalid(Seq(ValidationError(Messages("error.dob.invalid.future"))))
          else
            Valid
        }
        catch {
          // $COVERAGE-OFF$Disabling highlighting by default until a workaround for https://issues.scala-lang.org/browse/SI-8596 is found
          case e: Exception => Valid
          // $COVERAGE-ON$

        }
      }
    }
  })

  def checkForNumber(value: String): Boolean = {
    value forall Character.isDigit
  }

  def checkDayRange(day: String, month: String): Boolean = {
    if (day forall Character.isDigit){
      if(month.toInt == 2 && LocalDate.now().isLeapYear)
        day.toInt > 0 && day.toInt < 30
      else if(month.toInt == 2 && !LocalDate.now().isLeapYear)
          day.toInt > 0 && day.toInt < 29
      else if(List(4,6,9,11).contains(month.toInt))
        day.toInt > 0 && day.toInt < 31
      else
        day.toInt > 0 && day.toInt < 32
    }
    else
      false
  }

  def checkMonthRange(month: String): Boolean = {
    if (month forall Character.isDigit)
      month.toInt > 0 && month.toInt < 13
    else
      false
  }

  def checkYearLength(year: String): Boolean = {
    if (year forall Character.isDigit)
      year.length == YEAR_FIELD_LENGTH
    else
      false
  }
}

object DateValidator extends DateValidator
