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

package services

import helpers.FromListToCaseClass
import models.{MemberDetails, MemberName, RasDate}
import org.joda.time.LocalDate

import scala.io.Source

object FileReaderService extends FileReaderService

trait FileReaderService {

  case class Member(firstName: String, lastName: String, nino: String, dob: String)

  def getMemberDetailsFromFile(fileName: String): List[MemberDetails] = {
    readLines(fileName).flatMap { line =>
      fromListToCaseClass[Member](line.split(',').map(_.trim))
    } map convertMemberToMemberDetails
  }

  def readLines(fileName: String): List[String] = {
    Source.fromFile(fileName).getLines.toList
  }

  private def fromListToCaseClass[T] = new FromListToCaseClass[T]

  private def convertMemberToMemberDetails(member: Member) = {

    val rasDate = RasDate.fromLocalDate(new LocalDate(member.dob))

    MemberDetails(MemberName(member.firstName, member.lastName), member.nino, rasDate)
  }
}
