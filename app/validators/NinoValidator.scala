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

trait NinoValidator {
  val validNinoRegex = "^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D]?$"
  val specialCharacterRegex = "^[a-zA-Z0-9 ]*$"
  def isValid(nino: String): Boolean = nino.replaceAll("\\s", "").toUpperCase.matches(validNinoRegex)
  def containsNoSpecialCharacters(nino: String): Boolean = nino.replaceAll("\\s", "").toUpperCase.matches(specialCharacterRegex)
}

object NinoValidator extends NinoValidator {

}