# [Finatra][finatra] Examples

![finatra logo](../finatra_logo.png)

A collection of example [Finatra][finatra] applications.

## Details
For detailed information see the README.md within each
example project. Every project builds with [sbt](http://www.scala-sbt.org/) and
at least one example which also uses [Maven](http://maven.apache.org).


## Building and Running

### To Build
If you want to build/run the examples from the `develop` branch you will need to
make sure to follow the instructions in the
[CONTRIBUTING.md](../CONTRIBUTING.md) documentation for building SNAPSHOT
versions of Finatra Twitter OSS dependencies along with building Finatra itself.

This is because in the `develop` branch the examples depend on `SNAPSHOT` versions
of the Twitter OSS libraries which you will need to build locally.

To accomplish this easily:

```
curl -s https://raw.githubusercontent.com/twitter/dodo/develop/bin/build | bash -s -- --no-test --include finatra
```

This commands differs from the [CONTRIBUTING.md](../CONTRIBUTING.md) documentation in
that it will also *include* building and publishing Finatra locally from the Finatra
`develop` branch.

In the `master` branch, you should be able run the examples without building any dependencies 
as `master` uses the released versions of the Twitter OSS dependencies.

### To Run
Follow the instructions in the project's `README.md` for how to run the server.

## More information
For more information please see our [User Guide](https://twitter.github.io/finatra/user-guide/)
or browse the [source code][finatra] (tests are especially good sources of information and examples).

[finatra]: https://github.com/twitter/finatra
