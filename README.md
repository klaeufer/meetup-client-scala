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

# Dependencies

- play-ahc-ws (standalone)
- play-json (standalone)
- nscala-time (Scala wrapper for joda-time)

# References

http://code.google.com/p/meetup-java-client