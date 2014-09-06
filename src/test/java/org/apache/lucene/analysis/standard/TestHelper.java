package org.apache.lucene.analysis.standard;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Assert;

public class TestHelper {

  public static void assertTokens(TokenStream stream, String[] expected) throws IOException {
    stream.reset();
    CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
    Assert.assertNotNull(termAttr);
    int i = 0;
    while (stream.incrementToken()) {
      Assert.assertTrue(i < expected.length);
      Assert.assertEquals("expected different term at index " + i, expected[i++], termAttr.toString());
    }
    Assert.assertEquals("not all tokens produced", Integer.valueOf(i), Integer.valueOf(expected.length));
  }
}
