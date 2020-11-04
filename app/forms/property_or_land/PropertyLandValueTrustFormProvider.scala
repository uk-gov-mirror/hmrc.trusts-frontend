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

package forms.property_or_land

import forms.Validation
import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class PropertyLandValueTrustFormProvider @Inject() extends Mappings {

  def withMaxValue(maxValue: String): Form[String] = {

    Form(
      "value" -> currency("propertyLandValueTrust.error.required")
        .verifying(
          firstError(
            isNotEmpty("value", "propertyLandValueTrust.error.required"),
            regexp(Validation.decimalCheck, "propertyLandValueTrust.error.whole"),
            regexp(Validation.onlyNumbersRegex, "propertyLandValueTrust.error.invalid"),
            isLessThan(maxValue, "propertyLandValueTrust.error.moreThanTotal"),
            minimumValue("1", "propertyLandValueTrust.error.zero")
          )
        )
    )
  }
}