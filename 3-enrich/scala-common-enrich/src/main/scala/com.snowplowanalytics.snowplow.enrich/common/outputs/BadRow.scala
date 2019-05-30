/*
 * Copyright (c) 2014-2019 Snowplow Analytics Ltd. All rights reserved.
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
package outputs

import java.time.Instant

import cats.data.NonEmptyList
import com.snowplowanalytics.iglu.client.ClientError
import com.snowplowanalytics.iglu.core.{ParseError, SchemaCriterion, SchemaKey}
import org.apache.http.NameValuePair
import org.joda.time.DateTime

import loaders._

final case class BadRow(
  failure: Failure,
  payload: Payload,
  processor: Processor
)

sealed trait Payload
final case class RawPayload(line: String) extends Payload

/**
 * The canonical input format for the ETL process: it should be possible to convert any collector
 * input format to this format, ready for the main, collector-agnostic stage of the ETL.
 */
final case class CollectorPayload(
  api: CollectorApi,
  querystring: List[NameValuePair], // Could be empty in future trackers
  contentType: Option[String], // Not always set
  body: Option[String], // Not set for GETs
  source: CollectorSource,
  context: CollectorContext
) extends Payload
object CollectorPayload {

  /**
   * A constructor version to use. Supports legacy tp1 (where no API vendor or version provided
   * as well as Snowplow).
   */
  def apply(
    querystring: List[NameValuePair],
    sourceName: String,
    sourceEncoding: String,
    sourceHostname: Option[String],
    contextTimestamp: Option[DateTime],
    contextIpAddress: Option[String],
    contextUseragent: Option[String],
    contextRefererUri: Option[String],
    contextHeaders: List[String],
    contextUserId: Option[String],
    api: CollectorApi,
    contentType: Option[String],
    body: Option[String]
  ): CollectorPayload = {
    val source = CollectorSource(sourceName, sourceEncoding, sourceHostname)
    val context = CollectorContext(
      contextTimestamp,
      contextIpAddress,
      contextUseragent,
      contextRefererUri,
      contextHeaders,
      contextUserId
    )
    CollectorPayload(api, querystring, contentType, body, source, context)
  }
}

