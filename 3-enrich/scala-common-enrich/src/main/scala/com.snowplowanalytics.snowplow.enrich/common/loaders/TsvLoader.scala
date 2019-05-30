/*
 * Copyright (c) 2012-2019 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.snowplow.enrich.common
package loaders

import java.time.Instant

import cats.data.ValidatedNel
import cats.syntax.either._
import cats.syntax.option._
import cats.syntax.validated._

import outputs._

/** Loader for TSVs */
final case class TsvLoader(adapter: String) extends Loader[String] {
  private val CollectorName = "tsv"

  /**
   * Converts the source TSV into a ValidatedMaybeCollectorPayload.
   * @param line A TSV
   * @return either a set of validation errors or an Option-boxed CanonicalInput object, wrapped in
   * a ValidatedNel.
   */
  override def toCP(line: String): ValidatedNel[BadRow, Option[CollectorPayload]] =
    // Throw away the first two lines of Cloudfront web distribution access logs
    if (line.startsWith("#Version:") || line.startsWith("#Fields:")) {
      None.valid
    } else {
      CollectorApi
        .parsePath(adapter)
        .map(
          CollectorPayload(
            Nil,
            CollectorName,
            "UTF-8",
            None,
            None,
            None,
            None,
            None,
            Nil,
            None,
            _,
            None,
            Some(line)
          ).some
        )
        .leftMap(
          f =>
            BadRow(
              CPFormatViolation(Instant.now(), CollectorName, f),
              RawPayload(line),
              Processor.default
            )
        )
        .toValidatedNel
    }
}
