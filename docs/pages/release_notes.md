---
title: PMD Release Notes
permalink: pmd_release_notes.html
keywords: changelog, release notes
---

## {{ site.pmd.date }} - {{ site.pmd.version }}

The PMD team is pleased to announce PMD {{ site.pmd.version }}.

This is a {{ site.pmd.release_type }} release.

{% tocmaker is_release_notes_processor %}

### Fixed Issues

*   apex
    *   [#2092](https://github.com/pmd/pmd/issues/2092): \[apex] ApexLexer logs visible when Apex is the selected language upon starting the designer
    *   [#2136](https://github.com/pmd/pmd/issues/2136): \[apex] Provide access to underlying query of SoqlExpression
*   core
    *   [#2002](https://github.com/pmd/pmd/issues/2002): \[doc] Issue with http://pmdapplied.com/ linking to a gambling Web site
    *   [#2062](https://github.com/pmd/pmd/issues/2062): \[core] Shortnames parameter does not work with Ant
    *   [#2090](https://github.com/pmd/pmd/issues/2090): \[ci] Release notes and draft releases
    *   [#2096](https://github.com/pmd/pmd/issues/2096): \[core] Referencing category errorprone.xml produces deprecation warnings for InvalidSlf4jMessageFormat
*   java
    *   [#1861](https://github.com/pmd/pmd/issues/1861): \[java] Be more lenient with version numbers
    *   [#2105](https://github.com/pmd/pmd/issues/2105): \[java] Wrong name for inner classes in violations
*   java-bestpractices
    *   [#2016](https://github.com/pmd/pmd/issues/2016): \[java] UnusedImports: False positive if wildcard is used and only static methods
*   java-codestyle
    *   [#1362](https://github.com/pmd/pmd/issues/1362): \[java] LinguisticNaming flags Predicates with boolean-style names
    *   [#2029](https://github.com/pmd/pmd/issues/2029): \[java] UnnecessaryFullyQualifiedName false-positive for non-static nested classes
    *   [#2098](https://github.com/pmd/pmd/issues/2098): \[java] UnnecessaryFullyQualifiedName: regression / false positive
*   java-design
    *   [#2075](https://github.com/pmd/pmd/issues/2075): \[java] ImmutableField false positive with inner class
    *   [#2125](https://github.com/pmd/pmd/issues/2125): \[java] ImmutableField: False positive when variable is updated in conditional loop
*   java-errorprone
    *   [#2102](https://github.com/pmd/pmd/issues/2102): \[java] False positive MissingStaticMethodInNonInstantiatableClass when inheritors are instantiable

### External Contributions

*   [#2088](https://github.com/pmd/pmd/pull/2088): \[java] Add more version shortcuts for older java - [Henning Schmiedehausen](https://github.com/hgschmie)
*   [#2089](https://github.com/pmd/pmd/pull/2089): \[core] Minor unrelated improvements to code - [Gonzalo Exequiel Ibars Ingman](https://github.com/gibarsin)
*   [#2091](https://github.com/pmd/pmd/pull/2091): \[core] Fix pmd warnings (IdenticalCatchCases) - [Gonzalo Exequiel Ibars Ingman](https://github.com/gibarsin)
*   [#2106](https://github.com/pmd/pmd/pull/2106): \[java] Wrong name for inner classes - [Andi Pabst](https://github.com/andipabst)
*   [#2121](https://github.com/pmd/pmd/pull/2121): \[java] Predicates treated like booleans - [Ozan Gulle](https://github.com/ozangulle)

{% endtocmaker %}

