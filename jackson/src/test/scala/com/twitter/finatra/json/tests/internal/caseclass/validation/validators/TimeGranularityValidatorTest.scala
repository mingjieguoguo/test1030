package com.twitter.finatra.json.tests.internal.caseclass.validation.validators

import com.twitter.finatra.json.internal.caseclass.validation.validators.TimeGranularityValidator
import com.twitter.finatra.validation.ValidationResult._
import com.twitter.finatra.validation.{ErrorCode, TimeGranularity, ValidationResult, ValidatorTest}
import java.util.concurrent.TimeUnit
import org.joda.time.DateTime
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks

case class TimeGranularityNanosecondsExample(
  @TimeGranularity(TimeUnit.NANOSECONDS) timeValue: DateTime
)
case class TimeGranularityMicrosecondsExample(
  @TimeGranularity(TimeUnit.MICROSECONDS) timeValue: DateTime
)
case class TimeGranularityMillisecondsExample(
  @TimeGranularity(TimeUnit.MILLISECONDS) timeValue: DateTime
)
case class TimeGranularitySecondsExample(@TimeGranularity(TimeUnit.SECONDS) timeValue: DateTime)
case class TimeGranularityMinutesExample(@TimeGranularity(TimeUnit.MINUTES) timeValue: DateTime)
case class TimeGranularityHoursExample(@TimeGranularity(TimeUnit.HOURS) timeValue: DateTime)
case class TimeGranularityDaysExample(@TimeGranularity(TimeUnit.DAYS) timeValue: DateTime)

class TimeGranularityValidatorTest extends ValidatorTest with GeneratorDrivenPropertyChecks {

  test("pass validation for a day granularity value") {
    val dayGranularity = for {
      day <- Gen.choose[Long](1, 500)
    } yield
      new DateTime("2014-3-26T00:00:00Z").getMillis + java.util.concurrent.TimeUnit.DAYS
        .toMillis(day)

    forAll(dayGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityDaysExample](dateTimeValue) should equal(Valid)
    }
  }

  test("fail validation for an invalid day granularity value") {
    val dayInvalidGranularity = for {
      hour <- Gen.choose[Long](1, 1000).filter(_ % 24 != 0)
    } yield
      new DateTime("2014-3-26T00:00:00Z").getMillis + java.util.concurrent.TimeUnit.HOURS
        .toMillis(hour)

    forAll(dayInvalidGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityDaysExample](dateTimeValue) should equal(
        Invalid(
          errorMessage[TimeGranularityDaysExample](dateTimeValue),
          ErrorCode.InvalidTimeGranularity(dateTimeValue, TimeUnit.DAYS)
        )
      )
    }
  }

  test("pass validation for a hour granularity value") {
    val hourGranularity = for {
      hour <- Gen.choose[Long](1, 500)
    } yield
      new DateTime("2014-3-26T01:00:00Z").getMillis + java.util.concurrent.TimeUnit.HOURS
        .toMillis(hour)

    forAll(hourGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityHoursExample](dateTimeValue) should equal(Valid)
    }
  }

  test("fail validation for an invalid hour granularity value") {
    val hourInvalidGranularity = for {
      min <- Gen.choose[Long](1, 1000).filter(_ % 60 != 0)
    } yield
      new DateTime("2014-3-26T02:00:00Z").getMillis + java.util.concurrent.TimeUnit.MINUTES
        .toMillis(min)

    forAll(hourInvalidGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityHoursExample](dateTimeValue) should equal(
        Invalid(
          errorMessage[TimeGranularityHoursExample](dateTimeValue),
          ErrorCode.InvalidTimeGranularity(dateTimeValue, TimeUnit.HOURS)
        )
      )
    }
  }

  test("pass validation for a minute granularity value") {
    val minGranularity = for {
      min <- Gen.choose[Long](1, 500)
    } yield
      new DateTime("2014-3-26T01:10:00Z").getMillis + java.util.concurrent.TimeUnit.MINUTES
        .toMillis(min)

    forAll(minGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityMinutesExample](dateTimeValue) should equal(Valid)
    }
  }

  test("fail validation for an invalid minute granularity value") {
    val minInvalidGranularity = for {
      second <- Gen.choose[Long](1, 1000).filter(_ % 60 != 0)
    } yield
      new DateTime("2014-3-26T02:20:00Z").getMillis + java.util.concurrent.TimeUnit.SECONDS
        .toMillis(second)

    forAll(minInvalidGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityMinutesExample](dateTimeValue) should equal(
        Invalid(
          errorMessage[TimeGranularityMinutesExample](dateTimeValue),
          ErrorCode.InvalidTimeGranularity(dateTimeValue, TimeUnit.MINUTES)
        )
      )
    }
  }

  test("pass validation for a second granularity value") {
    val secondGranularity = for {
      second <- Gen.choose[Long](1, 500)
    } yield
      new DateTime("2014-3-26T01:10:10.000Z").getMillis + java.util.concurrent.TimeUnit.SECONDS
        .toMillis(second)

    forAll(secondGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularitySecondsExample](dateTimeValue) should equal(Valid)
    }
  }

  test("fail validation for an invalid second granularity value") {
    val secondInvalidGranularity = for {
      millis <- Gen.choose[Long](1, 999)
    } yield new DateTime("2014-3-26T02:20:20.000Z").getMillis + millis

    forAll(secondInvalidGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularitySecondsExample](dateTimeValue) should equal(
        Invalid(
          errorMessage[TimeGranularitySecondsExample](dateTimeValue),
          ErrorCode.InvalidTimeGranularity(dateTimeValue, TimeUnit.SECONDS)
        )
      )
    }
  }

  test("pass validation for a millisecond granularity value") {
    val millisGranularity = for {
      millis <- Gen.choose[Long](1, 1000)
    } yield new DateTime("2014-3-26T01:10:10.001Z").getMillis + millis

    forAll(millisGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityMillisecondsExample](dateTimeValue) should equal(Valid)
    }
  }

  test("pass validation for a microsecond granularity value") {
    val microGranularity = for {
      micro <- Gen.choose[Long](1, 1000)
    } yield
      (new DateTime("2014-3-26T01:10:10.001Z").getMillis) + java.util.concurrent.TimeUnit.MICROSECONDS
        .toMillis(micro)

    forAll(microGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityMicrosecondsExample](dateTimeValue) should equal(Valid)
    }
  }

  test("pass validation for a nanosecond granularity value") {
    val nanoGranularity = for {
      nano <- Gen.choose[Long](1, 1000)
    } yield
      (new DateTime("2014-3-26T01:10:10.001Z").getMillis) + java.util.concurrent.TimeUnit.NANOSECONDS
        .toMillis(nano)

    forAll(nanoGranularity) { millisValue =>
      val dateTimeValue = new DateTime(millisValue)
      validate[TimeGranularityNanosecondsExample](dateTimeValue) should equal(Valid)
    }
  }

  private def validate[C: Manifest](value: Any): ValidationResult = {
    super.validate(manifest[C].runtimeClass, "timeValue", classOf[TimeGranularity], value)
  }

  private def errorMessage[C: Manifest](value: DateTime): String = {
    val annotation =
      getValidationAnnotation(manifest[C].runtimeClass, "timeValue", classOf[TimeGranularity])

    TimeGranularityValidator.errorMessage(messageResolver, annotation.value(), value)
  }
}
