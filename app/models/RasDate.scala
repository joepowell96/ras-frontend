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

package models

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}

case class RasDate(day: String, month: String, year: String){

  def asLocalDate: LocalDate = {
    new LocalDate(2000, 12, 12)
  }

//  def isInFuture: Boolean = {
//    asLocalDate.isAfter(LocalDate.now)
//  }

  override def toString = year + "-" + month + "-" + day

}

object RasDate {

  implicit val rasDateReads: Reads[RasDate] = (
    (JsPath \ "day").read[String] and
      (JsPath \ "month").read[String]and
      (JsPath \ "year").read[String]
    )(RasDate.apply _)

  implicit val rasDateWrites: Writes[RasDate] = (
    (JsPath \ "day").write[String] and
      (JsPath \ "month").write[String]and
      (JsPath \ "year").write[String]
    )(unlift(RasDate.unapply))


}
