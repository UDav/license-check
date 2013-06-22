license-check
=============

[![Build Status](https://travis-ci.org/mrice/license-check.png)](https://travis-ci.org/mrice/license-check)

What is it?
--------------
For now, license-check just checks to make sure that your Maven dependencies have s licensed declared in the Central Repo. It basically looks at each dependency and runes a query against the Central Repository to see if the dependency declares a license that [depwatch.org](http://depwatch.org) recognizes.

Isn't there already something like this?
---------------
**No, not really.** There are a few different Maven plugins for doing license "things." But the purpose of this plugin is (or, I should say, will be) to help you make sure you're not including licenses you don't want to. For now, however, all it does is make sure that all the artifacts you've included in the project actually declare a license that depwatch recognizes as one of the [opensource.org](http://www.opensource.org/) registered licenses. 

This doesn't sound like much, but it's critically important. If the license isn't recognized or isn't declared at all, it's very possible that the authors or contributors could claim fully copyright in the library and expose you to a lot of liability. 

How to use it
---------------
Add the following to your pom.xml:

```xml

<build>
  <plugins>
    <plugin>
      <groupId>org.depwatch</groupId>
      <artifactId>license-check-maven-plugin</artifactId>
      <version>0.1-PREVIEW</version>
      <executions>
        <execution>
          <phase>verify</phase>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

**NOTE:** As of this writing (late at night on 2013-06-21), the artifact hasn't been pushed into Sonatype's Central Repository. I did everything I could to get it in there, but it takes a day or two. If you happen to be seeing this over the weekend, please be patient.

Is this it?
---------------
**Absolutely not!** This is just a rough beginning. Stay tuned by signing up my [depwatch.org mailing list](http://depwatch.org). 