final case class PartiallyEnrichedEvent(
  app_id: Option[String],
  platform: Option[String],
  etl_tstamp: Option[String],
  collector_tstamp: String,
  dvce_created_tstamp: Option[String],
  event: Option[String],
  event_id: Option[String],
  txn_id: Option[String],
  name_tracker: Option[String],
  v_tracker: Option[String],
  v_collector: String,
  v_etl: String,
  user_id: Option[String],
  user_ipaddress: Option[String],
  user_fingerprint: Option[String],
  domain_userid: Option[String],
  domain_sessionidx: Option[Int],
  network_userid: Option[String],
  geo_country: Option[String],
  geo_region: Option[String],
  geo_city: Option[String],
  geo_zipcode: Option[String],
  geo_latitude: Option[Float],
  geo_longitude: Option[Float],
  geo_region_name: Option[String],
  ip_isp: Option[String],
  ip_organization: Option[String],
  ip_domain: Option[String],
  ip_netspeed: Option[String],
  page_url: Option[String],
  page_title: Option[String],
  page_referrer: Option[String],
  page_urlscheme: Option[String],
  page_urlhost: Option[String],
  page_urlport: Option[Int],
  page_urlpath: Option[String],
  page_urlquery: Option[String],
  page_urlfragment: Option[String],
  refr_urlscheme: Option[String],
  refr_urlhost: Option[String],
  refr_urlport: Option[Int],
  refr_urlpath: Option[String],
  refr_urlquery: Option[String],
  refr_urlfragment: Option[String],
  refr_medium: Option[String],
  refr_source: Option[String],
  refr_term: Option[String],
  mkt_medium: Option[String],
  mkt_source: Option[String],
  mkt_term: Option[String],
  mkt_content: Option[String],
  mkt_campaign: Option[String],
  contexts: Option[String],
  se_category: Option[String],
  se_action: Option[String],
  se_label: Option[String],
  se_property: Option[String],
  se_value: Option[String],
  unstruct_event: Option[String],
  tr_orderid: Option[String],
  tr_affiliation: Option[String],
  tr_total: Option[String],
  tr_tax: Option[String],
  tr_shipping: Option[String],
  tr_city: Option[String],
  tr_state: Option[String],
  tr_country: Option[String],
  ti_orderid: Option[String],
  ti_sku: Option[String],
  ti_name: Option[String],
  ti_category: Option[String],
  ti_price: Option[String],
  ti_quantity: Option[Int],
  pp_xoffset_min: Option[Int],
  pp_xoffset_max: Option[Int],
  pp_yoffset_min: Option[Int],
  pp_yoffset_max: Option[Int],
  useragent: Option[String],
  br_name: Option[String],
  br_family: Option[String],
  br_version: Option[String],
  br_type: Option[String],
  br_renderengine: Option[String],
  br_lang: Option[String],
  br_features_pdf: Option[Byte],
  br_features_flash: Option[Byte],
  br_features_java: Option[Byte],
  br_features_director: Option[Byte],
  br_features_quicktime: Option[Byte],
  br_features_realplayer: Option[Byte],
  br_features_windowsmedia: Option[Byte],
  br_features_gears: Option[Byte],
  br_features_silverlight: Option[Byte],
  br_cookies: Option[Byte],
  br_colordepth: Option[String],
  br_viewwidth: Option[Int],
  br_viewheight: Option[Int],
  os_name: Option[String],
  os_family: Option[String],
  os_manufacturer: Option[String],
  os_timezone: Option[String],
  dvce_type: Option[String],
  dvce_ismobile: Option[Byte],
  dvce_screenwidth: Option[Int],
  dvce_screenheight: Option[Int],
  doc_charset: Option[String],
  doc_width: Option[Int],
  doc_height: Option[Int],
  tr_currency: Option[String],
  tr_total_base: Option[String],
  tr_tax_base: Option[String],
  tr_shipping_base: Option[String],
  ti_currency: Option[String],
  ti_price_base: Option[String],
  base_currency: Option[String],
  geo_timezone: Option[String],
  mkt_clickid: Option[String],
  mkt_network: Option[String],
  etl_tags: Option[String],
  dvce_sent_tstamp: Option[String],
  refr_domain_userid: Option[String],
  refr_dvce_tstamp: Option[String],
  derived_contexts: Option[String],
  domain_sessionid: Option[String],
  derived_tstamp: Option[String],
  event_vendor: Option[String],
  event_name: Option[String],
  event_format: Option[String],
  event_version: Option[String],
  event_fingerprint: Option[String],
  true_tstamp: Option[String]
) extends Payload
object PartiallyEnrichedEvent {
  def apply(enrichedEvent: EnrichedEvent): PartiallyEnrichedEvent = PartiallyEnrichedEvent(
    app_id = Option(enrichedEvent.app_id),
    platform = Option(enrichedEvent.platform),
    etl_tstamp = Option(enrichedEvent.etl_tstamp),
    collector_tstamp = enrichedEvent.collector_tstamp,
    dvce_created_tstamp = Option(enrichedEvent.dvce_created_tstamp),
    event = Option(enrichedEvent.event),
    event_id = Option(enrichedEvent.event_id),
    txn_id = Option(enrichedEvent.txn_id),
    name_tracker = Option(enrichedEvent.name_tracker),
    v_tracker = Option(enrichedEvent.v_tracker),
    v_collector = enrichedEvent.v_collector,
    v_etl = enrichedEvent.v_etl,
    user_id = Option(enrichedEvent.user_id),
    user_ipaddress = Option(enrichedEvent.user_ipaddress),
    user_fingerprint = Option(enrichedEvent.user_fingerprint),
    domain_userid = Option(enrichedEvent.domain_userid),
    domain_sessionidx = Option(Integer2int(enrichedEvent.domain_sessionidx)),
    network_userid = Option(enrichedEvent.network_userid),
    geo_country = Option(enrichedEvent.geo_country),
    geo_region = Option(enrichedEvent.geo_region),
    geo_city = Option(enrichedEvent.geo_city),
    geo_zipcode = Option(enrichedEvent.geo_zipcode),
    geo_latitude = Option(Float2float(enrichedEvent.geo_latitude)),
    geo_longitude = Option(Float2float(enrichedEvent.geo_longitude)),
    geo_region_name = Option(enrichedEvent.geo_region_name),
    ip_isp = Option(enrichedEvent.ip_isp),
    ip_organization = Option(enrichedEvent.ip_organization),
    ip_domain = Option(enrichedEvent.ip_domain),
    ip_netspeed = Option(enrichedEvent.ip_netspeed),
    page_url = Option(enrichedEvent.page_url),
    page_title = Option(enrichedEvent.page_title),
    page_referrer = Option(enrichedEvent.page_referrer),
    page_urlscheme = Option(enrichedEvent.page_urlscheme),
    page_urlhost = Option(enrichedEvent.page_urlhost),
    page_urlport = Option(Integer2int(enrichedEvent.page_urlport)),
    page_urlpath = Option(enrichedEvent.page_urlpath),
    page_urlquery = Option(enrichedEvent.page_urlquery),
    page_urlfragment = Option(enrichedEvent.page_urlfragment),
    refr_urlscheme = Option(enrichedEvent.refr_urlscheme),
    refr_urlhost = Option(enrichedEvent.refr_urlhost),
    refr_urlport = Option(Integer2int(enrichedEvent.refr_urlport)),
    refr_urlpath = Option(enrichedEvent.refr_urlpath),
    refr_urlquery = Option(enrichedEvent.refr_urlquery),
    refr_urlfragment = Option(enrichedEvent.refr_urlfragment),
    refr_medium = Option(enrichedEvent.refr_medium),
    refr_source = Option(enrichedEvent.refr_source),
    refr_term = Option(enrichedEvent.refr_term),
    mkt_medium = Option(enrichedEvent.mkt_medium),
    mkt_source = Option(enrichedEvent.mkt_source),
    mkt_term = Option(enrichedEvent.mkt_term),
    mkt_content = Option(enrichedEvent.mkt_content),
    mkt_campaign = Option(enrichedEvent.mkt_campaign),
    contexts = Option(enrichedEvent.contexts),
    se_category = Option(enrichedEvent.se_category),
    se_action = Option(enrichedEvent.se_action),
    se_label = Option(enrichedEvent.se_label),
    se_property = Option(enrichedEvent.se_property),
    se_value = Option(enrichedEvent.se_value),
    unstruct_event = Option(enrichedEvent.unstruct_event),
    tr_orderid = Option(enrichedEvent.tr_orderid),
    tr_affiliation = Option(enrichedEvent.tr_affiliation),
    tr_total = Option(enrichedEvent.tr_total),
    tr_tax = Option(enrichedEvent.tr_tax),
    tr_shipping = Option(enrichedEvent.tr_shipping),
    tr_city = Option(enrichedEvent.tr_city),
    tr_state = Option(enrichedEvent.tr_state),
    tr_country = Option(enrichedEvent.tr_country),
    ti_orderid = Option(enrichedEvent.ti_orderid),
    ti_sku = Option(enrichedEvent.ti_sku),
    ti_name = Option(enrichedEvent.ti_name),
    ti_category = Option(enrichedEvent.ti_category),
    ti_price = Option(enrichedEvent.ti_price),
    ti_quantity = Option(Integer2int(enrichedEvent.ti_quantity)),
    pp_xoffset_min = Option(Integer2int(enrichedEvent.pp_xoffset_min)),
    pp_xoffset_max = Option(Integer2int(enrichedEvent.pp_xoffset_max)),
    pp_yoffset_min = Option(Integer2int(enrichedEvent.pp_yoffset_min)),
    pp_yoffset_max = Option(Integer2int(enrichedEvent.pp_yoffset_max)),
    useragent = Option(enrichedEvent.useragent),
    br_name = Option(enrichedEvent.br_name),
    br_family = Option(enrichedEvent.br_family),
    br_version = Option(enrichedEvent.br_version),
    br_type = Option(enrichedEvent.br_type),
    br_renderengine = Option(enrichedEvent.br_renderengine),
    br_lang = Option(enrichedEvent.br_lang),
    br_features_pdf = Option(Byte2byte(enrichedEvent.br_features_pdf)),
    br_features_flash = Option(Byte2byte(enrichedEvent.br_features_flash)),
    br_features_java = Option(Byte2byte(enrichedEvent.br_features_java)),
    br_features_director = Option(Byte2byte(enrichedEvent.br_features_director)),
    br_features_quicktime = Option(Byte2byte(enrichedEvent.br_features_quicktime)),
    br_features_realplayer = Option(Byte2byte(enrichedEvent.br_features_realplayer)),
    br_features_windowsmedia = Option(Byte2byte(enrichedEvent.br_features_windowsmedia)),
    br_features_gears = Option(Byte2byte(enrichedEvent.br_features_gears)),
    br_features_silverlight = Option(Byte2byte(enrichedEvent.br_features_silverlight)),
    br_cookies = Option(Byte2byte(enrichedEvent.br_cookies)),
    br_colordepth = Option(enrichedEvent.br_colordepth),
    br_viewwidth = Option(Integer2int(enrichedEvent.br_viewwidth)),
    br_viewheight = Option(Integer2int(enrichedEvent.br_viewheight)),
    os_name = Option(enrichedEvent.os_name),
    os_family = Option(enrichedEvent.os_family),
    os_manufacturer = Option(enrichedEvent.os_manufacturer),
    os_timezone = Option(enrichedEvent.os_timezone),
    dvce_type = Option(enrichedEvent.dvce_type),
    dvce_ismobile = Option(Byte2byte(enrichedEvent.dvce_ismobile)),
    dvce_screenwidth = Option(Integer2int(enrichedEvent.dvce_screenwidth)),
    dvce_screenheight = Option(Integer2int(enrichedEvent.dvce_screenheight)),
    doc_charset = Option(enrichedEvent.doc_charset),
    doc_width = Option(Integer2int(enrichedEvent.doc_width)),
    doc_height = Option(Integer2int(enrichedEvent.doc_height)),
    tr_currency = Option(enrichedEvent.tr_currency),
    tr_total_base = Option(enrichedEvent.tr_total_base),
    tr_tax_base = Option(enrichedEvent.tr_tax_base),
    tr_shipping_base = Option(enrichedEvent.tr_shipping_base),
    ti_currency = Option(enrichedEvent.ti_currency),
    ti_price_base = Option(enrichedEvent.ti_price_base),
    base_currency = Option(enrichedEvent.base_currency),
    geo_timezone = Option(enrichedEvent.geo_timezone),
    mkt_clickid = Option(enrichedEvent.mkt_clickid),
    mkt_network = Option(enrichedEvent.mkt_network),
    etl_tags = Option(enrichedEvent.etl_tags),
    dvce_sent_tstamp = Option(enrichedEvent.dvce_sent_tstamp),
    refr_domain_userid = Option(enrichedEvent.refr_domain_userid),
    refr_dvce_tstamp = Option(enrichedEvent.refr_dvce_tstamp),
    derived_contexts = Option(enrichedEvent.derived_contexts),
    domain_sessionid = Option(enrichedEvent.domain_sessionid),
    derived_tstamp = Option(enrichedEvent.derived_tstamp),
    event_vendor = Option(enrichedEvent.event_vendor),
    event_name = Option(enrichedEvent.event_name),
    event_format = Option(enrichedEvent.event_format),
    event_version = Option(enrichedEvent.event_version),
    event_fingerprint = Option(enrichedEvent.event_fingerprint),
    true_tstamp = Option(enrichedEvent.true_tstamp)
  )
}

