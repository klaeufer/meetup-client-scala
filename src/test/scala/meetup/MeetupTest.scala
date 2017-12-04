package edu.luc.etl.connectorspace.meetup

import com.github.nscala_time.time.Imports._
import org.specs2.mutable._

class MeetupTest extends Specification {

  "An Effort instance" should {
    "return its attributes" in {
      val fromDate = DateTime.lastMonth
      val toDate = DateTime.now
      val duration = 5.hours
      val fixture = Effort(fromDate, toDate, duration)
      (fixture.from must be equalTo (fromDate)) and
        (fixture.to must be equalTo (toDate)) and
        (fixture.duration must be equalTo (duration))
    }
  }
}
