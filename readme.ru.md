[![GitHub License](https://img.shields.io/github/license/ovsyannykov/biz.softfor.i18ngen)](license.md)
[![Maven Central Version](https://img.shields.io/maven-central/v/biz.softfor/biz.softfor.spring.i18ngen)](https://mvnrepository.com/artifact/biz.softfor/biz.softfor.spring.i18ngen)
[![Java CI with Maven](https://github.com/ovsyannykov/biz.softfor.i18ngen/actions/workflows/maven.yml/badge.svg)](https://github.com/ovsyannykov/biz.softfor.i18ngen/actions/workflows/maven.yml)

[![UA](https://img.shields.io/badge/UA-yellow)](readme.ua.md)
[![EN](https://img.shields.io/badge/EN-blue)](readme.md)

<h1 align="center">biz.softfor.i18ngen</h1>

![Сгенерируй это!](biz.softfor.i18ngen/readme.png)

— это утилита для генерации messages-файлов локализации. Используя её, Вы:
1. Группируете все соответствующие одному ключу переводы в одном месте,
благодаря чему
2. Вы видите недостающие переводы и не делаете дубликатов.
3. Вы поставляете сделанные переводы вместе со своим кодом, что даёт возможность
4. Переиспользовать и добавлять поддержку новых языков.
5. Вы получаете автоматический отчёт о дубликатах и отсутствующих переводах!

## Пример использования

Для каждого артефакта со строками, которые требуется выводить на разных языках,
создаём параллельный артефакт с зависимостью:
```xml
<dependency>
  <groupId>biz.softfor</groupId>
  <artifactId>biz.softfor.i18ngen</artifactId>
  <version>${biz.softfor.i18ngen.version}</version>
</dependency>
```
и с классом(ами) следующего вида:
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
и аннотируем его аннотацией @I18n.

Итак, мы сгруппировали наши переводы по ключам, классам и артефактам. Но как
теперь их все прочитать и сгенерировать нужные нам файлы? В этом нам поможет
**gmavenplus-plugin**! В pom-файл уже Вашего многоязычного приложения
в секцию **<build><plugins>** добавляем такой код:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.codehaus.gmavenplus</groupId>
      <artifactId>gmavenplus-plugin</artifactId>
      <dependencies>
        <!-- Здесь перечисляем зависимости с нашими переводами, например: -->
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
                //Перечисляем локали, для которых будем генерировать messages*.properties файлы. Первая локаль - локаль по умолчанию.
                , "de,en,uk"
                //В данном случае можно было бы ограничиться "biz.softfor", но в целях демонстрации перечислим пакеты с аннотированными @I18n классами:
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
В результате в папке проекта "target/classes" получим наши файлы переводов, и
при наличии дубликатов или отсутствующих значений в папке "target/log" отчёт
с именами файлов и номерами строк.

## Лицензия

Этот проект имеет лицензию MIT - подробности смотрите в файле [license.md](license.md).
