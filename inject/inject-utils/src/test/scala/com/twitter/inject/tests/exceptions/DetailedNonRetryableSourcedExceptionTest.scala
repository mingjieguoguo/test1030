package com.twitter.inject.tests.exceptions

import com.twitter.inject.Test
import com.twitter.inject.exceptions.DetailedNonRetryableSourcedException

class DetailedNonRetryableSourcedExceptionTest extends Test {

  test("DetailedNonRetryableSourcedException#log details with no source") {
    val e = new DetailedNonRetryableSourcedException("Non-retryable exception occurred.")
    e.toDetailsString should be(Seq(e.getClass.getSimpleName).mkString("/"))
  }

  test("DetailedNonRetryableSourcedException#log details with source") {
    val detailedNonRetryableSource = "SomeProject"
    val e = new DetailedNonRetryableSourcedException("Non-retryable exception occurred.") {
      override val source = detailedNonRetryableSource
    }

    e.toDetailsString should be(
      s"$detailedNonRetryableSource/" + Seq(e.getClass.getSimpleName).mkString("/")
    )
  }

  test("DetailedNonRetryableSourcedException#toString") {
    val e = new DetailedNonRetryableSourcedException("Non-retryable exception occurred.")
    e.toString should be(
      "com.twitter.inject.exceptions.DetailedNonRetryableSourcedException: Non-retryable exception occurred."
    )
  }

  test("DetailedNonRetryableSourcedException#toString with Product") {
    val e = new TestException("Non-retryable exception occurred.")
    e.toString should be("TestException(Non-retryable exception occurred.)")
  }
}

case class TestException(override val message: String)
    extends DetailedNonRetryableSourcedException(message)
