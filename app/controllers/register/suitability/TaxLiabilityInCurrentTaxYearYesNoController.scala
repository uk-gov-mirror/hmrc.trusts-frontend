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

package controllers.register.suitability

import controllers.actions.StandardActionSets
import forms.YesNoFormProvider

import javax.inject.Inject
import models.Mode
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.register.suitability.{TaxLiabilityInCurrentTaxYearYesNoPage, TrustTaxableYesNoPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import services.LocalDateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.time.TaxYear
import utils.DateFormatter
import views.html.register.suitability.TaxLiabilityInCurrentTaxYearYesNoView

import scala.concurrent.{ExecutionContext, Future}

class TaxLiabilityInCurrentTaxYearYesNoController @Inject()(
                                                             override val messagesApi: MessagesApi,
                                                             registrationsRepository: RegistrationsRepository,
                                                             standardActionSets: StandardActionSets,
                                                             navigator: Navigator,
                                                             yesNoFormProvider: YesNoFormProvider,
                                                             val controllerComponents: MessagesControllerComponents,
                                                             view: TaxLiabilityInCurrentTaxYearYesNoView,
                                                             dateFormatter: DateFormatter,
                                                             localDateService: LocalDateService
                                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("suitability.taxLiabilityInCurrentTaxYear")

  private def actions(draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    standardActionSets.identifiedUserWithData(draftId)

  def onPageLoad(mode: Mode, draftId: String): Action[AnyContent] = actions(draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(TaxLiabilityInCurrentTaxYearYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, draftId, currentTaxYearStartAndEnd))
  }

  def onSubmit(mode: Mode, draftId : String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, draftId, currentTaxYearStartAndEnd))),

        value => {
          for {
            answers <- Future.fromTry(request.userAnswers.set(TaxLiabilityInCurrentTaxYearYesNoPage, value))
            updatedAnswers <- Future.fromTry(answers.set(TrustTaxableYesNoPage, value))
            _ <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(TaxLiabilityInCurrentTaxYearYesNoPage, mode, draftId)(updatedAnswers))
        }
      )
  }

  private def currentTaxYearStartAndEnd(implicit messages: Messages): (String, String) = {
    val currentTaxYear = TaxYear.taxYearFor(localDateService.now())
    (dateFormatter.formatDate(currentTaxYear.starts), dateFormatter.formatDate(currentTaxYear.finishes))
  }
}
