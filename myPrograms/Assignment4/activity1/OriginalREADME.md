# Assignment 4 Activity 1
## Description
The initial Performer code only has one function for adding strings to an array: 

## Protocol

### Requests
request: { "selected": <int: 1=add, 2=clear, 3=find, 4=display, 5=sort, 6=prepend
0=quit>, "data": <thing to send>}

  add: data <string> -- a string to add to the list, this should already mostly work
  clear: data <> -- no data given, clears the whole list
  find: data <string> -- display index of string if found, else -1
  display: data <> -- no data given, displays the whole list
  sort: data <> -- no data given, sorts the list
  prepend: data <int> <string> -- index and string, prepends given string to string at index

### Responses

success response: {"ok" : true, type": <String> "data": <thing to return> }

type <String>: echoes original selected from request
data <string>: 
    add: return current list
    clear: return empty list
    find: return integer value of index where that string was found or -1
    display: return current
    sort: return current list
    prepend: return current list


error response: {"ok" : false, "message"": <error string> }
error string: Should give good error message of what went wrong


## How to run the program
### Terminal
Base Code, please use the following commands:
```
    For Server, run "gradle runServer -Pport=9099 -q --console=plain"
```
```   
    For Client, run "gradle runClient -Phost=localhost -Pport=9099 -q --console=plain"
```   



