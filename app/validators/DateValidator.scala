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

trait DateValidator {
  def checkForNumber(value: String): Boolean
  def checkDayRange(day: String): Boolean
  def checkMonthRange(month: String): Boolean
  def checkYearLength(year: String): Boolean
}

object DateValidator extends DateValidator{

  val YEAR_FIELD_LENGTH: Int = 4

  override def checkForNumber(value: String): Boolean = {
    value forall Character.isDigit
  }

  override def checkDayRange(day: String): Boolean = {
    if(day.isEmpty)
      false
    else if (day forall Character.isDigit)
      day.toInt > 0 && day.toInt < 32
    else
      true
  }

  override def checkMonthRange(month: String): Boolean = {
    if(month.isEmpty)
      false
    else if (month forall Character.isDigit)
      month.toInt > 0 && month.toInt < 13
    else
      true
  }

  override def checkYearLength(year: String): Boolean = {
    if(year.isEmpty)
      false
    else if (year forall Character.isDigit)
      year.length == YEAR_FIELD_LENGTH
    else
      true
  }
}