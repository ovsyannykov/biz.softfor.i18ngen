package biz.softfor.i18ngen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static java.util.Locale.GERMAN;
import static java.util.Locale.ITALIAN;
import java.util.Map;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

@I18n
public class I18nGen {

  public final static Locale FINNISH = Locale.of("fi", "FI");
  public final static Locale RUSSIAN = Locale.of("ru", "RU");
  public final static Locale SPANISH = Locale.of("es", "ES");
  public final static Locale UKRAINIAN = Locale.of("uk", "UA");

  private record Error(String key, String lang, String message, String from) {}
  private record Message(String message, String from) {}

  private final static String ERRORS = "errors";
  private final static String MESSAGES = "messages";
  private final static List<Error> errors = new ArrayList<>();
  private final static Map<String, Map<String, Message>> messages = new HashMap<>();

  static {
    Locale[] locales = {
      ENGLISH
    , FINNISH
    , FRENCH
    , GERMAN
    , ITALIAN
    , RUSSIAN
    , SPANISH
    , UKRAINIAN
    };
    for(Locale locale : locales) {
      Map<Locale, String> m = new HashMap<>(locales.length);
      for(Locale l : locales) {
        m.put(l, locale.getDisplayLanguage(l));
      }
      add(locale.getLanguage(), m);
    }
  }

  public static void add(String key, Map<Locale, String> localisedMessages) {
    Map<String, Message> translates = messages.get(key);
    if(translates == null) {
      translates = new HashMap<>(localisedMessages.size());
      messages.put(key, translates);
    }
    for(Map.Entry<Locale, String> a : localisedMessages.entrySet()) {
      String lang = a.getKey().getLanguage();
      Message m = translates.get(lang);
      if(m == null) {
        translates.put(lang, new Message(a.getValue(), trace()));
      } else {
        errors.add(new Error(key, lang, m.message, m.from));
        errors.add(new Error(key, lang, a.getValue(), trace()));
      }
    }
  }

  /**
   * Creates files with translations (messages*.properties)
   * @param resourcesDir The directory where translation files are created
   * @param logDir The directory where the error log is created
   * @param locales Comma-separated list of locales for which translations need to be generated (for example, "de,en,fi,uk"). The first locale is the default locale.
   * @param packages Packages with classes marked with {@code @I18n} annotation
   * @throws Exception In case of absence of default constructor for {@code @I18n} annotated classes or errors in writing to file
   */
  public static void genMessages
  (String resourcesDir, String logDir, String locales, String... packages)
  throws Exception {
    Reflections reflections = new Reflections(new ConfigurationBuilder()
    .forPackages(packages).setScanners(Scanners.TypesAnnotated));
    for(Class c : reflections.getTypesAnnotatedWith(I18n.class)) {
      c.getConstructor().newInstance();
    }
    List<String> keys = messages.keySet().stream().sorted().collect(Collectors.toList());
    File resourcesPath = new File(resourcesDir);
    if(!resourcesPath.exists()) {
      resourcesPath.mkdir();
    }
    String langs[] = locales.split(",");
    for(String lang : langs) {
      String fileName = messagesName(resourcesDir, langs[0], lang);
      try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
        for(String key : keys) {
          Message message = messages.get(key).get(lang);
          if(message == null) {
            errors.add(new Error(key, lang, null, null));
          } else {
            writer.write(key + "=" + message.message + "\n");
          }
        }
      }
    }
    File logFile = new File(logDir);
    if(!logFile.exists()) {
      logFile.mkdir();
    }
    String logName = logName(logDir);
    logFile = new File(logName);
    if(logFile.exists()) {
      logFile.delete();
    }
    if(!errors.isEmpty()) {
      try(BufferedWriter writer = new BufferedWriter(new FileWriter(logName))) {
        for(int i = 0; i < errors.size(); ++i) {
          Error e = errors.get(i);
          if(i > 0) {
            writer.write("\n\n");
          }
          if(e.message == null) {
            String out = "MISSING " + e.lang + " [" + e.key + "]\n";
            Map<String, Message> translates = messages.get(e.key);
            if(!translates.isEmpty()) {
              out += translates.entrySet().stream().findAny().get().getValue().from;
            }
            writer.write(out);
          } else {
            Error e2 = errors.get(++i);
            writer.write("DUPLICATE " + e.lang + " [" + e.key + "]\n"
            + e.from + ": " + e.message + "\n" + e2.from + ": " + e2.message);
          }
        }
      }
    }
  }

  public static String logName(String dir) {
    return dir + File.separator + MESSAGES + "." + ERRORS;
  }

  public static String messagesName(String dir, String defaultLang, String lang) {
    String suffix = lang.equals(defaultLang) ? "" : ("_" + lang);
    return dir + File.separator + MESSAGES + suffix + ".properties";
  }

  private static String trace() {
    StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
    return ste.getClassName() + "." + ste.getMethodName()
    + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
  }

}
