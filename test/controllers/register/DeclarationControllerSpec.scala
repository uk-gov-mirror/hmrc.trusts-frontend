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

package controllers.register

import base.RegistrationSpecBase
import forms.DeclarationFormProvider
import models.NormalMode
import models.RegistrationSubmission.AllStatus
import models.core.UserAnswers
import models.core.http.RegistrationTRNResponse
import models.core.http.TrustResponse._
import models.core.pages.{Declaration, FullName}
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when, _}
import pages.register.{DeclarationPage, RegistrationProgress}
import play.api.data.Form
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FeatureFlagService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import views.html.register.DeclarationView

import scala.concurrent.Future


class DeclarationControllerSpec extends RegistrationSpecBase {

  val formProvider = new DeclarationFormProvider()
  val form: Form[Declaration] = formProvider()
  val name = "name"

  lazy val declarationRoute: String = routes.DeclarationController.onPageLoad(fakeDraftId).url

  before {
    reset(mockSubmissionService)
  }

  when(registrationsRepository.getAllStatus(any())(any())).thenReturn(Future.successful(AllStatus.withAllComplete))

  "Declaration Controller" must {

    "redirect when registration is not complete" in {
      val mockRegistrationProgress = mock[RegistrationProgress]

      when(mockRegistrationProgress.isTaskListComplete(any(), any())(any())).thenReturn(Future.successful(false))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), AffinityGroup.Agent)
        .overrides(inject.bind[RegistrationProgress].toInstance(mockRegistrationProgress))
        .build()

      val request = FakeRequest(GET, declarationRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TaskListController.onPageLoad(fakeDraftId).url

      application.stop()
    }

    "return OK and the correct view for a GET for Organisation user" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), AffinityGroup.Organisation).build()

      val request = FakeRequest(GET, declarationRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[DeclarationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode,fakeDraftId,AffinityGroup.Organisation)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET for Agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), AffinityGroup.Agent).build()

      val request = FakeRequest(GET, declarationRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[DeclarationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode,fakeDraftId,AffinityGroup.Agent)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(DeclarationPage, Declaration(FullName("First", None, "Last"), Some("email@email.com"))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), AffinityGroup.Agent).build()

      val request = FakeRequest(GET, declarationRoute)

      val view = application.injector.instanceOf[DeclarationView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(Declaration(FullName("First",None, "Last"), Some("email@email.com"))),
          NormalMode,fakeDraftId,AffinityGroup.Agent)(request, messages).toString

      application.stop()
    }

    "redirect to the confirmation page when valid data is submitted and registration submitted successfully " in {

      when(mockSubmissionService.submit(any[UserAnswers], any[Boolean])(any(), any[HeaderCarrier], any())).
        thenReturn(Future.successful(RegistrationTRNResponse("xTRN12456")))

      val featureFlagService = mock[FeatureFlagService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), AffinityGroup.Agent)
          .overrides(
            bind[FeatureFlagService].toInstance(featureFlagService)
          ).build()

      when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

      val request =
        FakeRequest(POST, declarationRoute)
          .withFormUrlEncodedBody(("firstName", "value 1"), ("lastName", "value 2"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(fakeDraftId).url
      verify(mockSubmissionService, times(1)).submit(any[UserAnswers], any[Boolean])(any(), any[HeaderCarrier], any())
      application.stop()
    }

    "redirect to the task list page when valid data is submitted and submission service can not register successfully" in {

      when(mockSubmissionService.submit(any[UserAnswers], any[Boolean])(any(), any[HeaderCarrier], any())).
        thenReturn(Future.failed(UnableToRegister()))

      val featureFlagService = mock[FeatureFlagService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), AffinityGroup.Agent)
          .overrides(
            bind[FeatureFlagService].toInstance(featureFlagService)
          ).build()

      when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

      val request =
        FakeRequest(POST, declarationRoute)
          .withFormUrlEncodedBody(("firstName", "value 1"), ("lastName", "value 2"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.TaskListController.onPageLoad(fakeDraftId).url
      verify(mockSubmissionService, times(1)).submit(any[UserAnswers], any[Boolean])(any(), any[HeaderCarrier], any())
      application.stop()
    }

    "redirect to the already registered page when valid data is submitted and trust is already registered" in {

      when(mockSubmissionService.submit(any[UserAnswers], any[Boolean])(any(), any[HeaderCarrier], any())).
        thenReturn(Future.successful(AlreadyRegistered))

      val featureFlagService = mock[FeatureFlagService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), AffinityGroup.Agent)
          .overrides(
            bind[FeatureFlagService].toInstance(featureFlagService)
          ).build()

      when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

      val request =
        FakeRequest(POST, declarationRoute)
          .withFormUrlEncodedBody(("firstName", "value 1"), ("lastName", "value 2"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UTRSentByPostController.onPageLoad().url
      verify(mockSubmissionService, times(1)).submit(any[UserAnswers], any[Boolean])(any(), any[HeaderCarrier], any())
      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), AffinityGroup.Agent).build()

      val request =
        FakeRequest(POST, declarationRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[DeclarationView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode,fakeDraftId,AffinityGroup.Agent)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, AffinityGroup.Agent).build()

      val request = FakeRequest(GET, declarationRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, AffinityGroup.Agent).build()

      val request =
        FakeRequest(POST, declarationRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
