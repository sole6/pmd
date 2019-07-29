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
For the changes, see [PMD Designer Changelog](https://github.com/pmd/pmd-designer/releases/tag/6.17.0).
It contains a new feature to edit test cases directly within the designer. Any feedback is highly appreciated.

#### Lua support

Thanks to the contribution from [Maikel Steneker](https://github.com/maikelsteneker), and built on top of the ongoing efforts to fully support Antlr-based languages,
PMD now has CPD support for [Lua](https://www.lua.org/).

Being based on a proper Antlr grammar, CPD can:
*   ignore comments
*   honor [comment-based suppressions](pmd_userdocs_cpd.html#suppression)

#### Modified Rules

*   The Java rule {% rule "java/errorprone/CloseResource" %} (`java-errorprone`) ignores now by default
    `java.io.ByteArrayInputStream` and `java.io.CharArrayWriter`. Such streams/writers do not need to be closed.

*   The Java rule {% rule "java/errorprone/MissingStaticMethodInNonInstantiatableClass" %} (`java-errorprone`) has now
    the new property `annotations`.
    When one of the private constructors is annotated with one of the annotations, then the class is not considered
    non-instantiatable anymore and no violation will be reported. By default, Spring's `@Autowired` and
    Java EE's `@Inject` annotations are recognized.

### Fixed Issues

*   core
    *   [#1913](https://github.com/pmd/pmd/issues/1913): \[core] "-help" CLI option ends with status code != 0
*   doc
    *   [#1896](https://github.com/pmd/pmd/issues/1896): \[doc] Error in changelog 6.16.0 due to not properly closed rule tag
    *   [#1898](https://github.com/pmd/pmd/issues/1898): \[doc] Incorrect code example for DoubleBraceInitialization in documentation on website
    *   [#1906](https://github.com/pmd/pmd/issues/1906): \[doc] Broken link for adding own CPD languages
    *   [#1909](https://github.com/pmd/pmd/issues/1909): \[doc] Sample usage example refers to deprecated ruleset "basic.xml" instead of "quickstart.xml"
*   java
    *   [#1910](https://github.com/pmd/pmd/issues/1910): \[java] ATFD calculation problem
*   java-errorprone
    *   [#1749](https://github.com/pmd/pmd/issues/1749): \[java] DD False Positive in DataflowAnomalyAnalysis
    *   [#1832](https://github.com/pmd/pmd/issues/1832): \[java] False positives for MissingStaticMethodInNonInstantiatableClass when DI is used
    *   [#1921](https://github.com/pmd/pmd/issues/1921): \[java] CloseResource false positive with ByteArrayInputStream
*   java-multithreading
    *   [#1903](https://github.com/pmd/pmd/issues/1903): \[java] UnsynchronizedStaticFormatter doesn't allow block-level synchronization when using allowMethodLevelSynchronization=true
*   plsql
    *   [#1902](https://github.com/pmd/pmd/issues/1902): \[pslql] ParseException when parsing (+)
*   xml
    *   [#1666](https://github.com/pmd/pmd/issues/1666): \[xml] wrong cdata rule description and examples

### External Contributions

*   [#1869](https://github.com/pmd/pmd/pull/1869): \[xml] fix #1666 wrong cdata rule description and examples - [Artem](https://github.com/KroArtem)
*   [#1892](https://github.com/pmd/pmd/pull/1892): \[lua] \[cpd] Added CPD support for Lua - [Maikel Steneker](https://github.com/maikelsteneker)
*   [#1905](https://github.com/pmd/pmd/pull/1905): \[java] DataflowAnomalyAnalysis Rule in right order - [YoonhoChoi96](https://github.com/YoonhoChoi96)
*   [#1908](https://github.com/pmd/pmd/pull/1908): \[doc] Update ruleset filename from deprecated basic.xml to quickstart.xml - [crunsk](https://github.com/crunsk)
*   [#1916](https://github.com/pmd/pmd/pull/1916): \[java] Exclude Autowired and Inject for MissingStaticMethodInNonInstantiatableClass - [AnthonyKot](https://github.com/AnthonyKot)
*   [#1917](https://github.com/pmd/pmd/pull/1917): \[core] Add 'no error' return option, and assign it to the cli when the help command is invoked - [Renato Oliveira](https://github.com/renatoliveira)

{% endtocmaker %}

