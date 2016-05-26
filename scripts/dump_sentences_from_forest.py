# -*- coding: UTF-8 -*-

import sys
import json

for line in sys.stdin:
    forest = json.loads(line)
    print forest['sentence'].encode("utf-8", "replace")
    sent_set = set()
    for sent in forest['forest']:
        sentence  = " ".join([word['word'] for word in sent['words']])
        if sentence not in sent_set:
            sent_set.add(sentence)
            print "\t" + sentence.encode("utf-8", "replace")
        
