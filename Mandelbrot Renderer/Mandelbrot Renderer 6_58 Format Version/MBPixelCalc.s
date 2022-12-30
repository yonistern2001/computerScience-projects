.section .data

.section .text
.globl MBPixelCalc
#x0 in %rdi, y0 in %rsi
MBPixelCalc:
	pushq	%r12
	pushq	%r13
	pushq	%r14
	pushq	%rbx
	
	movq	$4, %r13
	shlq	$58, %r13	#turn 4 into 6 58 format
	
	movl	$1000, %r14d
	
	movq	%rdi, %rdx	#store x0 in %rdx
	movq	%rsi, %rcx	#store y0 in %rcx

	movl	$0, %r8d	#x
	movl	$0, %r9d	#y
	movl	$0, %ebx
	
	jmp .L2
.L1:
	#calculate y
	movq	%r8, %rdi
	movq	%r9, %rsi
	
	call	Mul
	
	leaq	(%rcx , %rax, 2), %r9	#set y to 2 times x times y plus y0	

	#calculate x
	subq	%r11, %r10			#subtract x sqaured from y sqaured
	leaq	(%r10, %rdx), %r8	#store x0 plus x sqaured minus y sqaured
	
	addl	$1, %ebx
.L2:	
	movq	%r8, %rdi
	movq	%r8, %rsi
	
	call	Mul
	
	movq	%rax, %r10	#store x sqaured in %r10
	movq	%r9, %rdi
	movq	%r9, %rsi
	
	call	Mul
	
	movq	%rax, %r11	#store y sqaured in %r11
	
	leaq	(%r10, %r11), %r12
	
	cmpl	%ebx, %r14d
	
	je .L3		#jump if we are up to 1000 iterations
	
	cmpq	%r13, %r12
	
	jle .L1		#jump if x sqaured plus y sqaured is less than or equal to 4
.L3:
	movl	%ebx, %eax
	
	popq	%rbx
	popq	%r14
	popq	%r13
	popq	%r12
	
	ret

#multiply 2 numbers in 6 58 format
Mul:
	pushq	%rbx
	pushq	%rdx
	movq	%rsi, %rax
	movl	$0, %ebx
	imulq	%rdi
	
	jns .L4
	
	movl	$1, %ebx
	shlq	$64, %rbx	#if negative create the negative bit
.L4:
	shlq	$6, %rdx
	shrq	$58, %rax
	orq		%rdx, %rax
	orq		%rbx, %rax
	
	popq	%rdx
	popq	%rbx
	ret
	