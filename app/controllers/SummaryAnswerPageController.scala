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

package controllers

import controllers.actions._
import javax.inject.Inject
import pages.{AgentInternalReferencePage, RegistrationProgress}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection
import views.html.SummaryAnswerPageView


class SummaryAnswerPageController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getData: DraftIdRetrievalActionProvider,
                                              requireData: DataRequiredAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: SummaryAnswerPageView,
                                              countryOptions : CountryOptions,
                                              registrationProgress: RegistrationProgress,
                                              registrationComplete : TaskListCompleteActionRefiner
                                            ) extends FrontendBaseController with I18nSupport {

  private def actions(draftId : String) =
    identify andThen getData(draftId) andThen requireData andThen registrationComplete

  def onPageLoad(draftId : String) = actions(draftId) {
    implicit request =>

      val isAgent = request.affinityGroup == Agent
      val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(request.userAnswers, draftId, canEdit = false)
      val trustDetails = checkYourAnswersHelper.trustDetails.getOrElse(Nil)
      val trustees = checkYourAnswersHelper.trustees.getOrElse(Nil)
      val settlors = checkYourAnswersHelper.settlors.getOrElse(Nil)
      val individualBeneficiaries = checkYourAnswersHelper.individualBeneficiaries.getOrElse(Nil)
      val individualBeneficiariesExist: Boolean = individualBeneficiaries.nonEmpty
      val classOfBeneficiaries = checkYourAnswersHelper.classOfBeneficiaries(individualBeneficiariesExist).getOrElse(Nil)
      val moneyAsset = checkYourAnswersHelper.moneyAsset.getOrElse(Nil)
      val shareAsset = checkYourAnswersHelper.shareAssets.getOrElse(Nil)

//      val allAssets = checkYourAnswersHelper.allAssets.getOrElse(Nil)
            val allAssets = Seq(AnswerSection(None, Seq(), Some("Assets"))) ++ moneyAsset ++ shareAsset
//      val allAssetsSections = Seq(AnswerSection(None, Seq(), Some("Assets"))) ++ allAssets

      val sections = trustDetails ++ settlors ++ trustees ++ individualBeneficiaries ++ classOfBeneficiaries ++ allAssets

      val agentClientRef = request.userAnswers.get(AgentInternalReferencePage).getOrElse("")

      Ok(view(sections, isAgent, agentClientRef))

  }

}