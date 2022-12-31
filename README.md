## Kotlin Gradle CDK Template

When using the AWS CDK example projects, all the samples are written in Java and built using *maven*. This template
provides a starter project for writing in Kotlin, and building with *gradle* instead.

The project is split into two components - the main project, which holds the AWS Stack, and a submodule, which contains
a very basic Lambda function.

The gradle `run` task is configured to be dependent on the `shadowJar` task of the Lambda submodule, so that the Lambda
is always recompiled before the CDK synthesis takes place (as `cdk synth` builds and executes the main project class).

As this relies on AWS CDK, you must have `node` installed.