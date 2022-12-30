#include <stdio.h>
#include <stdlib.h>

double power(int base, int pow);
long doubleTo6_58(double decimal);

int main(int argc, char *argv[])
{
	extern int MBPixelCalc(long, long);
	
	double num1= atof(argv[1]);
	double num2= atof(argv[2]);	
	
	printf("MBPixelCalc() returned %d.\n", MBPixelCalc(doubleTo6_58(num1), doubleTo6_58(num2)));
}

long doubleTo6_58(double decimal)
{
	return (long) (decimal * power(2, 58));
}

double power(int base, int pow)
{
	if(pow == 0)
	{
		return 1;
	}
	long num= base;
	for(unsigned i= 1; i < pow; i++)
	{
		num*= base;
	}
	return (double)num;
}