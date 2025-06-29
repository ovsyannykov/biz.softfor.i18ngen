package biz.softfor.i18ngen;

import static biz.softfor.i18ngen.I18nGen.UKRAINIAN;
import static biz.softfor.i18ngen.I18nGen.add;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.Map.entry;
import static java.util.Map.of;
import static java.util.Map.ofEntries;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

@I18n
public class I18nGenTest {

  static {
    //Example of correct addition of translations
    add("language", of(
      ENGLISH, "Language"
    , GERMAN, "Sprache"
    , UKRAINIAN, "Мова"
    ));
    //Duplicate of an existing translation
    add(ENGLISH.getLanguage(), of(
      ENGLISH, ENGLISH.getDisplayLanguage(ENGLISH)
    ));
    //Missing translation for Ukrainian language
    add("Use_this_syntax_if_you_need_support_for_more_than_10_languages", ofEntries(
      entry(ENGLISH, "Use this syntax if you need support for more than 10 languages")
    , entry(GERMAN, "Verwenden Sie diese Syntax, wenn Sie Unterstützung für mehr als 10 Sprachen benötigen.")
    ));
  }

  @Test
  public void test(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path dir)
  throws Exception {
    String[] langs = { "de", "en", "uk" };
    String defaultLang = langs[0];
    String outDir = dir.toString();
    I18nGen.genMessages
    (outDir, outDir, String.join(",", langs), getClass().getPackageName());
    String etalonsDir = new File(URLDecoder.decode(
      getClass().getClassLoader().getResource(
        getClass().getPackageName().replace('.', File.separatorChar)
        + File.separator + "assets"
      ).getPath()
    , StandardCharsets.UTF_8
    )).getAbsolutePath();
    int resultsSize = langs.length + 1;
    String[] results = new String[resultsSize];
    String[] etalons = new String[resultsSize];
    for(int i = 0; i < langs.length; ++i) {
      results[i] = I18nGen.messagesName(outDir, defaultLang, langs[i]);
      etalons[i] = I18nGen.messagesName(etalonsDir, defaultLang, langs[i]);
    }
    results[langs.length] = I18nGen.logName(outDir);
    etalons[langs.length] = I18nGen.logName(etalonsDir);
    String errors = "";
    for(int i = 0; i < resultsSize; ++i) {
      try(
        BufferedReader actualReader
        = new BufferedReader(new FileReader(results[i]));
        BufferedReader expectedReader
        = new BufferedReader(new FileReader(etalons[i]));
      ) {
        String actual, expected;
        int l = 1;
        while(true) {
          actual = actualReader.readLine();
          expected = expectedReader.readLine();
          if(actual == null || expected == null) {
            break;
          }
          if(!actual.equals(expected)) {
            errors +=
            "\n" + results[i] + ":" + l
            + "\nactual: " + actual
            + "\nexpected: " + expected
            ;
          }
          ++l;
        }
        if(actual != null) {
          errors += "\n" + results[i] + " is longer than expected.";
        }
        if(expected != null) {
          errors += "\n" + results[i] + " is shorter than expected.";
        }
      }
    }
    if(!errors.isEmpty()) {
      fail("Differences from reference files found (see " + outDir + "):"
      + errors);
    }
  }

}