final case class Processor(artifact: String, version: String)
object Processor {
  val default = Processor("scala-common-enrich", "0.0.0")
}

sealed trait Failure

// COLLECTOR PAYLOAD FORMAT VIOLATION

final case class CPFormatViolation(
  timestamp: Instant,
  loader: String,
  message: CPFormatViolationMessage
) extends Failure

sealed trait CPFormatViolationMessage
final case class InputDataCPFormatViolationMessage(
  payloadField: String,
  value: Option[String],
  expectation: String
) extends CPFormatViolationMessage
final case class FallbackCPFormatViolationMessage(error: String) extends CPFormatViolationMessage

// ADAPTER FAILURES

final case class AdapterFailures(
  timestamp: Instant,
  vendor: String,
  version: String,
  messages: NonEmptyList[AdapterFailure]
) extends Failure

sealed trait AdapterFailure
// tracker protocol
final case class NotJsonAdapterFailure(
  field: String,
  json: String,
  error: String
) extends AdapterFailure
final case class NotSDAdapterFailure(json: String, error: ParseError) extends AdapterFailure
final case class IgluErrorAdapterFailure(schemaKey: SchemaKey, error: ClientError)
    extends AdapterFailure
final case class SchemaCritAdapterFailure(schemaKey: SchemaKey, schemaCriterion: SchemaCriterion)
    extends AdapterFailure
