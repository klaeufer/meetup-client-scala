[![Build Status](https://travis-ci.org/klaeufer/meetup-client-scala.svg?branch=master)](https://travis-ci.org/klaeufer/meetup-client-scala)
[![codecov](https://codecov.io/gh/klaeufer/meetup-client-scala/branch/master/graph/badge.svg)](https://codecov.io/gh/klaeufer/meetup-client-scala)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/30fc9e6008694421af22617542109007)](https://www.codacy.com/app/laufer/meetup-client-scala?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=klaeufer/meetup-client-scala&amp;utm_campaign=Badge_Grade)

[![Stories in Ready](https://badge.waffle.io/klaeufer/meetup-client-scala.png?label=ready&title=Ready)](http://waffle.io/klaeufer/meetup-client-scala)
[![Issue Stats](http://issuestats.com/github/klaeufer/meetup-client-scala/badge/pr)](http://issuestats.com/github/klaeufer/meetup-client-scala)
[![Issue Stats](http://issuestats.com/github/klaeufer/meetup-client-scala/badge/issue)](http://issuestats.com/github/klaeufer/meetup-client-scala)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/klaeufer/meetup-client-scala.svg)](http://isitmaintained.com/project/klaeufer/meetup-client-scala "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/klaeufer/meetup-client-scala.svg)](http://isitmaintained.com/project/klaeufer/meetup-client-scala "Percentage of issues still open")

# meetup-client-scala

Aims to provide a simple but useful Scala client to communicate with [Meetup API](https://www.meetup.com/meetup_api/clients) and parse the results into Scala case class instances.
See also the [Meetup API v3 documentation](https://www.meetup.com/meetup_api/docs).

*Currently in the exploration stage.*

# Objectives

- query a user's (past) events based on certain criteria
  - within a given date/time interval
  - by a group on a shortlist of groups of interest
  - etc.
- represent results using suitable model classes
- aggregate information about collections of events
  - total time spent at events
  - time spent at events per group
  - etc.

# Example output

```
found 68 events total
found 5 events last year
spent a total of 7.25 hours at events last year
```

# Dependencies

- play-ahc-ws (standalone)
- play-json (standalone)
- nscala-time (Scala wrapper for joda-time)

# References

http://code.google.com/p/meetup-java-client