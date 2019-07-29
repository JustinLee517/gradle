The Gradle team is excited to announce Gradle @version@.

This release features [improvements to make Groovy compilation faster](#groovy-improvements), a new plugin for [Java test fixtures](#test-fixtures) and [better management of plugin versions](#improved-plugin-management) in multi-project builds.

We would like to thank the following community contributors to this release of Gradle:
<!-- 
Include only their name, impactful features should be called out separately below.
 [Some person](https://github.com/some-person)
-->

<!-- 
## Cancellable custom tasks

When a build is cancelled (e.g. using CTRL+C), the threads executing each task are interrupted.
Task authors only need to make their tasks respond to interrupts in order for the task to be cancellable.

details of 1

## 2

details of 2

## n
-->

## Upgrade Instructions

Switch your build to use Gradle @version@ by updating your wrapper:

`./gradlew wrapper --gradle-version=@version@`

See the [Gradle 5.x upgrade guide](userguide/upgrading_version_5.html#changes_@baseVersion@) to learn about deprecations, breaking changes and other considerations when upgrading to Gradle @version@.

<!-- Do not add breaking changes or deprecations here! Add them to the upgrade guide instead. --> 

New methods `Provider.orElse(T)` and `Provider.orElse(Provider<T>)` has been added. These allow you to perform an 'or' operation on a provider and some other value.

### Managed nested properties

A custom type, such as a task type, plugin or project extension can be implemented as an abstract class or, in the case of project extensions and other data types, an interface.
Under some conditions, Gradle can provide an implementation for abstract properties.

Now, if the custom type has an abstract getter annotated with `@Nested`, Gradle will provide an implementation for the getter method and also create a value for the property.
See the [user manual](userguide/custom_gradle_types.html#sec:managed_nested_properties) for more information.

## Promoted features
Promoted features are features that were incubating in previous versions of Gradle but are now supported and subject to backwards compatibility.
See the User Manual section on the “[Feature Lifecycle](userguide/feature_lifecycle.html)” for more information.

The following are the features that have been promoted in this Gradle release.

<!--
### Example promoted
-->

## Fixed issues

## Known issues

Known issues are problems that were discovered post release that are directly related to changes made in this release.

## External contributions

We love getting contributions from the Gradle community. For information on contributing, please see [gradle.org/contribute](https://gradle.org/contribute).

## Reporting Problems

If you find a problem with this release, please file a bug on [GitHub Issues](https://github.com/gradle/gradle/issues) adhering to our issue guidelines. 
If you're not sure you're encountering a bug, please use the [forum](https://discuss.gradle.org/c/help-discuss).

We hope you will build happiness with Gradle, and we look forward to your feedback via [Twitter](https://twitter.com/gradle) or on [GitHub](https://github.com/gradle).
