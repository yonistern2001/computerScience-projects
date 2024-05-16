Multi-Threaded Client

Converted single threaded REST client to multi threaded client to improve performance. Supports 2 scheduling modes, FIFO and CONCUR. FIFO sends GET requests in thread order (thread 1 sends then thread 2 ...). CONCUR sends all GET requests at the same time. If 2 files are given as arguments, code will switch between the 2 files when sending GET requests.
