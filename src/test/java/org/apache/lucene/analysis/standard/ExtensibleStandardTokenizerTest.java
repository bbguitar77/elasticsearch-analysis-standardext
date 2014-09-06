package org.apache.lucene.analysis.standard;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class ExtensibleStandardTokenizerTest {

  @Test
  public void customWordBoundaries_extendedNumLetter() throws IOException {
    String f_text = "%sfoo%sbar%s";
    char[] charz = new char[] { '#', '@', '+', '-', '_' };
    final Map<Character, Character> characterMappings = new HashMap<>();
    for (char chr : charz)
      characterMappings.put(chr, ExtensibleStandardTokenizerImpl.WB_VALUE_EXTENDED_NUM_LETTER);

    for (char chr : charz) {
      final String input = String.format(f_text, args(3, chr));
      final String[] expected = { input };
      TestHelper.assertTokens(createExtensibleStandardTokenizer(input, characterMappings), expected);
    }
  }

  private static Tokenizer createExtensibleStandardTokenizer(final String input,
                                                             final Map<Character, Character> characterMappings) {
    return new ExtensibleStandardTokenizer(Version.LUCENE_4_9, new StringReader(input), characterMappings);
  }

  private static Object[] args(int length, char chr) {
    Object[] args = new Object[length];
    Arrays.fill(args, chr);
    return args;
  }
}
