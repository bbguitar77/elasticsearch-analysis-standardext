ElasticSearch Analysis Extensible Standard Tokenizer
=====================================================

Introduction
------------

This plugin features an extension of Lucene's StandardTokenizer that allows the user to customize the word boundary property values for any Unicode character. 

Currently, Lucene's StandardTokenizer implements the word break rules from the [http://www.unicode.org/reports/tr29/#Word_Boundaries](Unicode Text Segmentation) algorithm. It provides no mechanism to override the word break rules for various Unicode characters (e.g. '#', '@', '+', etc.). This is problematic because most punctuation characters are treated as hard word boundary breaks, which can lead to inaccurate results at querying time.

A simple example can prove this point. For instance, consider the following tweet:

    @ericschmidt google+ rocks #social

Using the default StandardTokenizer, this tweet will produce the following tokens:

    "ericschmidt", "google", "rocks", "social"

That might look fine, but essentially we have lost the ability to do exact matching on tweets containing "#social" (i.e. hashtag social). Since "#social" is tokenized to "social", when a user runs an exact search on "#social", they will get matches on all tweets that have the term social (with or without the hashtag). Obviously, this is not what the user will be expecting.

Currently, the only _partial_ solution is to leverage ElasticSearch's [http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/analysis-mapping-charfilter.html](mapping char filter). This filter gets run before tokenization and allows the user to replace any arbitrary sequence of characters with the given mapping. So, in our previous example, we could map characters like '@', '+', and '#' to a destination character that is not treated as a word boundary break (e.g. '_'). But, it's a bit of a hack and in our example you wouldn't want to map '@' and '#' to the same character (or else, we wouldn't be able to distinguish between mentions and hashtag terms at query time). Also, using the mapping char filter has other possible side-effets too (e.g. highlighting accuracy)

What we want is to be able to leverage all the goodness of Lucene's StandardTokenizer (which does a TON for us), but just be able to override the default word boundary rules for any paricular Unicode character (most likely - special punctation symbols that are becoming more meaningful in the Web). This is what this plugin strives to do...

Compatibility
------------

|_. Extensible StandardTokenizer Plugin   |_.  Elasticsearch    |
| 0.1         |  1.1 -> master   |

 * Lucene 4.7+
 * ElasticSearch 1.1+

Usage
------------

	{
    	"index":{
        	"analysis":{
           		"analyzer" : {
             		"my_analyzer" : {
               			"type":"custom",
               			"char_filter":[],
               			"tokenizer": "my_standard_ext",
               			"filter":["lowercase", "stop"]
             		}            
           		},
           		"tokenizer": {
             		"my_standard_ext": {
        	   			"type": "standard_ext",
        	     		"mappings": [ 
              	   			"@=>EXNL",
              	   			"#=>EXNL",
              	   			"+=>EXNL",
              	   			"-=>EXNL"
            	 		]
        	   		}
        	 	}
           	}
        }
    }

The extensible StandardTokenizer is registered unded the tokenizer name "standard_ext" and requires a mapping property. The LHS (left-hand side) of each mapping entry is a single character value. The RHS of each mapping entry is the word-boundary property type you wish to assign to the character on the left.

The supported word-boundary property types are:
 * L    -> A Letter
 * N    -> Numeric
 * EXNL -> Extended Number Letter (preserved at start, middle, end of alpha-numeric characters - e.g. '_')
 * MNL  -> Mid Number Letter (preserved between alpha-numeric characters - e.g. '.')
 * MN   -> Mid Number (preserved between numerics - e.g. ',')
 * ML   -> Mid Letter (preserved between letters - e.g. ':')
 * SQ   -> Single-quote
 * DQ   -> Double-quote

Author Information
==================
Bryan Warner
bwarner [at] traackr.com

License
=======

Elasticsearch Extensible Standard Tokenizer Plugin

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.