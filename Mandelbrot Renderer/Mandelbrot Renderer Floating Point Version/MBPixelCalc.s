.section .data

.section .text
.globl MBPixelCalc
#x0 in %xmm0, y0 in %xmm1
MBPixelCalc:
	movl	$4, %r9d
	movl	$0, %eax
	movl	$1000, %r10d	#store the max iterations in %r10
	
	vcvtsi2sd	%rax, %xmm8, %xmm8		#x
	vcvtsi2sd	%rax, %xmm9, %xmm9		#y
	
	vcvtsi2sd	%r9d, %xmm13, %xmm13
		
	jmp .L2
.L1:
	#calculate y
	vmulsd		%xmm9, %xmm8, %xmm9		#multiply x times y
	vaddsd		%xmm9, %xmm9, %xmm9		#multiply by 2
	vaddsd		%xmm1, %xmm9, %xmm9		#add y0
	
	#calculate x
	vsubsd		%xmm11, %xmm10, %xmm8			#subtract x sqaured from y sqaured
	vaddsd		%xmm8, %xmm0, %xmm8				#store x0 plus x sqaured minus y sqaured
	
	addl	$1, %eax					#increment counter for number of iterations
.L2:
	vmulsd		%xmm8, %xmm8, %xmm10	#store x sqaured in %xmm10

	vmulsd		%xmm9, %xmm9, %xmm11	#store y sqaured in %xmm11
	
	vaddsd		%xmm10, %xmm11, %xmm12	#set up for comparison
	
	cmpl	%eax, %r10d
	
	je .L3		#jump if we are up to 1000 iterations
	
	vucomisd	%xmm13, %xmm12
	
	jbe .L1		#jump if x sqaured plus y sqaured is less than or equal to 4
.L3:
	ret
