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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import models.AddATrustee.{NoComplete, YesLater, YesNow}
import pages.{AssetMoneyValuePage, _}
import models._
import uk.gov.hmrc.auth.core.AffinityGroup

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => AffinityGroup => UserAnswers => Call = {
    //  Matching
    case TrustRegisteredOnlinePage => af => _ => routes.TrustHaveAUTRController.onPageLoad(NormalMode)
    case TrustHaveAUTRPage => af => userAnswers => trustHaveAUTRRoute(userAnswers, af)
    case AgentInternalReferencePage => _ => _ => routes.TrustNameController.onPageLoad(NormalMode)
    case WhatIsTheUTRPage => _ => _ => routes.TrustNameController.onPageLoad(NormalMode)
    case PostcodeForTheTrustPage => _ => _ => routes.FailedMatchController.onPageLoad()

    //  Trust Details
    case TrustNamePage => _ => trustNameRoute
    case WhenTrustSetupPage => _ => _ => routes.GovernedInsideTheUKController.onPageLoad(NormalMode)
    case GovernedInsideTheUKPage => _ => isTrustGovernedInsideUKRoute
    case CountryGoverningTrustPage => _ => _ => routes.AdministrationInsideUKController.onPageLoad(NormalMode)
    case AdministrationInsideUKPage => _ => isTrustGeneralAdministrationRoute
    case CountryAdministeringTrustPage => _ => _ => routes.TrustResidentInUKController.onPageLoad(NormalMode)
    case TrustResidentInUKPage => _ => isTrustResidentInUKRoute
    case EstablishedUnderScotsLawPage => _ => _ => routes.TrustResidentOffshoreController.onPageLoad(NormalMode)
    case TrustResidentOffshorePage => _ => wasTrustPreviouslyResidentOffshoreRoute
    case TrustPreviouslyResidentPage => _ => _ => routes.TrustDetailsAnswerPageController.onPageLoad()
    case RegisteringTrustFor5APage => _ => registeringForPurposeOfSchedule5ARoute
    case NonResidentTypePage => _ => _ => routes.TrustDetailsAnswerPageController.onPageLoad()
    case InheritanceTaxActPage => _ => inheritanceTaxRoute
    case AgentOtherThanBarristerPage => _ => _ => routes.TrustDetailsAnswerPageController.onPageLoad()
    case TrustDetailsAnswerPage => _ => ua => routes.IsThisLeadTrusteeController.onPageLoad(NormalMode, 0)

    //  Trustees
    case IsThisLeadTrusteePage(index) => _ =>_ => routes.TrusteeIndividualOrBusinessController.onPageLoad(NormalMode, index)
    case TrusteeIndividualOrBusinessPage(index) => _ => _ => routes.TrusteesNameController.onPageLoad(NormalMode, index)
    case TrusteesNamePage(index) => _ => _ => routes.TrusteesDateOfBirthController.onPageLoad(NormalMode, index)
    case TrusteesDateOfBirthPage(index) => _ => ua => trusteeDateOfBirthRoute(ua, index)
    case TrusteeAUKCitizenPage(index) => _ => ua => trusteeAUKCitizenRoute(ua, index)
    case TrusteesNinoPage(index) => _ => _ => routes.TrusteeLiveInTheUKController.onPageLoad(NormalMode, index)
    case TrusteeLiveInTheUKPage(index) => _ => _ => routes.TrusteesUkAddressController.onPageLoad(NormalMode, index)
    case TrusteesUkAddressPage(index) => _ => _ => routes.TelephoneNumberController.onPageLoad(NormalMode, index)
    case TelephoneNumberPage(index) => _ => _ => routes.TrusteesAnswerPageController.onPageLoad(index)
    case TrusteesAnswerPage => _ => _ => routes.AddATrusteeController.onPageLoad()
    case AddATrusteePage => _ => addATrusteeRoute

    //Agents
    case AgentTelephoneNumberPage => _ => _ => routes.AgentAnswerController.onPageLoad()

    //Assets
    case AssetMoneyValuePage(index) => _ => _ => routes.AssetMoneyValueController.onPageLoad(NormalMode)

    //  Default
    case _ => _ => _ => routes.IndexController.onPageLoad()
  }

  private def addATrusteeRoute(answers: UserAnswers) = {
    val addAnother = answers.get(AddATrusteePage)

    def routeToTrusteeIndex = {
      val trustees = answers.get(Trustees).getOrElse(List.empty)
      trustees match {
        case Nil =>
          routes.IsThisLeadTrusteeController.onPageLoad(NormalMode, 0)
        case t if t.nonEmpty =>
          routes.IsThisLeadTrusteeController.onPageLoad(NormalMode, t.size)
      }
    }

    addAnother match {
      case Some(YesNow) =>
        routeToTrusteeIndex
      case Some(YesLater) =>
        routes.AddATrusteeController.onPageLoad()
      case Some(NoComplete) =>
        routes.AddATrusteeController.onPageLoad()
      case _ => routes.SessionExpiredController.onPageLoad()
    }
  }

  private def trustHaveAUTRRoute(answers: UserAnswers, af: AffinityGroup) = {
    val condition = (answers.get(TrustRegisteredOnlinePage), answers.get(TrustHaveAUTRPage))

    condition match {
      case (Some(false), Some(true)) => routes.WhatIsTheUTRController.onPageLoad(NormalMode)
      case (Some(false), Some(false)) => if(af == AffinityGroup.Organisation){ routes.TrustNameController.onPageLoad(NormalMode)} else {routes.AgentInternalReferenceController.onPageLoad(NormalMode)}
      case (Some(true), Some(false)) => routes.UTRSentByPostController.onPageLoad()
      case (Some(true), Some(true)) => routes.CannotMakeChangesController.onPageLoad()
      case _ => routes.SessionExpiredController.onPageLoad()
    }
  }

  private def trustNameRoute(answers: UserAnswers) = {
    val hasUTR = answers.get(TrustHaveAUTRPage).contains(true)

    if (hasUTR) {
      routes.PostcodeForTheTrustController.onPageLoad(NormalMode)
    } else {
      routes.WhenTrustSetupController.onPageLoad(NormalMode)
    }
  }

  private def isTrustGovernedInsideUKRoute(answers: UserAnswers) = answers.get(GovernedInsideTheUKPage) match {
    case Some(true)  => routes.AdministrationInsideUKController.onPageLoad(NormalMode)
    case Some(false) => routes.CountryGoverningTrustController.onPageLoad(NormalMode)
    case None        => routes.SessionExpiredController.onPageLoad()
  }

  private def isTrustGeneralAdministrationRoute(answers: UserAnswers) = answers.get(AdministrationInsideUKPage) match {
    case Some(true)  => routes.TrustResidentInUKController.onPageLoad(NormalMode)
    case Some(false) => routes.CountryAdministeringTrustController.onPageLoad(NormalMode)
    case None        => routes.SessionExpiredController.onPageLoad()
  }

  private def isTrustResidentInUKRoute(answers: UserAnswers) = answers.get(TrustResidentInUKPage) match {
    case Some(true)   => routes.EstablishedUnderScotsLawController.onPageLoad(NormalMode)
    case Some(false)  => routes.RegisteringTrustFor5AController.onPageLoad(NormalMode)
    case None         => routes.SessionExpiredController.onPageLoad()
  }

  private def wasTrustPreviouslyResidentOffshoreRoute(answers: UserAnswers) = answers.get(TrustResidentOffshorePage) match {
    case Some(true)   => routes.TrustPreviouslyResidentController.onPageLoad(NormalMode)
    case Some(false)  => routes.TrustDetailsAnswerPageController.onPageLoad()
    case None         => routes.SessionExpiredController.onPageLoad()
  }

  private def registeringForPurposeOfSchedule5ARoute(answers: UserAnswers) = answers.get(RegisteringTrustFor5APage) match {
    case Some(true)   => routes.NonResidentTypeController.onPageLoad(NormalMode)
    case Some(false)  => routes.InheritanceTaxActController.onPageLoad(NormalMode)
    case None         => routes.SessionExpiredController.onPageLoad()
  }

  private def inheritanceTaxRoute(answers: UserAnswers) = answers.get(InheritanceTaxActPage) match {
    case Some(true)   => routes.AgentOtherThanBarristerController.onPageLoad(NormalMode)
    case Some(false)  => routes.TrustDetailsAnswerPageController.onPageLoad()
    case None         => routes.SessionExpiredController.onPageLoad()
  }

  private def trusteeAUKCitizenRoute(answers: UserAnswers, index: Int) = answers.get(TrusteeAUKCitizenPage(index)) match {
    case Some(true)   => routes.TrusteesNinoController.onPageLoad(NormalMode,index)
    case Some(false)  => routes.TrusteesAnswerPageController.onPageLoad(index)
    case None         => routes.SessionExpiredController.onPageLoad()
  }

  private def trusteeDateOfBirthRoute(answers: UserAnswers, index : Int) = answers.get(IsThisLeadTrusteePage(index)) match {
    case Some(true) => routes.TrusteeAUKCitizenController.onPageLoad(NormalMode, index)
    case Some(false) => routes.TrusteesAnswerPageController.onPageLoad(index)
    case None => routes.SessionExpiredController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    // TrustDetails
    case TrustNamePage => _ => routes.TrustDetailsAnswerPageController.onPageLoad()
    case WhenTrustSetupPage => _ => routes.TrustDetailsAnswerPageController.onPageLoad()
    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, af :AffinityGroup = AffinityGroup.Organisation): UserAnswers => Call = mode match {
    case NormalMode =>
      normalRoutes(page)(af)
    case CheckMode =>
      checkRouteMap(page)
  }

}
