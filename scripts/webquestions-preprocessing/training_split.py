'''
Created on 26 May 3014

@author: siva
'''

import sys
import json
import random


training_file = open(sys.argv[1] + ".70", "w")
dev_file = open(sys.argv[1] + ".30", "w")

sys.stderr.write("Creating training and dev splits\n");

random.seed(1)

data = open(sys.argv[1]).readlines()
random.shuffle(data)
random.shuffle(data)
random.shuffle(data)
random.shuffle(data)
random.shuffle(data)
random.shuffle(data)
random.shuffle(data)

training_data_size = 70 * len(data) / 100

for line in data[:training_data_size]:
    training_file.write(line)
training_file.close()

for line in data[training_data_size:]:
    dev_file.write(line)
dev_file.close()
