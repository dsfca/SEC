# SEC
Highly Dependable Systems

the goal of the project is to implement a highly dependabe location tracker(HDLT) system with
following characteristics:
Users periodically send their location to a server. To do this they perform the
following steps:
                ○ A user that wants to prove its location - known as the prover -
                  broadcasts a location proof request to other users that might be
                 nearby.
                ○ A user that receives a location proof request - known as the witness -
                   checks if the prover is close enough and if that is the case sends back
                   a reply. The reply encodes the fact that those two users were nearby at
                   that time.
                ○ After receiving the proof(s), the user sends it/them to the location
                 server.
                
To run the project first clean and compile using maven build(you don't need
to put these goals into in the goal parameter because they are already configured on POM file), 
you only need to run the pom file.

TrackeLocationSystem is the class that starts all the nodes of the system(users and server).
To execure the main function in this class you need to pass as argument the following arguments:
- num_of_users: number of users that you want in the system.
-G_width: the width of the grid where the users will be spreaded
-G_height: height of the width.

there are 3 test at path src/test/java (userTest-> represent normal operation of user like submitreport and obtain location of its own position,
BizantineTest-> attacker that try to attack the system and HATest-> to query the server. 

Note: each user is operating on port 9090 + ID where the ID is the user ID.
The grid is changed for every time that the TrackerLocationSystem is executed.
So to confirm the results that you get from tests you will need to verify the grids.



