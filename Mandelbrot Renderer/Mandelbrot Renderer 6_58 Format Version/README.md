Converted pseudocode for a Mandelbrot Renderer into Assembly Language (x86_64). In the assembly code instead of representing decimals in IEEE Floating Point encoding I used a custom encoding where the first 6 bits represented whole numbers and the other bits represented the fraction (encoding also used 2's compliment to be able to represent negative numbers).
