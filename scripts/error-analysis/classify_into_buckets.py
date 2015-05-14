'''
Created on 13 May 2015

@author: siva
'''

import json
import sys
from sempre_evaluation_lib import computeF1, getResults

one_best_file = sys.argv[1]
nbest_file = sys.argv[2]

fully_correct = set()
partially_correct = set()
correct_in_beam = set()
out_of_beam = set()
incorrect_in_beam = set()
no_predictions = set()

for line in open(one_best_file):
    line = line.strip()
    sentence, gold, predicted = line.split("\t")
    gold = set(json.loads(gold))
    predicted = set(json.loads(predicted))
    if gold == set():
        sys.stderr.write("Gold answers cannot be empty: " + line)
        exit()
    r, p, f = computeF1(gold, predicted)

    if f > 0.99:
        fully_correct.add(sentence)
    elif f > 0.8:
        partially_correct.add(sentence)

for line in open(nbest_file):
    line = line.strip()
    sentence, gold, predicted = line.split("\t")
    gold = set(json.loads(gold))
    predicted = set(json.loads(predicted))
    if gold == set():
        sys.stderr.write("Gold answers cannot be empty: " + line)
        exit()
    r, p, f = computeF1(gold, predicted)

    if f > 0.99:
        correct_in_beam.add(sentence)
    elif len(predicted) == 0:
        no_predictions.add(sentence)
    else:
        out_of_beam.add(sentence)


correct_only_in_beam = correct_in_beam - fully_correct - partially_correct
out_of_beam = out_of_beam - partially_correct

total = 0.0 + (len(fully_correct) + len(partially_correct) +
               len(correct_only_in_beam) + len(out_of_beam) + len(no_predictions))

print "Results from %s" % (one_best_file.split("/")[-1])
print "========================="
print getResults(one_best_file)
print

print "Results from %s" % (nbest_file.split("/")[-1])
print "========================="
print getResults(nbest_file)
print

print "Fully Correct Answers: %d (%.2f%%)" % (len(fully_correct), len(fully_correct) / total)
print "========================="
for sentence in fully_correct:
    print sentence
print

print "Partially Correct Answers: %d (%.2f%%)" % (len(partially_correct), len(partially_correct) / total)
print "========================="
for sentence in partially_correct:
    print sentence
print

print "Correct Only in Beam: %d (%.2f%%)" % (len(correct_only_in_beam), len(correct_only_in_beam) / total)
print "========================="
for sentence in correct_only_in_beam:
    print sentence
print

print "Out of Beam: %d (%.2f%%)" % (len(out_of_beam), len(out_of_beam) / total)
print "========================="
for sentence in out_of_beam:
    print sentence
print

print "No Predictions: %d (%.2f%%)" % (len(no_predictions), len(no_predictions) / total)
print "========================="
for sentence in no_predictions:
    print sentence
print

print "Total: %d" % (total)
