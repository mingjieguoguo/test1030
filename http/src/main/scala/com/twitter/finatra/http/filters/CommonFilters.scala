package com.twitter.finatra.http.filters

import com.twitter.finagle.http.Request
import com.twitter.finatra.filters.MergedFilter
import javax.inject.{Inject, Singleton}

/**
 * A typical collection of Filters for HTTP services. Ordering of Filters is important.
 *
 * @note Filter ordering is determined by the implementation of [[MergedFilter]] and can be
 *       read as Requests enter the top Filter and progress down, Responses traverse in the
 *       opposite manner from the bottom up.
 *
 * @note [[HttpNackFilter]] converts Finagle's nacks into `HttpNackExceptions`. This Filter MUST
 *      come "below" the [[ExceptionMappingFilter]] otherwise the `HttpNackExceptions` will not be
 *      properly converted into a meaningful HTTP response.
 */
@Singleton
class CommonFilters @Inject()(
  a: StatsFilter[Request],
  b: AccessLoggingFilter[Request],
  c: HttpResponseFilter[Request],
  d: ExceptionMappingFilter[Request],
  e: HttpNackFilter[Request]
) extends MergedFilter(a, b, c, d, e)
