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
import forms.YesNoFormProvider
import models.NormalMode
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.register.TrustRegisteredOnlinePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FeatureFlagService
import views.html.register.TrustRegisteredOnlineView

import scala.concurrent.Future

class TrustRegisteredOnlineControllerSpec extends RegistrationSpecBase {

  val formProvider = new YesNoFormProvider()
  val form = formProvider.withPrefix("trustRegisteredOnline")

  val featureFlagService = mock[FeatureFlagService]

  lazy val trustRegisteredOnlineRoute = routes.TrustRegisteredOnlineController.onPageLoad(NormalMode,fakeDraftId).url

  "TrustRegisteredOnline Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, trustRegisteredOnlineRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[TrustRegisteredOnlineView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode,fakeDraftId)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(TrustRegisteredOnlinePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, trustRegisteredOnlineRoute)

      val view = application.injector.instanceOf[TrustRegisteredOnlineView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), NormalMode, fakeDraftId)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FeatureFlagService].toInstance(featureFlagService)
          ).build()

      when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

      val request =
        FakeRequest(POST, trustRegisteredOnlineRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FeatureFlagService].toInstance(featureFlagService)
          ).build()

      when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

      val request =
        FakeRequest(POST, trustRegisteredOnlineRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[TrustRegisteredOnlineView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, fakeDraftId)(request, messages).toString

      application.stop()
    }

    "redirect SessionExpired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, trustRegisteredOnlineRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to the SessionExpired when valid data is submitted if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, trustRegisteredOnlineRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }


  }
}
