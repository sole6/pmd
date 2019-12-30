---
title: PMD Release Notes
permalink: pmd_release_notes.html
keywords: changelog, release notes
---

## {{ site.pmd.date }} - {{ site.pmd.version }}

The PMD team is pleased to announce PMD {{ site.pmd.version }}.

This is a {{ site.pmd.release_type }} release.

{% tocmaker is_release_notes_processor %}

### New and noteworthy

#### Updated PMD Designer

This PMD release ships a new version of the pmd-designer.
For the changes, see [PMD Designer Changelog](https://github.com/pmd/pmd-designer/releases/tag/6.19.0).

#### Java Metrics

*   The new metric "Class Fan Out Complexity" has been added. See
    [Java Metrics Documentation](pmd_java_metrics_index.html#class-fan-out-complexity-class_fan_out) for details.


#### Modified Rules

*   The Java rules {% rule "java/errorprone/InvalidLogMessageFormat" %} and {% rule "java/errorprone/MoreThanOneLogger" %}
    (`java-errorprone`) now both support [Log4j2](https://logging.apache.org/log4j/2.x/). Note that the
    rule "InvalidSlf4jMessageFormat" has been renamed to "InvalidLogMessageFormat" to reflect the fact, that it now
    supports more than slf4j.

*   The Java rule {% rule "java/design/LawOfDemeter" %} (`java-design`) ignores now also Builders, that are
    not assigned to a local variable, but just directly used within a method call chain. The method, that creates
    the builder needs to end with "Builder", e.g. `newBuilder()` or `initBuilder()` works. This change
    fixes a couple of false positives.

*   The Java rule {% rule "java/errorprone/DataflowAnomalyAnalysis" %} (`java-errorprone`) doesn't check for
    UR anomalies (undefined and then referenced) anymore. These checks were all false-positives, since actual
    UR occurrences would lead to compile errors.

*   The java rule {% rule "java/multithreading/DoNotUseThreads" %} (`java-multithreading`) has been changed
    to not report usages of `java.lang.Runnable` anymore. Just using `Runnable` does not automatically create
    a new thread. While the check for `Runnable` has been removed, the rule now additionally checks for
    usages of `Executors` and `ExecutorService`. Both create new threads, which are not managed by a J2EE
    server.

#### Renamed Rules

*   The Java rule {% rule "java/errorprone/InvalidSlf4jMessageFormat" %} has been renamed to
    {% rule "java/errorprone/InvalidLogMessageFormat" %} since it supports now both slf4j and log4j2
    message formats.

### Fixed Issues

*   core
    *   [#1978](https://github.com/pmd/pmd/issues/1978): \[core] PMD fails on excluding unknown rules
    *   [#2014](https://github.com/pmd/pmd/issues/2014): \[core] Making add(SourceCode sourceCode) public for alternative file systems
    *   [#2020](https://github.com/pmd/pmd/issues/2020): \[core] Wrong deprecation warnings for unused XPath attributes
    *   [#2036](https://github.com/pmd/pmd/issues/2036): \[core] Wrong include/exclude patterns are silently ignored
    *   [#2048](https://github.com/pmd/pmd/issues/2048): \[core] Enable type resolution by default for XPath rules
    *   [#2067](https://github.com/pmd/pmd/issues/2067): \[core] Build issue on Windows
    *   [#2068](https://github.com/pmd/pmd/pull/2068): \[core] Rule loader should use the same resources loader for the ruleset
    *   [#2071](https://github.com/pmd/pmd/issues/2071): \[ci] Add travis build on windows
    *   [#2072](https://github.com/pmd/pmd/issues/2072): \[test]\[core] Not enough info in "test setup error" when numbers of lines do not match
    *   [#2082](https://github.com/pmd/pmd/issues/2082): \[core] Incorrect logging of deprecated/renamed rules
*   java
    *   [#2042](https://github.com/pmd/pmd/issues/2042): \[java] PMD crashes with ClassFormatError: Absent Code attribute...
*   java-bestpractices
    *   [#1531](https://github.com/pmd/pmd/issues/1531): \[java] UnusedPrivateMethod false-positive with method result
    *   [#2025](https://github.com/pmd/pmd/issues/2025): \[java] UnusedImports when @see / @link pattern includes a FQCN
*   java-codestyle
    *   [#2017](https://github.com/pmd/pmd/issues/2017): \[java] UnnecessaryFullyQualifiedName triggered for inner class
*   java-design
    *   [#1912](https://github.com/pmd/pmd/issues/1912): \[java] Metrics not computed correctly with annotations
*   java-errorprone
    *   [#336](https://github.com/pmd/pmd/issues/336): \[java] InvalidSlf4jMessageFormat applies to log4j2
    *   [#1636](https://github.com/pmd/pmd/issues/1636): \[java] Stop checking UR anomalies for DataflowAnomalyAnalysis
*   java-multithreading
    *   [#1627](https://github.com/pmd/pmd/issues/1627): \[java] DoNotUseThreads should not warn on Runnable
*   doc
    * [#2058](https://github.com/pmd/pmd/issues/2058): \[doc] CLI reference for `-norulesetcompatibility` shows a boolean default value


### API Changes

#### Deprecated APIs

##### For removal

* pmd-core
  * All the package {% jdoc_package core::dcd %} and its subpackages. See {% jdoc core::dcd.DCD %}.
  * In {% jdoc core::lang.LanguageRegistry %}:
    * {% jdoc core::lang.LanguageRegistry#commaSeparatedTerseNamesForLanguageVersion(List) %}
    * {% jdoc core::lang.LanguageRegistry#commaSeparatedTerseNamesForLanguage(List) %}
    * {% jdoc core::lang.LanguageRegistry#findAllVersions() %}
    * {% jdoc core::lang.LanguageRegistry#findLanguageVersionByTerseName(String) %}
    * {% jdoc core::lang.LanguageRegistry#getInstance() %}
  * {% jdoc !!core::RuleSet#getExcludePatterns() %}. Use the new method {% jdoc core::RuleSet#getFileExclusions() %} instead.
  * {% jdoc !!core::RuleSet#getIncludePatterns() %}. Use the new method {% jdoc core::RuleSet#getFileInclusions() %} instead.
  * {% jdoc !!core::lang.Parser#canParse() %}
  * {% jdoc !!core::lang.Parser#getSuppressMap() %}
  * {% jdoc !!core::rules.RuleBuilder#RuleBuilder(String,String,String) %}. Use the new constructor with the correct ResourceLoader instead.
  * {% jdoc !!core::rules.RuleFactory#RuleFactory() %}. Use the new constructor with the correct ResourceLoader instead.
* pmd-java
  * {% jdoc java::lang.java.ast.CanSuppressWarnings %} and its implementations
  * {% jdoc java::lang.java.rule.AbstractJavaRule#isSuppressed(Node) %}
  * {% jdoc java::lang.java.rule.AbstractJavaRule#getDeclaringType(Node) %}.
  * {% jdoc java::lang.java.rule.JavaRuleViolation#isSupressed(Node,Rule) %}
  * {% jdoc java::lang.java.ast.ASTMethodDeclarator %}
  * {% jdoc java::lang.java.ast.ASTMethodDeclaration#getMethodName() %}
  * {% jdoc java::lang.java.ast.ASTMethodDeclaration#getBlock() %}
  * {% jdoc java::lang.java.ast.ASTConstructorDeclaration#getParameterCount() %}
* pmd-apex
  * {% jdoc apex::lang.apex.ast.CanSuppressWarnings %} and its implementations
  * {% jdoc apex::lang.apex.rule.ApexRuleViolation#isSupressed(Node,Rule) %}

##### Internal APIs

* pmd-core
  * All the package {% jdoc_package core::util %} and its subpackages,
  except {% jdoc_package core::util.datasource %} and {% jdoc_package core::util.database %}.
  * {% jdoc core::cpd.GridBagHelper %}
  * {% jdoc core::renderers.ColumnDescriptor %}



### External Contributions

*   [#2010](https://github.com/pmd/pmd/pull/2010): \[java] LawOfDemeter to support inner builder pattern - [Gregor Riegler](https://github.com/gregorriegler)
*   [#2012](https://github.com/pmd/pmd/pull/2012): \[java] Fixes 336, slf4j log4j2 support - [Mark Hall](https://github.com/markhall82)
*   [#2032](https://github.com/pmd/pmd/pull/2032): \[core] Allow adding SourceCode directly into CPD - [Nathan Braun](https://github.com/nbraun-Google)
*   [#2047](https://github.com/pmd/pmd/pull/2047): \[java] Fix computation of metrics with annotations - [Andi Pabst](https://github.com/andipabst)
*   [#2065](https://github.com/pmd/pmd/pull/2065): \[java] Stop checking UR anomalies - [Carlos Macasaet](https://github.com/l0s)
*   [#2068](https://github.com/pmd/pmd/pull/2068): \[core] Rule loader should use the same resources loader for the ruleset - [Chen Yang](https://github.com/willamette)
*   [#2070](https://github.com/pmd/pmd/pull/2070): \[core] Fix renderer tests for windows builds - [Saladoc](https://github.com/Saladoc)
*   [#2073](https://github.com/pmd/pmd/pull/2073): \[test]\[core] Add expected and actual line of numbers to message wording - [snuyanzin](https://github.com/snuyanzin)
*   [#2076](https://github.com/pmd/pmd/pull/2076): \[java] Add Metric ClassFanOutComplexity - [Andi Pabst](https://github.com/andipabst)
*   [#2078](https://github.com/pmd/pmd/pull/2078): \[java] DoNotUseThreads should not warn on Runnable #1627 - [Michael Clay](https://github.com/mclay)

{% endtocmaker %}

