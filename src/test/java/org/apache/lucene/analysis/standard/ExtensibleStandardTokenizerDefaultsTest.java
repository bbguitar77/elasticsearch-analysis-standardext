package org.apache.lucene.analysis.standard;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class ExtensibleStandardTokenizerDefaultsTest {

  @Test
  public void defaultWordBoundaries_break() throws IOException {
    String f_text = "%sfoo%sbar%s";
    char[] charz = new char[] { '!', '#', '$', '%', '&', '(', ')', '*', '+', '-', '/', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '`', '{', '|', '}', '~' };
    for (char chr : charz) {
      final String input = String.format(f_text, args(3, chr));
      final String[] expected = { "foo", "bar" };
      TestHelper.assertTokens(createExtensibleStandardTokenizer(input), expected);
    }
  }

  @Test
  public void defaultWordBoundaries_singleQuote() throws IOException {
    String f_text = "%sfoo%sbar%s";
    char[] charz = new char[] { '\'' };
    for (char chr : charz) {
      final String input = String.format(f_text, chr, chr, chr);
      final String[] expected = { "foo'bar" };
      TestHelper.assertTokens(createExtensibleStandardTokenizer(input), expected);
    }
  }

  @Test
  public void defaultWordBoundaries_doubleQuotes() throws IOException {
    String f_text = "%sfoo%sbar%s";
    char[] charz = new char[] { '"' };
    for (char chr : charz) {
      final String input = String.format(f_text, chr, chr, chr);
      final String[] expected = { "foo", "bar" };
      TestHelper.assertTokens(createExtensibleStandardTokenizer(input), expected);
    }
  }

  @Test
  public void defaultWordBoundaries_midLetter() throws IOException {
    String f_text = "%sfoo%sbar%s %s123%s456%s";
    char[] charz = new char[] { ':' };
    // preserved between letters
    for (char chr : charz) {
      final String input = String.format(f_text, args(6, chr));
      final String[] expected = { String.format("foo%sbar", chr), "123", "456" };
      TestHelper.assertTokens(createExtensibleStandardTokenizer(input), expected);
    }
  }

  @Test
  public void defaultWordBoundaries_midNumber() throws IOException {
    String f_text = "%sfoo%sbar%s %s123%s456%s";
    char[] charz = new char[] { ',', ';' };
    // preserved between numbers
    for (char chr : charz) {
      final String input = String.format(f_text, args(6, chr));
      final String[] expected = { "foo", "bar", String.format("123%s456", chr) };
      TestHelper.assertTokens(createExtensibleStandardTokenizer(input), expected);
    }
  }

  @Test
  public void defaultWordBoundaries_midNumLetter() throws IOException {
    String f_text = "%sfoo%sbar%s %s123%s456%s";
    char[] charz = new char[] { '.' };
    // preserved between numbers & letters
    for (char chr : charz) {
      final String input = String.format(f_text, args(6, chr));
      final String[] expected = { String.format("foo%sbar", chr), String.format("123%s456", chr) };
      TestHelper.assertTokens(createExtensibleStandardTokenizer(input), expected);
    }
  }

  @Test
  public void defaultWordBoundaries_extendedNumLetter() throws IOException {
    String f_text = "%sfoo%sbar%s";
    char[] charz = new char[] { '_' };
    // preserved at the beginning, middle, and end of alpha-numeric characters
    for (char chr : charz) {
      final String input = String.format(f_text, chr, chr, chr);
      final String[] expected = { input };
      TestHelper.assertTokens(createExtensibleStandardTokenizer(input), expected);
    }
  }

  /**
   * Create an {@link ExtensibleStandardTokenizer} with no character mapping
   * overrides
   */
  private static Tokenizer createExtensibleStandardTokenizer(final String input) {
    return new ExtensibleStandardTokenizer(Version.LUCENE_4_9,
                                           new StringReader(input),
                                           Collections.<Character, Character> emptyMap());
  }

  private static Object[] args(int length, char chr) {
    Object[] args = new Object[length];
    Arrays.fill(args, chr);
    return args;
  }
}
