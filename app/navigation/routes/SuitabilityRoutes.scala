/*
 * Copyright 2021 HM Revenue & Customs
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

package navigation.routes

import controllers.register.suitability.routes
import models.NormalMode
import models.core.UserAnswers
import pages.Page
import pages.register.suitability._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

object SuitabilityRoutes extends Routes {

  def route(draftId: String, is5mldEnabled: Boolean): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case ExpressTrustYesNoPage => _ => _ =>
      routes.TaxLiabilityInCurrentTaxYearYesNoController.onPageLoad(NormalMode, draftId)
    case TaxLiabilityInCurrentTaxYearYesNoPage => _ => ua =>
      yesNoNav(
        ua,
        TaxLiabilityInCurrentTaxYearYesNoPage,
        routes.BeforeYouContinueController.onPageLoad(draftId),
        routes.UndeclaredTaxLiabilityYesNoController.onPageLoad(NormalMode, draftId)
      )
    case UndeclaredTaxLiabilityYesNoPage => _ => ua =>
      yesNoNav(
        ua,
        UndeclaredTaxLiabilityYesNoPage,
        routes.BeforeYouContinueController.onPageLoad(draftId),
        nonTaxableRoute(draftId, is5mldEnabled, ua)
      )
  }

  private def nonTaxableRoute(draftId: String, is5mldEnabled: Boolean, answers: UserAnswers): Call = {
    if (is5mldEnabled) {
      yesNoNav(
        answers,
        ExpressTrustYesNoPage,
        routes.BeforeYouContinueController.onPageLoad(draftId),
        routes.NoNeedToRegisterController.onPageLoad(draftId)
      )
    } else {
      routes.NoNeedToRegisterController.onPageLoad(draftId)
    }
  }
}
