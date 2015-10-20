Alchemy HTTP
==============================================

[![Build Status](https://travis-ci.org/SirWellington/alchemy-http.svg)](https://travis-ci.org/SirWellington/alchemy-http)

# Purpose
Why can't making a REST call in Java be as easy and fluid as it is for other languages?


# Requirements

* JDK 8
* Maven installation
* Thrift Compiler installation (for compilation)

# Building
To build, just run a `mvn clean install` to compile and install to your local maven repository


# Download

> This library is not yet available on Maven Central

To use, simply add the following maven dependency.

## Release
```xml
<dependency>
	<groupId>sir.wellington.commons</groupId>
	<artifactId>alchemy-arguments</artifactId>
	<version>1.0.0</version>
</dependency>
```
## Snapshot

```xml
<dependency>
	<groupId>tech.sirwellington.alchemy</groupId>
	<artifactId>alchemy-arguments</artifactId>
	<version>1.1-SNAPSHOT</version>
</dependency>


## JitPack

You can also use [JitPack.io](https://jitpack.io/#SirWellington/commons-thrift/v1.0.0).

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.SirWellington</groupId>
    <artifactId>commons-thrift</artifactId>
    <version>v1.0.0</version>
</dependency>
```

# Examples
Coming soon....

# License

This Software is licensed under the Apache 2.0 License

http://www.apache.org/licenses/LICENSE-2.0

# Release Notes

## 1.0
+ Added Json Conversion utilities
+ Added ThriftOperation interface for Thrift Services
