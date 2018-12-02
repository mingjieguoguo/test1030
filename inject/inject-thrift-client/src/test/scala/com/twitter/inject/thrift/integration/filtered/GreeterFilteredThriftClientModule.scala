package com.twitter.inject.thrift.integration.filtered

import com.twitter.greeter.thriftscala.Greeter.{Bye, Hello, Hi}
import com.twitter.greeter.thriftscala.{Greeter, InvalidOperation}
import com.twitter.inject.thrift.filters.ThriftClientFilterBuilder
import com.twitter.inject.thrift.integration.filters.MethodLoggingTypeAgnosticFilter
import com.twitter.inject.thrift.modules.FilteredThriftClientModule
import com.twitter.util.tunable.Tunable
import com.twitter.util.{Future, Return, Throw}
import org.joda.time.Duration
import scala.util.control.NonFatal

object GreeterFilteredThriftClientModule
  extends FilteredThriftClientModule[Greeter[Future], Greeter.ServiceIface] {

  override val label = "greeter-thrift-client"
  override val dest = "flag!greeter-thrift-service"
  override val sessionAcquisitionTimeout = 1.minute.toDuration

  override def filterServiceIface(
    serviceIface: Greeter.ServiceIface,
    filter: ThriftClientFilterBuilder
  ) = {

    val timeoutTunable = Tunable.emptyMutable[Duration]("id")
    timeoutTunable.set(new Duration(60000)) // 1.minute

    serviceIface.copy(
      hi = filter
        .method(Hi)
        .withAgnosticFilter(new MethodLoggingTypeAgnosticFilter())
        .withMethodLatency
        .withConstantRetry(
          shouldRetry = {
            case (_, Throw(InvalidOperation(_))) => true
            case (_, Return(success)) => success == "ERROR"
            case (_, Throw(NonFatal(_))) => true
          },
          start = 50.millis,
          retries = 3
        )
        .withRequestTimeout(1.minute)
        .filtered(new HiThriftClientFilter)
        .andThen(serviceIface.hi),
      hello = filter
        .method(Hello)
        .withAgnosticFilter(new MethodLoggingTypeAgnosticFilter())
        .withMethodLatency
        .withConstantRetry(
          shouldRetry = {
            case (_, Throw(InvalidOperation(_))) => true
            case (_, Return(success)) => success == "ERROR"
            case (_, Throw(NonFatal(_))) => true
          },
          start = 50.millis,
          retries = 3
        )
        .withRequestTimeout(1.minute)
        .filtered(new HelloThriftClientFilter)
        .andThen(serviceIface.hello),
      bye = filter
        .method(Bye)
        .withAgnosticFilter[MethodLoggingTypeAgnosticFilter]
        .withMethodLatency
        .withExponentialRetry(
          shouldRetryResponse = PossiblyRetryableExceptions,
          start = 50.millis,
          multiplier = 2,
          retries = 3
        )
        .withRequestLatency
        .withRequestTimeout(timeoutTunable)
        .filtered[ByeThriftClientFilter]
        .andThen(serviceIface.bye)
    )
  }
}
