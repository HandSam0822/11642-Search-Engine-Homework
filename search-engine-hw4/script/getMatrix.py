#!/usr/bin/python

#
#  This python script illustrates fetching information from a CGI program
#  that typically gets its data via an HTML form using a POST method.
#
#  Copyright (c) 2018, Carnegie Mellon University.  All Rights Reserved.
#

import requests
import sys

#  ===> FILL IN YOUR PARAMETERS <===

userId = 'shousanl@andrew.cmu.edu'
password = 'KHVVoj0N'
fileIn1 = (sys.argv[1]).strip()  # origin
fileIn2 = (sys.argv[2]).strip()  # expanded
# outputFile = (sys.argv[2]).strip()

hwId = 'HW3'
qrels = 'cw09a.adhoc.1-200.qrel.indexed'

#  Form parameters - these must match form parameters in the web page

url = 'https://boston.lti.cs.cmu.edu/classes/11-642/HW/HTS/tes.cgi'
values = {'hwid': hwId,				# cgi parameter
          'qrel': qrels,				# cgi parameter
          'logtype': 'Detailed',			# cgi parameter
          'leaderboard': 'No'				# cgi parameter
          }

#  Make the request

files = {'infile': (fileIn1, open(fileIn1, 'rb'))}  # cgi parameter
result1 = requests.post(url, data=values, files=files, auth=(userId, password))

files = {'infile': (fileIn2, open(fileIn2, 'rb'))}  # cgi parameter
result2 = requests.post(url, data=values, files=files, auth=(userId, password))

#  Replace the <br /> with \n for clarity
result1 = result1.text.replace('<br />', '\n')
chunks1 = result1.split('\n')
result2 = result2.text.replace('<br />', '\n')
chunks2 = result2.split('\n')

# print matrix result
print("matrix for file1")
for line in chunks1:
    if ("P_10" in line or "P_20" in line or "P_30" in line or "map" in line):
        print(line)

print()

# print matrix result
print("matrix for file2")
for line in chunks2:
    if ("P_10" in line or "P_20" in line or "P_30" in line or "map" in line):
        print(line)

print()

# calculate MAP diff of the two files
countWin = 0
countLoss = 0
countTie = 0

for i in range(0, len(chunks1)):
    line1 = chunks1[i]
    line2 = chunks2[i]
    if ("map" in line1):
        print(line1)
        print(line2)
        qid = line1.split()[1]
        if qid != "all":
            map1 = float(line1.split()[2])
            map2 = float(line2.split()[2])
            mapDiff = (map2 - map1) / map1
            print(mapDiff)
            if (mapDiff >= 0.02):
                countWin += 1
            elif (mapDiff <= -0.02):
                countLoss += 1
            else:
                countTie += 1

# print result
print("win:tie:loss {}:{}:{}".format(countWin, countTie, countLoss))
