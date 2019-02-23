# icemd-bigdata

## Development

```bash
# run
sbt run

# format code
sbt scalafmt

# show project dependencies
sbt dependencyTree

# verify dependencies
sbt "whatDependsOn ch.qos.logback logback-classic 1.2.3"

# show outdated dependencies
sbt dependencyUpdates

# create scaladoc
sbt unidoc

# view scaladoc in browser [mac|linux]
[open|xdg-open] ./target/scala-2.12/unidoc/index.html
```

## Testing

Unit
```bash
# run tests
sbt test

# run tests in a project only
sbt "project common" test

# run single test
sbt "test:testOnly *RoutesSpec"

# debug tests (remote)
sbt test -jvm-debug 5005

# run tests, verify coverage, generate report and aggregate results
sbt clean coverage test coverageReport coverageAggregate

# view coverage report in browser [mac|linux]
[open|xdg-open] ./target/scala-2.12/scoverage-report/index.html
```

## Local deployment

Requirements

* foo
