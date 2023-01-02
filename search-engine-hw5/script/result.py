import sys
num = (sys.argv[1]).strip().split(",")
col_array = "runid,topic,ERR-IA@5,ERR-IA@10,ERR-IA@20,nERR-IA@5,nERR-IA@10,nERR-IA@20,alpha-DCG@5,alpha-DCG@10,alpha-DCG@20,alpha-nDCG@5,alpha-nDCG@10,alpha-nDCG@20,NRBP,nNRBP,MAP-IA,P-IA@5,P-IA@10,P-IA@20,strec@5,strec@10,strec@20".split(",")
assert len(col_array) == len(num)
res = {}
for i in range(len(num)):
    res[col_array[i]] = num[i]

print("P-IA@10: ", res["P-IA@10"])
print("P-IA@20: ", res["P-IA@20"])
print("alpha-nDCG@20: ", res["alpha-nDCG@20"])