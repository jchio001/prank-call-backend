 #!/usr/bin/env
import math 

tmp = 26
tmp -= 1

digits = 1
counter = 1

while (counter <= tmp):
	counter *= 26;
	++digits

ChrList = []
for i in range(digits + 1, 0, -1):
	#print counter
	#print counter / pow(26, i)
	ChrList.append(counter / pow(26, i))
	counter = (counter % pow(26, i))	

for i in range(len(ChrList) - 1, -1, -1):
	print str(ChrList[i])