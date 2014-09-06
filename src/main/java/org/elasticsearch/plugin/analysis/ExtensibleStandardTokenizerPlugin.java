package org.elasticsearch.plugin.analysis;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.ExtensibleStandardTokenizerFactory;
import org.elasticsearch.plugins.AbstractPlugin;

public class ExtensibleStandardTokenizerPlugin extends AbstractPlugin {

  @Override
  public String name() {
    return "analysis-standard-ext";
  }

  @Override
  public String description() {
    return "An extensible standard tokenizer that supports custom character mappings to override word boundary property values";
  }

  public void onModule(AnalysisModule module) {
    module.addTokenizer("standard_ext", ExtensibleStandardTokenizerFactory.class);
  }
}
