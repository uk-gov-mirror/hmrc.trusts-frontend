/*
 * Copyright 2020 HM Revenue & Customs
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

package navigation.registration

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.core.UserAnswers
import play.api.mvc.Call

@Singleton
class TaskListNavigator @Inject()(frontendAppConfig: FrontendAppConfig) {

  def trustDetailsJourney(draftId: String): String = {
    frontendAppConfig.trustDetailsFrontendUrl(draftId)
  }

  def trusteesJourneyUrl(draftId: String): String = {
    frontendAppConfig.trusteesFrontendUrl(draftId)
  }

  def beneficiariesJourneyUrl(draftId: String): String = {
    frontendAppConfig.beneficiariesFrontendUrl(draftId)
  }

  def assetsJourney(userAnswers: UserAnswers, draftId: String): Call = {
    userAnswers.get(sections.Assets).getOrElse(Nil) match {
      case _ :: _ =>
        controllers.register.asset.routes.AddAssetsController.onPageLoad(draftId)
      case Nil =>
        controllers.register.asset.routes.AssetInterruptPageController.onPageLoad(draftId)
    }
  }

  def taxLiabilityJourney(draftId: String): String = {
    frontendAppConfig.taxLiabilityFrontendUrl(draftId)
  }

  def protectorsJourneyUrl(draftId: String): String = {
    frontendAppConfig.protectorsFrontendUrl(draftId)
  }

  def otherIndividualsJourneyUrl(draftId: String): String = {
    frontendAppConfig.otherIndividualsFrontendUrl(draftId)
  }
}
