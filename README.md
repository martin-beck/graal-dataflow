# dataflow-graal-example

## TL;DR

A very simple project that shows how to read a class into Graal and extract the transitive set of methods that are called.

## How to build

* Import into Eclipse or use javac and add the `libs` folder into the classpath.

## How to run

* Download the Graal VM or an Graal enabled JVM
* Run with JVM parameters `-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler -XX:-UseJVMCIClassLoader`
