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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import java.io._

import models.{MemberDetails, MemberName, RasDate}
import org.scalatest.BeforeAndAfter

class FileReaderServiceSpec extends UnitSpec with OneServerPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfter {

  val SUT = FileReaderService

  val multiLineFile = "testDataMultiLine.csv"
  val singleLineFile = "testDataSingleLine.csv"
  val blankFile = "blankFile.csv"
  val multiLineWithMissingValueFile = "multiLineWithMissingValues.csv"
  val multiLineWithIncorrectDateFile = "multiLineWithIncorrectDateFile.csv"

  val testData_multiLine = List("Joe, Bloggs, AB123456C, 1976-05-19", "Jane, Doe, AA123456A, 1999-09-21")
  val testData_singleLine = List("Joe, Bloggs, AB123456C, 1976-05-19")
  val testData_blankFile = Nil
  val testData_multiLine_missingAValue = List("Joe, Bloggs, AB123456C", "Jane, Doe, AA123456A, 1999-09-21")
  val testData_multiLine_multiLineWithIncorrectDate = List("Joe, Bloggs, AB123456C, 1976-05-19", "Jane, Doe, AA123456A, 1999-21-09")

/*  before{
    setupTestFile(multiLineFile, testData_multiLine)
    setupTestFile(singleLineFile, testData_singleLine)
    setupTestFile(blankFile, testData_blankFile)
  }

  after{
    new File(multiLineFile).delete()
    new File(singleLineFile).delete()
    new File(blankFile).delete()
  }*/

  "readLines" should {
    "read each line of a file and return as a list for a multi line file" in {
      setupTestFile(multiLineFile, testData_multiLine)

      SUT.readLines(multiLineFile) should contain theSameElementsAs testData_multiLine

      deleteTestFile(multiLineFile)
    }

    "read the one line declared within a single line file and return as a list" in {
      setupTestFile(singleLineFile, testData_singleLine)

      SUT.readLines(singleLineFile) should contain theSameElementsAs testData_singleLine

      deleteTestFile(singleLineFile)
    }

    "read no lines as none declared in a blank file and return as an empty list" in {
      setupTestFile(blankFile, testData_blankFile)

      SUT.readLines(blankFile) should contain theSameElementsAs testData_blankFile

      deleteTestFile(blankFile)
    }

    "read each line of a file where one line contains one less column with a total of two rows and return a list with two elements" in {
      setupTestFile(multiLineWithMissingValueFile, testData_multiLine_missingAValue)

      SUT.readLines(multiLineWithMissingValueFile) should contain theSameElementsAs testData_multiLine_missingAValue

      deleteTestFile(multiLineWithMissingValueFile)
    }
  }

  "getMembersFromFile" should {
    val member1 = MemberDetails(MemberName("Joe", "Bloggs"), "AB123456C", RasDate(Some("19"), Some("5"), Some("1976")))

    "return a list of type MemberDetails containing two elements as two lines declared in the file" in {
      setupTestFile(multiLineFile, testData_multiLine)

      val member2 = MemberDetails(MemberName("Jane", "Doe"), "AA123456A", RasDate(Some("21"), Some("9"), Some("1999")))

      val expectedResult: List[MemberDetails] = List(member1, member2)

      SUT.getMemberDetailsFromFile(multiLineFile) should contain theSameElementsAs expectedResult

      deleteTestFile(multiLineFile)
    }

    "return a list of type MemberDetails containing one element as one line declared in the file" in {
      setupTestFile(singleLineFile, testData_singleLine)

      val expectedResult: List[MemberDetails] = List(member1)

      SUT.getMemberDetailsFromFile(singleLineFile) should contain theSameElementsAs expectedResult

      deleteTestFile(singleLineFile)
    }

    "return a list of empty as the file was blank" in {
      setupTestFile(blankFile, testData_blankFile)

      SUT.getMemberDetailsFromFile(blankFile) should contain theSameElementsAs Nil

      deleteTestFile(blankFile)
    }

    "read each line of a file where one line contains one less column with a total of two rows and return a list with one element" in {
      setupTestFile(multiLineWithMissingValueFile, testData_multiLine_missingAValue)

      val member2 = MemberDetails(MemberName("Jane", "Doe"), "AA123456A", RasDate(Some("21"), Some("9"), Some("1999")))

      val expectedResult: List[MemberDetails] = List(member2)

      SUT.getMemberDetailsFromFile(multiLineWithMissingValueFile) should contain theSameElementsAs expectedResult

      deleteTestFile(multiLineWithMissingValueFile)
    }
//
//    "read each line of a file and ignore a line if the date is not correctly stored" in {
//      setupTestFile(multiLineWithIncorrectDateFile, testData_multiLine_multiLineWithIncorrectDate)
//
//      val expectedResult: List[MemberDetails] = List(member1)
//
//      SUT.getMemberDetailsFromFile(multiLineWithIncorrectDateFile) should contain theSameElementsAs expectedResult
//
//      deleteTestFile(multiLineWithIncorrectDateFile)
//    }
  }

  def setupTestFile(fileName: String, testData: List[String]) = {
    using(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
      writer =>
        for (x <- testData) {
          writer.write(x + "\r\n")
        }
    }
  }

  def deleteTestFile(fileName: String) = {
    new File(fileName).delete()
  }

  def using[T <: Closeable, R](resource: T)(block: T => R): R = {
    try { block(resource) }
    finally { resource.close() }
  }
}
