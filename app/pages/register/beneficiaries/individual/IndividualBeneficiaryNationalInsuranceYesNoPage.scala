/*
 * Copyright 2019 HM Revenue & Customs
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

package pages.register.beneficiaries.individual

import models.core.UserAnswers
import pages.QuestionPage
import play.api.libs.json.JsPath
import sections.beneficiaries.{Beneficiaries, IndividualBeneficiaries}

import scala.util.Try

final case class IndividualBeneficiaryNationalInsuranceYesNoPage(index : Int) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \  Beneficiaries \ IndividualBeneficiaries \ index \ toString

  override def toString: String = "nationalInsuranceNumberYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(IndividualBeneficiaryNationalInsuranceNumberPage(index))
      case Some(true) =>
        userAnswers.remove(IndividualBeneficiaryAddressYesNoPage(index))
          .flatMap(_.remove(IndividualBeneficiaryAddressUKYesNoPage(index)))
          .flatMap(_.remove(IndividualBeneficiaryAddressUKPage(index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}
