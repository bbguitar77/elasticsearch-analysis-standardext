package org.elasticsearch.index.analysis;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.TestHelper;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.plugin.analysis.ExtensibleStandardTokenizerPlugin;
import org.junit.Test;

public class ExtensibleStandardAnalysisTest {

  @Test
  public void standardAnalysis() throws IOException {
    String source = "@foobar Google+ is leading the way in user-generated content #social";
    String[] expected = { "foobar", "google", "leading", "way", "user", "generated", "content", "social" };
    Analyzer analyzer = initAnalysisService().analyzer("test_standard").analyzer();
    TestHelper.assertTokens(analyzer.tokenStream("_tmp", new StringReader(source)), expected);
  }

  @Test
  public void extStandardAnalysis() throws IOException {
    String source = "@foobar Google+ is leading the way in user-generated content #social";
    String[] expected = { "@foobar", "google+", "leading", "way", "user-generated", "content", "#social" };
    Analyzer analyzer = initAnalysisService().analyzer("test_standard_ext").analyzer();
    TestHelper.assertTokens(analyzer.tokenStream("_tmp", new StringReader(source)), expected);
  }

  public AnalysisService initAnalysisService() {
    Settings settings = ImmutableSettings.settingsBuilder()
                                         .loadFromClasspath("org/elasticsearch/index/analysis/standard_ext_analysis.json")
                                         .build();

    Index index = new Index("test_index");
    Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                                                       new EnvironmentModule(new Environment(settings)),
                                                       new IndicesAnalysisModule()).createInjector();

    AnalysisModule analysisModule = new AnalysisModule(settings,
                                                       parentInjector.getInstance(IndicesAnalysisService.class));
    new ExtensibleStandardTokenizerPlugin().onModule(analysisModule);

    Injector injector = new ModulesBuilder().add(new IndexSettingsModule(index, settings),
                                                 new IndexNameModule(index),
                                                 analysisModule).createChildInjector(parentInjector);

    return injector.getInstance(AnalysisService.class);
  }
}
