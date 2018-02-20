# Walmart Ticket Service

One Paragraph of project description goes here

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Installing
I have not used gradle to make a project like this before. I believe these are the only steps:
```
1. git clone this repository
2. Navigate to the main directory with the gradle files
3. type ./gradlew run
```
The command line GUI should pop up and walk you through my program from there

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## High Level Explanation
I implemented the ticket service interface into my Venue class. Inside this class is where the magic happens. This is the "server" that the client or Driver program interacts with. It creates the grid, finds the seats, and displays the venue. When the client requests a seat from the Venue, it sends it off to the SeatHold class. This seathold accepts an arraylist of Seats. These Seats are created when the venue is created and they each have a separate place in memory. This is important because I can then separate out the rows into a Row class with these seat objects. If I edit a referenced Seat in the row class, it affects it elsewhere because it has its own memory. 

After creating the SeatHold, it sends off a Timer Task that expires after a set amount of time. This time can be changed inside the class itself manually. If the user decides to reserve these seats, I update a boolean associated with the seathold object. This is almost like a broadcast method. After the TimerTask expires, it checks the state of the boolean. If it is true, meaning the user moved forward, then we don't do anything. If it is false, meaning the user did not move forward, we release the seats. We do this by adding the seat objects back to the Row and update the remaining seat counters. 

This separation works very cleanly and the architecture mimics the client-server interactions inside a web-app. I assign the confirmation tickets based on Java's UID class. Thus, I changed the implementation of a String SeatID instead of an Integer SeatID.

I use Java's Reentrant locks to ensure concurrency among users.





### Assumptions

* The challenge is left intentionally vague. When determining how to find the best seat possible, I have three metrics.
```
1. If the venue is a movie-esque venue that revolves around central seating, I address this by giving the user the option to create this type of venue.
2. If the venue is a concert venue, I assume that the best possible seat is the at the front. I fill in from the front-left.
3. It also makes sense that the user wants to pick seats that are in the front but also consecutive, so I also give this option.
4. Finally, I allow the user to select a custom row and column at which they can reserve a seat.

```
In order to address the (1), I had to create a graph structure of the venue and assign weights. I assigned weights by taking the index of the row or column and dividing it by half of its length and multiplying it by 100 in order to get a int value instead of a floating point. I add the column and row together to get a comprehensive weighting algorithm with the weights centrally focused. From there, I make a Hashmap that maps each of the weights to Seat objects. I do this because I need to know WHERE to start my Breadth First Search. I could start from 0,0 but that would take too long. Therefore, I start at the highest priority weight and expand from there. This saves having to expand through the graph exponentially. 

For example, a weighted venue can look like this for a 20x20 grid:

```
                          STAGE
0 5 10 15 20 25 30 35 40 45 50 45 40 35 30 25 20 15 10 5 
5 10 15 20 25 30 35 40 45 50 55 50 45 40 35 30 25 20 15 10 
10 15 20 25 30 35 40 45 50 55 60 55 50 45 40 35 30 25 20 15 
15 20 25 30 35 40 45 50 55 60 65 60 55 50 45 40 35 30 25 20 
20 25 30 35 40 45 50 55 60 65 70 65 60 55 50 45 40 35 30 25 
25 30 35 40 45 50 55 60 65 70 75 70 65 60 55 50 45 40 35 30 
30 35 40 45 50 55 60 65 70 75 80 75 70 65 60 55 50 45 40 35 
35 40 45 50 55 60 65 70 75 80 85 80 75 70 65 60 55 50 45 40 
40 45 50 55 60 65 70 75 80 85 90 85 80 75 70 65 60 55 50 45 
45 50 55 60 65 70 75 80 85 90 95 90 85 80 75 70 65 60 55 50 
50 55 60 65 70 75 80 85 90 95 100 95 90 85 80 75 70 65 60 55 
45 50 55 60 65 70 75 80 85 90 95 90 85 80 75 70 65 60 55 50 
40 45 50 55 60 65 70 75 80 85 90 85 80 75 70 65 60 55 50 45 
35 40 45 50 55 60 65 70 75 80 85 80 75 70 65 60 55 50 45 40 
30 35 40 45 50 55 60 65 70 75 80 75 70 65 60 55 50 45 40 35 
25 30 35 40 45 50 55 60 65 70 75 70 65 60 55 50 45 40 35 30 
20 25 30 35 40 45 50 55 60 65 70 65 60 55 50 45 40 35 30 25 
15 20 25 30 35 40 45 50 55 60 65 60 55 50 45 40 35 30 25 20 
10 15 20 25 30 35 40 45 50 55 60 55 50 45 40 35 30 25 20 15 
5 10 15 20 25 30 35 40 45 50 55 50 45 40 35 30 25 20 15 10 

```
And would pick these 10 seats marked by the 'x'

```
------[[ STAGE ]]------
--------------------
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
sssssssssxxsssssssss
ssssssssxxxxxsssssss
sssssssssxxxssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
ssssssssssssssssssss
```
In order to address (2), I created an architecture where I separate out the rows. This 'Bucket System' Architecture cuts down on run time. Instead of having to search through mxn rows and columns , or even n columsns, I ask if there is enough seats in this Row (bucket) and pop them from the stack if there is enough. This is O(1) time.

In order to address consecutive seating (3), I have to search through the entire row taking O(N) column time. This has to be done to ensure that there are consecutive seats available. If I was simply choosing the best seat, it would take O(1) time as stated in the paragprah above. 

Finally, to address individual seating (4), I ask for input of a row and column and remove it from remove it from the stack. Since I have to find the Seat object in the stack, I believe this takes O(N) time. I could convert to array but and get the index but I think the seats are not in order and I may have to sort it. Also, I think converting from stack to array takes O(N) time since I believe the Stack is built on top of a Linkedlist implementation opposed to an array implementation of a stack. 

```
Give an example
```

### And coding style tests

I did include tests to check for concurrency. I have been testing this like crazy with custom input, but I needed to write some tests for concurrency. When I try to run them on gradle, the print statements are not working for some reason. I have ran these offline and they pass. They're included. 

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With
* Java
* Gradle



## Authors

* **Greg Aronson** 


