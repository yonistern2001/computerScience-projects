#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
	extern int MBPixelCalc(double, double);
	
	double num1= atof(argv[1]);
	double num2= atof(argv[2]);	
	
	printf("MBPixelCalc() returned %d.\n", MBPixelCalc(num1, num2));
}