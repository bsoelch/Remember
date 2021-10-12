# Remember
Simple interpreted [esoteric programing language](
https://esolangs.org
)


Like the human short term memory this language can 
only remember a limited number (8) objects at any time
and forgets all additional objects without warning.

## Usage
Write the code in a text-file.
run Main.main.
The program should output:
```
Input a Filename:
```
Supply the name of the file to run, 
use the prefix "." for files in the local directory

##Syntax:
the program is organized in lines of operations and values 
separated by spaces.

the program is executed line by line.
if the program is unable to execute a line,
it simply starts executing the next line

##Memory:
The program can remember values with arbitrary names
of the form 
```
[a-zA-Z][a-zA-Z_0-9]*
```

The "forgetting" process works in the following way
- when remembering a variable it is stored on top of the memory
- using a values brings it up to the top of the memory
- if the memory is full the lowest value in the memory is removed

## Root Operations:
### Remember
remembers the given values and stores it under the given ID
```
REMEMBER [ID] [Value]
```
### Forget
forgets the value associated with the given id
```
FORGET [ID]
```
### Jump
Jumps to the given id
```
JUMP [ID] 
```
### JGT, JGE, JNE
jumps to the value of ID if Value is >,>=,!= 0
```
JGT [ID] [Value]
JGE [ID] [Value]
JNE [ID] [Value]
```
### Print
Prints the given Value
```
PRINT [Value] 
```
##Values
A value is either a 32-bit integer,
a Memory id or an Operation on another value


Each element on the operation-stack takes 
one memory slot therefore long operation chains 
clear most of the memory
### Operations
#### Not
1 if value is 0, otherwise 0
```
NOT [Value] 
```
#### Negate, Neg
negates the given value
```
NEG [Value] 
NEGATE [Value] 
```
#### And
bitwise and of the given values
```
AND [Value] [Value] 
```
#### Add
Adds the given values
```
ADD [Value] [Value] 
```
#### Line
The current line Number
```
LINE
```
#### Read
Reads the next line, and converts it to an integer if possible
```
READ
```



