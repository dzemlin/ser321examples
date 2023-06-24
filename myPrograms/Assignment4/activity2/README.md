# Assignment 4 Activity 1
## a. Description

ALERT!
I had the project completed except 3 or for requirements when my git repo got corrupted. I lost almost all of my work on assignment 2 =(
So if you see check boxes for things not done, then I am sorry. that is something that was lost in the crash.

This A simple task oriented game that uses protobufs to communicate to a multiplayer (and multi-threaded) server 


## b. How to run

To run the program first start the server with the line below
runServer -Pport=9099

Then run the client with 
gradle runClient -Phost='localhost' -Pport=9099

If you do not pass the shown arguments with some value, they will default to:
host = 'localhost'
port = 8000


## c. How to use

Use is simple, menus and instructions are printed on screen, just do the actions requested by the text, depending on the option chosen.


## d. Video Link

https://youtu.be/DDSdj_SM21Q


Requirements 
[ ] Assignment 2
	[x] 1: Runs via gradle
	[x] 2: Implement Protobuf protocol from given files
	[x] 3: Main menu gives 3 options (leaderbords, play and quit)
	[x] 4: leaderboard works
	[x] 5: Leaderboard is persistant and error free
	[x] 6: New Game Starts
	[x] 7: Multiple clients can enter the SAME GAME!
	[x] 8: Client Wins after compeating image and returns to main menu
	[x] 9: server can send tasks and pass/fail resualts
	[x] 10: Client present information clearly, and answers are simple (one word answers)
	[x] 11: game quits gracefully on option 3
	[x] 12: server does not crash when client disconnects
	[x] 13: tasks should not repeat (use - randomize or shuffle)
	[x] 14: keep server online on AWS
	[x] 15: number of tiles turned is affected my number of games loged into
	[x] 16: Play on the servers of 3 other players
	[ ] 17: make all clients get board update immediatly
	[ ] 18: if player types exit while playing, they gracfully leave the game.
	[x] server prints answer to each question
