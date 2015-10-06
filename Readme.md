Commons Library for Apache Thrift
==============================================

[![Build Status](https://travis-ci.org/SirWellington/commons-thrift.svg)](https://travis-ci.org/SirWellington/commons-thrift)

# Purpose
This Library makes it easier to work with Thrift in Java by managing Thrift Clients, providing for simple serialization and deserialization of Thrift Objects.
This saves some boilerplate code. 

It is a design goal of this project to cover a few common use-cases really well, rather than trying to cover every possible case. 
As such this project is designed with the following in mind:


# Requirements

* JDK 8
* Maven installation
* Thrift Compiler installation (for compilation)

# Building
This project requires both maven and thrift to be on the system `PATH`. To build, just run a `mvn clean install` to compile and install to your local maven repository


# Download

> This library is not yet available on Maven Central

To use, simply add the following maven depedencncy.

## Release

```xml
<dependency>
	<groupId>sir.wellington.commons</groupId>
	<artifactId>commons-thrift</artifactId>
	<version>1.0.0</version>
</dependency>
```


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

# Release Notes

## 1.0.0
+ Added Json Conversion utilities
+ Added ThriftOperation interface for Thrift Services