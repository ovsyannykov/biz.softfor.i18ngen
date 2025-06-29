[![GitHub License](https://img.shields.io/github/license/ovsyannykov/biz.softfor.i18ngen)](license.md)
[![Maven Central Version](https://img.shields.io/maven-central/v/biz.softfor/biz.softfor.spring.i18ngen)](https://mvnrepository.com/artifact/biz.softfor/biz.softfor.spring.i18ngen)
[![Java CI with Maven](https://github.com/ovsyannykov/biz.softfor.i18ngen/actions/workflows/maven.yml/badge.svg)](https://github.com/ovsyannykov/biz.softfor.i18ngen/actions/workflows/maven.yml)

[![UA](https://img.shields.io/badge/UA-yellow)](readme.ua.md)
[![RU](https://img.shields.io/badge/RU-black)](readme.ru.md)

<h1 align="center">biz.softfor.i18ngen</h1>

![Generate it!](biz.softfor.i18ngen/readme.png)

— is a utility for generating localization messages files. Using it, you:
1. Group all translations corresponding to one key in one place, thanks to which
2. You see missing translations and do not make duplicates.
3. You supply the translations you have made together with your code, which
makes it possible
4. Reuse and add support for new languages.
5. You receive an automatic report on duplicates and missing translations!

## Example of use

For each artifact with strings that need to be output in different languages,
we create a parallel artifact with a dependency:
```xml
<dependency>
  <groupId>biz.softfor</groupId>
  <artifactId>biz.softfor.i18ngen</artifactId>
  <version>${biz.softfor.i18ngen.version}</version>
</dependency>
```
and with class(es) of the following template:
```java
import biz.softfor.i18ngen.I18n;
import static biz.softfor.i18ngen.I18nGen.UKRAINIAN;
import static biz.softfor.i18ngen.I18nGen.add;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.Map.entry;
import static java.util.Map.of;
import static java.util.Map.ofEntries;
...
@I18n
public class SampleI18n {

  static {
    add(Sample.Language, of(
      ENGLISH, "Language"
    , GERMAN, "Sprache"
    , UKRAINIAN, "Мова"
    ));
    add(Sample.Use_this_syntax_if_you_need_support_for_more_than_10_languages, ofEntries(
      entry(ENGLISH, "Use this syntax if you need support for more than 10 languages")
    , entry(GERMAN, "Verwenden Sie diese Syntax, wenn Sie Unterstützung für mehr als 10 Sprachen benötigen")
    , entry(GERMAN, "Використовуйте цей синтаксис, якщо потрібна підтримка понад 10 мов")
    ));
  }

}
```
and annotate it with the @I18n annotation.

So, we have grouped our translations by keys, classes and artifacts. But how
to read them all and generate the files we need? **gmavenplus-plugin** will
help us with this! In the pom-file of your multilingual application in the
**<build><plugins>** section, add the following code:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.codehaus.gmavenplus</groupId>
      <artifactId>gmavenplus-plugin</artifactId>
      <dependencies>
        <!-- Here we list dependencies with our translations, for example: -->
        <dependency>
          <groupId>biz.softfor</groupId>
          <artifactId>biz.softfor.partner.i18n</artifactId>
          <version>0.0.0</version>
        </dependency>
        <dependency>
          <groupId>biz.softfor</groupId>
          <artifactId>biz.softfor.user.i18n</artifactId>
          <version>0.0.0</version>
        </dependency>
        <dependency>
          <groupId>biz.softfor</groupId>
          <artifactId>biz.softfor.util.i18n</artifactId>
          <version>0.0.0</version>
        </dependency>
      </dependencies>
      <executions>
        <execution>
          <id>generate-resources</id>
          <phase>generate-resources</phase>
          <goals>
            <goal>execute</goal>
          </goals>
          <configuration>
            <scripts>
              <script><![CDATA[
                String baseDir = project.properties.getProperty('project.basedir').replace('\\','/');
                biz.softfor.i18ngen.I18nGen.genMessages(
                  baseDir + "/target/classes"
                , baseDir + "/target/log"
                //We list the locales for which we will generate messages*.properties files. The first locale is the default locale.
                , "de,en,uk"
                //In this case, we could limit ourselves to "biz.softfor", but for demonstration purposes, we will list packages with @I18n annotated classes:
                , "biz.softfor.util"
                , "biz.softfor.user.i18n"
                , "biz.softfor.address.i18n"
                , "biz.softfor.partner.i18n"
                );
              ]]></script>
            </scripts>
          </configuration>
        </execution>
      </executions>
    </plugin>      
  </plugins>
</build>
```
As a result, in the project folder "target/classes" we will receive our
translation files, and if there are duplicates or missing values ​​in the folder
"target/log" a report with file names and line numbers.

## License

This project is licensed under the MIT License - see the [license.md](license.md) file for details.
