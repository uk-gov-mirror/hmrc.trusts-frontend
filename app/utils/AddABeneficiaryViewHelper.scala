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

package utils

import controllers.routes
import models.{FullName, NormalMode, UserAnswers}
import play.api.i18n.Messages
import sections.{ClassOfBeneficiaries, IndividualBeneficiaries}
import viewmodels.addAnother.{ClassOfBeneficiaryViewModel, IndividualBeneficiaryViewModel}
import viewmodels.{AddRow, AddToRows}

class AddABeneficiaryViewHelper(userAnswers: UserAnswers, draftId : String)(implicit messages: Messages) {

  private case class InProgressComplete(inProgress : List[AddRow], complete: List[AddRow])

  private def parseName(name : Option[FullName]) : String = {
    val defaultValue = messages("entities.no.name.added")
    name.map(_.toString).getOrElse(defaultValue)
  }

  private def parseIndividualBeneficiary(individualBeneficiary : (IndividualBeneficiaryViewModel, Int)) : AddRow = {
    val vm = individualBeneficiary._1
    val index = individualBeneficiary._2

    AddRow(
      name = parseName(vm.name),
      typeLabel = messages("entities.beneficiary.individual"),
      changeUrl = "#",
      removeUrl = routes.RemoveIndividualBeneficiaryController.onPageLoad(NormalMode, index, draftId).url
    )
  }

  private def parseClassOfBeneficiary(classOfBeneficiary : ClassOfBeneficiaryViewModel) : AddRow = {
    val defaultValue = messages("entities.no.description.added")
    AddRow(
      classOfBeneficiary.description.getOrElse(defaultValue),
      messages("entities.beneficiary.class"),
      "#",
      "#"
    )
  }

  private def individualBeneficiaries = {
    val individualBeneficiaries = userAnswers.get(IndividualBeneficiaries).toList.flatten.zipWithIndex

    val indBeneficiaryComplete = individualBeneficiaries.filter(_._1.isComplete).map(parseIndividualBeneficiary)

    val indBenInProgress = individualBeneficiaries.filterNot(_._1.isComplete).map(parseIndividualBeneficiary)

    InProgressComplete(inProgress = indBenInProgress, complete = indBeneficiaryComplete)
  }

  private def classOfBeneficiaries = {
    val classOfBeneficiaries = userAnswers.get(ClassOfBeneficiaries).toList.flatten

    val completed = classOfBeneficiaries.filter(_.isComplete).map(parseClassOfBeneficiary)

    val progress = classOfBeneficiaries.filterNot(_.isComplete).map(parseClassOfBeneficiary)

    InProgressComplete(inProgress = progress, complete = completed)
  }

  def rows : AddToRows =
    AddToRows(
      inProgress = individualBeneficiaries.inProgress ::: classOfBeneficiaries.inProgress,
      complete = individualBeneficiaries.complete ::: classOfBeneficiaries.complete
    )

}
