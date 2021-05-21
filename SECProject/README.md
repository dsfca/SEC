# SEC
Highly Dependable Systems

the goal of the project is to implement a highly dependabe location
tracker(HDLT) system.

To run the project first clean and compile using maven build (you don't need to
put these goals into in the goal parameter because they are already configured
on POM file), you only need to run the pom file. The system servers work with
MongoDB in background, make sure you have database running to get expected
behavior.

*TrackerLocationSystem* is the class that starts all the nodes of the system
*(users and servers).
To execute the main function in this class you need to pass as argument the
following arguments:
- G_width: the width of the grid where the users will be placed, and
- G_height: height of the width.

There are several unit tests at path *src/test/java*
- User test represents normal operation of user like submit report and obtain location of its own position,
- Byzantine test represents attacker that try to attack the system and

## HA User
The Healthcare Authority simulation is implemented as interactive tool.
To start the tool, launch *HAUser* class passing unique userID.

The CLI accept only following formats:
```
getReport <ID> <EPOCH>
getUsers <POSITION_X> <POSITION_Y> <EPOCH>
```
The first command, *getReport*, performs a call of *ObtainLocationReport()*
function with specified id and epoch as input argument. The latter represents
call of *obtainUsersAtLocation()* with position defined by X and Y coordinates
and specific epoch.

## Additional notes
1. Each user is operating on port 9090 + ID where the ID is the user ID.
The grid is changed for every time that the TrackerLocationSystem is executed.
So to confirm the results that you get from tests you will need to verify the grids.

2. You need to have mongodb installed to store the request locations.
before executes the program, open a terminal and execute the following command: *mongod*
