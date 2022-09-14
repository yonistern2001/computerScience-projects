import sys

def evaluate_Formula(envirements, formulaLeft, formulaRight):
    left= True
    right= True
    for envirement in envirements:
        envirement.update({"T": True})
        envirement.update({"F": False})        
        bool1= eval(formulaLeft, envirement)
        bool2= eval(formulaRight, envirement)
        if bool1:
            if not bool2:
                left= False
        if bool2:
            if not bool1:
                right= False
    if left and right:
        print("EQUIVALENT")
    elif left:
        print("LEFT implies RIGHT")
    elif right:
        print("Right implies LEFT")
    else:
        print("NO IMPLICATION")

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
            if char!="T" and char!="F":
                variables.add(char)
    return variables

#file= open(sys.argv[1], "r")
file= open("Formula.txt", "r")
formulaLeft= file.readline()
formulaRight= file.readline()
set1= getVariables(formulaLeft)
set2= getVariables(formulaRight)
set= set1.union(set2)
envirements= enumirateVariables(list(set))
evaluate_Formula(envirements, formulaLeft, formulaRight)
file.close()