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

package repositories

import connector.SubmissionDraftConnector
import models.RegistrationSubmission.AllStatus
import models.core.UserAnswers
import models.core.http.{AddressType, LeadTrusteeType}
import models.registration.pages.RegistrationStatus.InProgress
import play.api.http
import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.DateFormatter
import viewmodels.{DraftRegistration, RegistrationAnswerSections}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultRegistrationsRepository @Inject()(dateFormatter: DateFormatter,
                                               submissionDraftConnector: SubmissionDraftConnector)
                                              (implicit ec: ExecutionContext) extends RegistrationsRepository {

  override def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    submissionDraftConnector.getDraftMain(draftId).map {
      response => Some(response.data.as[UserAnswers])
    }
  }

  override def getMostRecentDraftId()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    submissionDraftConnector.getCurrentDraftIds().map(_.headOption.map(_.draftId))
  }

  override def listDrafts()(implicit hc: HeaderCarrier, messages: Messages): Future[List[DraftRegistration]] = {
    submissionDraftConnector.getCurrentDraftIds().map {
      draftIds =>
        draftIds.flatMap {
          x => x.reference.map {
            reference => DraftRegistration(x.draftId, reference, dateFormatter.savedUntil(x.createdAt))
          }
        }
    }
  }

  override def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      reference <- getClientReference(userAnswers.draftId)
      response <- submissionDraftConnector.setDraftMain(
        draftId = userAnswers.draftId,
        draftData = Json.toJson(userAnswers),
        inProgress = userAnswers.progress == InProgress,
        reference = reference
      )
    } yield {
      response.status == http.Status.OK
    }
  }

  private def decodePath(encodedPath: String): JsPath =
    encodedPath.split('/').foldLeft[JsPath](
      JsPath
    )(
      (cur: JsPath, component: String) => cur \ component
    )

  private def addSection(key: String, section: JsValue, data: JsValue): JsResult[JsValue] = {
    val path = decodePath(key).json
    val transform = __.json.update(path.put(section))

    data.transform(transform)
  }

  override def addDraftRegistrationSections(draftId: String, registrationJson: JsValue)(implicit hc: HeaderCarrier): Future[JsValue] = {
    submissionDraftConnector.getRegistrationPieces(draftId).map {
      pieces =>
        val added: JsResult[JsValue] = pieces.keys.foldLeft[JsResult[JsValue]](
          JsSuccess(registrationJson)
        )(
          (cur, key) => cur.flatMap(addSection(key, pieces(key), _))
        )

        added match {
          case JsSuccess(value, _) => value
          case _ => registrationJson
        }
    }
  }

  override def getAllStatus(draftId: String)(implicit hc: HeaderCarrier): Future[AllStatus] = {
    submissionDraftConnector.getStatus(draftId)
  }

  override def getAnswerSections(draftId: String)(implicit hc: HeaderCarrier): Future[RegistrationAnswerSections] = {
    submissionDraftConnector.getAnswerSections(draftId).map(RegistrationAnswerSections.fromAllAnswerSections)
  }

  override def getLeadTrustee(draftId: String)(implicit hc: HeaderCarrier): Future[LeadTrusteeType] =
    submissionDraftConnector.getLeadTrustee(draftId)

  override def getCorrespondenceAddress(draftId: String)(implicit hc: HeaderCarrier): Future[AddressType] =
    submissionDraftConnector.getCorrespondenceAddress(draftId)

  override def getAgentAddress(draftId: String)(implicit hc: HeaderCarrier): Future[Option[AddressType]] = {
    submissionDraftConnector.getAgentAddress(draftId).map(Some(_)).recover {
      case _ => None
    }
  }

  override def getClientReference(draftId: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    submissionDraftConnector.getClientReference(draftId).map(Some(_)).recover {
      case _ => None
    }
  }

  override def getTrustSetupDate(draftId: String)(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    submissionDraftConnector.getTrustSetupDate(draftId)

  override def getTrustName(draftId: String)(implicit hc: HeaderCarrier): Future[String] =
    submissionDraftConnector.getTrustName(draftId)

  override def removeDraft(draftId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    submissionDraftConnector.removeDraft(draftId)

  override def updateTaxLiability(draftId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    submissionDraftConnector.updateTaxLiability(draftId)
}

trait RegistrationsRepository {

  def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean]

  def listDrafts()(implicit hc: HeaderCarrier, messages: Messages): Future[List[DraftRegistration]]

  def getMostRecentDraftId()(implicit hc: HeaderCarrier): Future[Option[String]]

  def addDraftRegistrationSections(draftId: String, registrationJson: JsValue)(implicit hc: HeaderCarrier): Future[JsValue]

  def getAllStatus(draftId: String)(implicit hc: HeaderCarrier): Future[AllStatus]

  def getAnswerSections(draftId: String)(implicit hc: HeaderCarrier): Future[RegistrationAnswerSections]

  def getLeadTrustee(draftId: String)(implicit hc: HeaderCarrier): Future[LeadTrusteeType]

  def getCorrespondenceAddress(draftId: String)(implicit hc: HeaderCarrier): Future[AddressType]

  def getAgentAddress(draftId: String)(implicit hc: HeaderCarrier): Future[Option[AddressType]]

  def getClientReference(draftId: String)(implicit hc: HeaderCarrier): Future[Option[String]]

  def getTrustSetupDate(draftId: String)(implicit hc: HeaderCarrier): Future[Option[LocalDate]]

  def getTrustName(draftId: String)(implicit hc: HeaderCarrier): Future[String]

  def removeDraft(draftId: String)(implicit hc: HeaderCarrier): Future[HttpResponse]

  def updateTaxLiability(draftId: String)(implicit hc: HeaderCarrier): Future[HttpResponse]
}
