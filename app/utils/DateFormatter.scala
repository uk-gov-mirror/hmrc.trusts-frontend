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

package utils

import java.time.{LocalDateTime, LocalDate => JavaDate}

import com.google.inject.Inject
import config.FrontendAppConfig
import org.joda.time.{LocalDate => JodaDate}
import play.api.i18n.Messages
import uk.gov.hmrc.play.language.LanguageUtils

class DateFormatter @Inject()(config: FrontendAppConfig, languageUtils: LanguageUtils) {

  def savedUntil(date: LocalDateTime)(implicit messages: Messages): String = {
    formatDateTime(date.plusSeconds(config.ttlInSeconds))
  }

  def formatDateTime(dateTime: LocalDateTime)(implicit messages: Messages): String = {
    formatDate(dateTime.toLocalDate)
  }

  def formatDate(date: JavaDate)(implicit messages: Messages): String = {
    val convertedDate: JodaDate = new JodaDate(date.getYear, date.getMonthValue, date.getDayOfMonth)
    formatDate(convertedDate)
  }

  def formatDate(date: JodaDate)(implicit messages: Messages): String = {
    languageUtils.Dates.formatDate(date)
  }

}
