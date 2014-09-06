ElasticSearch Analysis Extensible Standard Tokenizer
=====================================================

Introduction
------------

Currently, when using Lucene's StandardTokenizer, there is no convenient way to customize the word boundary property values for various Unicode characters (e.g. '#', '@', '+', etc.). As a result, most of these characters are treated as word boundary breaks, which can prove problematic for surfacing exact matches.

For instance, using the default StandardTokenizer, the following tweet - "@ericschmidt google+ rocks #social" - produces the following tokens - {"ericschmidt", "google", "rocks", "social"}. So, a user who wants to see only content with "#social" will be unable to do so - they will match on all content containing the term 'social' (since '#social' and 'social' produce the same search token).

Currently, the only partial solution is to leverage ElasticSearch's mapping char filter (which is run before tokenization), where you could map characters like '@', '#', etc. to other characters that aren't treated as word boundary breaks (e.g. '_'). But, it's a hacky solution at best ... you can't map all the characters to the same destination character as then you wouldn't be able to distinguish between them at query time ('@user' -> '_user', '#user' -> '_user'). In addition, this would alter the source of the stored document field and prevent you from showing accurate highlighted text snippets.

So, to solve this problem, this plugin provides an extension to the StandardTokenizer and allows the user to customize the word boundary rules for any Unicode character.

Requirements
------------

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

 * SQ   -> Single-quote
 * DQ   -> Double-quote
 * EXNL -> Extended Number Letter (preserved at start, middle, end of alpha-numeric characters - e.g. '_')
 * MNL  -> Mid Number Letter (preserved between alpha-numeric characters - e.g. '.')
 * MN   -> Mid Number (preserved between numerics - e.g. ',')
 * ML   -> Mid Letter (preserved between letters - e.g. ':')

Author Information
==================
Bryan Warner
bwarner [at] traackr.com

License
=======

Elasticsearch Extensible Standard Tokenizer Plugin

Copyright 2014 Bryan Warner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.