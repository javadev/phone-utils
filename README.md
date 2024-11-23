# phone-utils
A convenient phone library helping to work with phone numbers.

[![Maven Central](https://img.shields.io/maven-central/v/com.github.javadev/phone-utils.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.javadev%22%20AND%20a%3A%22phone-utils%22)
[![MIT License](http://img.shields.io/badge/license-MIT-green.svg) ](https://github.com/javadev/phone-utils/blob/master/LICENSE)
[![Java CI](https://github.com/javadev/phone-utils/actions/workflows/maven.yml/badge.svg)](https://github.com/javadev/phone-utils/actions/workflows/maven.yml)
[![codecov](https://codecov.io/github/javadev/phone-utils/graph/badge.svg?token=bvL8ftHG7n)](https://codecov.io/github/javadev/phone-utils)

Requirements
============

Java 1.8 and later.

## Installation

Include the following in your `pom.xml` for Maven:

```
<dependencies>
  <dependency>
    <groupId>com.github.javadev</groupId>
    <artifactId>phone-utils</artifactId>
    <version>1.0</version>
  </dependency>
  ...
</dependencies>
```

Gradle:

```groovy
compile 'com.github.javadev:phone-utils:1.0'
```

## Definitions

Definitions of vocabulary used in project 

* Country code - Integer. Examples: 380, 47, 46, etc
* National number - String. (+47)45037118, (+380)985777268
* Full phone number: + Country code and national number
* Valid phone number: means that Google library says that this is real number according to Telecom rules
* Possible number: semantically incorrect, but syntactically correct numbers. 

## Examples

### Check if number is valid
```
$ isValidPhoneNumber("+47", "45 45 45 45") => true
$ isValidPhoneNumber("+47", "85 45 45 45") => false
```

### Check if possible number
```
$ isPossibleFullPhoneNumber("+4736985214") => true
$ isPossibleFullPhoneNumber("+473698521fdsd4") => false
```

### Normalize number
```
$ normalizePhoneNumber("+1 (650) - 713 (9923)") => +16507139923
```

## Check if number has given country code (for example: Norway)
```
$ hasCountryCode(47, "+4736985214") => true
$ hasCountryCode(46, "+4736985214") => false
$ hasCountryCode(47, "+478587845454545") => false (not possible number)
```

## Check valid norwegian number
```
$ isValidNorwegianPhoneNumber("45037118") => true
$ isValidNorwegianPhoneNumber("906 (30) 185") => true
$ isValidNorwegianPhoneNumber("+47 906 (30) 185") => true
$ isValidNorwegianPhoneNumber("+4790630185") => true
 
$ isValidNorwegianPhoneNumber("800185") => false
$ isValidNorwegianPhoneNumber("80630185") => false
$ isValidNorwegianPhoneNumber("+47 80630185") => false
```

## Generate valid full norwegian number
```
$ generateFullNorwegianPhoneNumber("45037118") => +4745037118
$ generateFullNorwegianPhoneNumber("906 (30) 185") => +4790630185
$ generateFullNorwegianPhoneNumber("+47 906 (30) 185") => +4790630185
```

