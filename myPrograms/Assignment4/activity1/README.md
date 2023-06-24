# Assignment 4 Activity 1
## a. Description

This activity demonstrates three different connection types use on the same core logic classe. runTask1 runs the server via a direct single instance blocking connection  runTask2 runs the server via an unbounded threaded server allowing any number of clients. Task 3 runs the server via a set limited number of threads in a thread pool, only connecting with a number of threads given as an argument at start up.


## b. How to run

To run the program first decide which version of the server you want to run, then call it with one of the 3 lines below:gradle runTask1 -Pport=9099 -q --console=plaingradle runTask2 -Pport=9099 -q --console=plaingradle runTask3 -Pport=9099 -Psize=5 -q --console=plain
Then run the Client with this line:
gradle runClient -Phost=localhost -Pport=9099 -q --console=plain

If you do not pass the shown arguments with some value, they will default to:host = 'localhost'port = 8000
size = 3

-q and --console=plain are optional. used to keep the command line clean.


## c. How to use

Use is simple, menus and instructions are printed on screen, just do the actions requested by the text, depending on the option chosen.

## d. Video Link

https://youtu.be/toAon4DT-pw


Requirements 
[x] Assignment 1
	[x] Task 1: Finish Performer commands
	[x] Task 2: Make server MUlti Threaded
		[x] Make Class named "ThreadedServer"
		[x] Allow Unbounded connections
		[x] No Clients should be blocked
		[x] StringList's shared state is well managed	
	[x] Task 3: Create Bounded version
		[x] Make Class named "ThreadPoolServer"
		[x] Make Thread Limit Set by argument
	[x] Gradle
		[x] One gradle file for all three tasks
		[x] default: host=localhost port=8000
		[x] Runs with no aruments
