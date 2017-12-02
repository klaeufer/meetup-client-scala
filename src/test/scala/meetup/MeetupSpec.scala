package edu.luc.etl.connectorspace.meetup

import org.specs2.mutable._

class MeetupSpec extends Specification {

  val apiKey = sys.env(KeyApiKey)
  require { apiKey != null }

  "The 'Hello world' string" should {
    "contain 11 characters" in {
      "Hello world" must have size (11)
    }
  }
}
