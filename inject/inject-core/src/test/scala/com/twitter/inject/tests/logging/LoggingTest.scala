package com.twitter.inject.tests.logging

import com.twitter.inject.{Logging, Test}
import com.twitter.util.Future

class LoggingTest extends Test with Logging {

  test("Logging") {
    debug("a")
    warn("a")
    info("a")
    trace("a")

    debugResult("%s") { "a" }
    warnResult("%s") { "a" }
    infoResult("%s") { "a" }
    errorResult("%s") { "a" }
    debugFutureResult("%s") {
      Future("a")
    }

    time("time %s ms") {
      1 + 2
    }

    intercept[RuntimeException] {
      time("error time %s ms") {
        throw new RuntimeException("oops")
      }
    }
  }
}
