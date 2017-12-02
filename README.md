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

# How to run

1. Visit https://secure.meetup.com/meetup_api/oauth_consumers/ to create a Meetup OAuth2 consumer with redirect URI http://localhost:8080.
1. Then create a `.env` file in the project root containing your OAuth consumer credentials (without quotes):

        MEETUP_CLIENT_ID=<key>
        MEETUP_CLIENT_SECRET=<secret>
1. Pull the credentials from the `.env` file into the environment:

        set -o allexport
        . .env
        set +o allexport
1. Now compile the code and create the scripts.

        sbt stage
1. Then obtain an OAuth2 access token by running

        ./target/universal/stage/bin/meetup-client-scala -a
   This should redirect you to your web browser so you can authenticate through Meetup.
1. Now you can run the actual command-line connector. E.g.

        ./target/universal/stage/bin/meetup-client-scala -c -f 2015-01-01
    will compute how much time you spent at meetups since the given date.

1. You can also run the connector as a web service

        ./target/universal/stage/bin/meetup-client-scala -s
    and connect to it through a user agent

        curl -H "Authorization: Bearer <OAuth2 access token>" localhost:8080/effort?from=2015-01-01
        {"from":1420092000000,"to":1512248480088,"effort":45900000}

# Example output

    found 68 events total
    found  5 events last year
    spent a total of 7.25 hours at events last year

# Functional requirements

- query a user's (past) events based on certain criteria
  - within a given date/time interval
  - by a group on a shortlist of groups of interest
  - etc.
- represent results using suitable model classes
- aggregate information about collections of events
  - total time spent at events
  - time spent at events per group
  - etc.

# Nonfunctional requirements

- static quality attributes: testability, maintainability, etc.
- dynamic quality attributes: performance, scalability, reliability, etc.
- frictionless solution stack based on best practices and community support

# Learning objectives

- provide exemplars of API client connectors suitable for learning
- include basic, easy-to-understand blocking implementations as starting points
- include advanced nonblocking/async implementations as next steps
- maintain a polyglot mindset by focusing on patterns

# Dependencies/solution stack

- Play framework (Scala) including
  - play-ahc-ws (HTTP client)
  - play-akka-http-server
  - play-json
- nscala-time: Scala wrapper for joda-time
- scopt: command-line options)
- specs2: testing including JSON matchers

# References

http://code.google.com/p/meetup-java-client