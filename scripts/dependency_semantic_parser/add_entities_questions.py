'''
Created on Nov 25, 2014

@author: sivareddy
'''

import sys
import simplejson

entity_lexicon = sys.argv[1]
lexicon = {}
for line in open(entity_lexicon):
  (word, mid) = line.strip().split('\t')
  lexicon[word] = mid

for line in sys.stdin:
  sent = simplejson.loads(line)
  entities = []
  for i, word in enumerate(sent['tok']):
    if word['word'] in lexicon:
      entity = {}
      entity['entity'] = lexicon[word['word']]
      entity['index'] = i
      entities.append(entity)
  sent['entities'] = entities
  print simplejson.dumps(sent)
