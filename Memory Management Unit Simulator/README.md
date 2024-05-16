MMU Simulator

Created a program that simulates how an MMU chip works. Lets users store and read data at inputted memory address (virtual address). Used a page table to map virtual addresses to pages. Simulated evictions (when all virtual pages are in use but another page is needed) using a fake disc. On evictions I used FIFO to decide which page to move to disc (only needed to copy page to disc if dirty bit was set).