// webhook adapters
final case class SchemaMappingAdapterFailure(
  actual: Option[String],
  expectedMapping: Map[String, String],
  expectation: String
) extends AdapterFailure
final case class InputDataAdapterFailure(
  field: String,
  value: Option[String],
  expectation: String
) extends AdapterFailure

// SCHEMA VIOLATIONS

sealed trait EnrichmentStageIssue

final case class SchemaViolations(timestamp: Instant, messages: NonEmptyList[SchemaViolation])
    extends Failure

sealed trait SchemaViolation extends EnrichmentStageIssue
final case class NotJsonSchemaViolation(
  field: String,
  json: String,
  error: String
) extends SchemaViolation
final case class NotSDSchemaViolation(json: String, error: ParseError) extends SchemaViolation
final case class IgluErrorSchemaViolation(schemaKey: SchemaKey, error: ClientError)
    extends SchemaViolation
final case class SchemaCritSchemaViolation(schemaKey: SchemaKey, schemaCriterion: SchemaCriterion)
    extends SchemaViolation

// ENRICHMENT FAILURES

final case class EnrichmentFailures(timestamp: Instant, messages: NonEmptyList[EnrichmentFailure])
    extends Failure

final case class EnrichmentFailure(
  enrichment: Option[EnrichmentInformation],
  message: EnrichmentFailureMessage
) extends EnrichmentStageIssue

sealed trait EnrichmentFailureMessage
final case class SimpleEnrichmentFailureMessage(error: String) extends EnrichmentFailureMessage
final case class InputDataEnrichmentFailureMessage(
  field: String,
  value: Option[String],
  expectation: String
) extends EnrichmentFailureMessage

final case class EnrichmentInformation(schemaKey: SchemaKey, identifier: String)
