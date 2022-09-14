import sys

def evaluate_Formula(envirements, formula):
    satisfied= 0
    unsatisfied=0
    for envirement in envirements:
        #print(envirement)
        isSatisfied= eval(formula, envirement)
        if isSatisfied:
            satisfied+=1
        else:
            unsatisfied+=1
    print("Satisfied: ",satisfied,"; Not Satisfied: ",unsatisfied, sep="")

def enumirateVariables(variables):
    envirement= [False]*len(variables)
    envirements= []
    for value in range(2**len(variables)):
        dictonary= dict()
        doAdd=True
        for index, value in enumerate(variables):
            if doAdd:
                if envirement[index]:
                    envirement[index]= False
                    doAdd=True
                else:
                    envirement[index]= True
                    doAdd=False
            dictonary.update({value : envirement[index]})
        envirements.append(dictonary)
    return envirements

def getVariables(formula):
    variables= set()
    for char in formula:
        if char.isalpha() and char.isupper():
            variables.add(char)
    return variables

file= open(sys.argv[1], "r")
formula= file.read()
#print(formula)
set= getVariables(formula)
envirements= enumirateVariables(list(set))
evaluate_Formula(envirements, formula)
file.close()