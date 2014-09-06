package org.elasticsearch.index.analysis;

import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.ExtensibleStandardTokenizer;
import org.apache.lucene.analysis.standard.ExtensibleStandardTokenizerImpl;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

@AnalysisSettingsRequired
public class ExtensibleStandardTokenizerFactory extends AbstractTokenizerFactory {

  private final Map<Character,Character> characterMappings;

  @Inject
  public ExtensibleStandardTokenizerFactory(Index index,
                                            @IndexSettings Settings indexSettings,
                                            Environment env,
                                            @Assisted String name,
                                            @Assisted Settings settings) {
    super(index, indexSettings, name, settings);

    List<String> rules = Analysis.getWordList(env, settings, "mappings");
    if (rules == null) {
      throw new ElasticsearchIllegalArgumentException("mapping requires either `mappings` or `mappings_path` to be configured");
    }

    Map<Character, Character> map = new HashMap<>();
    parseRules(rules, map);
    characterMappings = Collections.unmodifiableMap(map);
  }

  @Override
  public Tokenizer create(Reader reader) {
    return new ExtensibleStandardTokenizer(version, reader, characterMappings);
  }

  // source => target
  private static Pattern rulePattern = Pattern.compile("(.*)\\s*=>\\s*(.*)\\s*$");

  /**
   * parses a list of MappingCharFilter style rules into a normalize char map
   */
  private void parseRules(List<String> rules, Map<Character, Character> map) {
    for (String rule : rules) {
      Matcher m = rulePattern.matcher(rule);
      if (!m.find())
        throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]");
      String lhs = parseString(m.group(1).trim());
      String rhs = parseString(m.group(2).trim());
      if (lhs == null || rhs == null || lhs.length() > 1)
        throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]. Illegal mapping.");

      try {
        map.put(lhs.charAt(0), translateWordBoundary(rhs));
      }
      catch (IllegalArgumentException iae) {
        throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]. Unrecognized WordBoundary property value");
      }
    }
  }

  public static enum WBProperty {
    SQ, DQ, EXNL, MNL, MN, ML
  }

  private Character translateWordBoundary(String mapping) throws IllegalArgumentException {
    WBProperty property = WBProperty.valueOf(mapping);
    switch (property) {
      case SQ:
        return ExtensibleStandardTokenizerImpl.WB_VALUE_SINGLE_QUOTE;
      case DQ:
        return ExtensibleStandardTokenizerImpl.WB_VALUE_DOUBLE_QUOTE;
      case EXNL:
        return ExtensibleStandardTokenizerImpl.WB_VALUE_EXTENDED_NUM_LETTER;
      case MNL:
        return ExtensibleStandardTokenizerImpl.WB_VALUE_MID_NUMBER_LETTER;
      case MN:
        return ExtensibleStandardTokenizerImpl.WB_VALUE_MID_NUMBER;
      case ML:
        return ExtensibleStandardTokenizerImpl.WB_VALUE_MID_LETTER;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Copied from {@link org.elasticsearch.index.analysis.MappingCharFilterFactory}
   */
  char[] out = new char[256];
  private String parseString(String s) {
    int readPos = 0;
    int len = s.length();
    int writePos = 0;
    while (readPos < len) {
      char c = s.charAt(readPos++);
      if (c == '\\') {
        if (readPos >= len)
          throw new RuntimeException("Invalid escaped char in [" + s + "]");
        c = s.charAt(readPos++);
        switch (c) {
          case '\\':
            c = '\\';
            break;
          case 'n':
            c = '\n';
            break;
          case 't':
            c = '\t';
            break;
          case 'r':
            c = '\r';
            break;
          case 'b':
            c = '\b';
            break;
          case 'f':
            c = '\f';
            break;
          case 'u':
            if (readPos + 3 >= len)
              throw new RuntimeException("Invalid escaped char in [" + s + "]");
            c = (char) Integer.parseInt(s.substring(readPos, readPos + 4), 16);
            readPos += 4;
            break;
        }
      }
      out[writePos++] = c;
    }
    return new String(out, 0, writePos);
  }
}
