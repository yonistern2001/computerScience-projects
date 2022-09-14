from sieve import sieve

def prime_fac(number):
    factors= list()
    for prime_number in sieve(number):
        temp=number
        count=0
        while temp%prime_number == 0:
            count+=1
            temp/=prime_number
        if not count == 0:
            factor= (prime_number, count)
            factors.append(factor)
    return factors

def phi(primeFactorization):
    count= 0
    number= 1
    for i in primeFactorization:
        number*= i[0]**i[1]
    def canDivide(number):
        nonlocal primeFactorization
        for x in primeFactorization:
            if number%x[0] == 0:
                return True
        return False

    for num in range(0, number):
        if not canDivide(num):
            count+=1
    return count

factors= prime_fac(2)
print(factors)
print(phi(factors))