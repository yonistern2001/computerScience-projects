USER_INPUT=int(input())
x=USER_INPUT
timesRun=0
while x != 1:
    if x % 2 == 1:
        x= 3*x+1
    else:
        x=x/2
    timesRun+=1
print(timesRun)