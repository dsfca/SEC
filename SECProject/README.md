### Questions
1. Even the mongoDB does not run, server claims the report was submitted. Some check if it was really submitted?

## Assumption

- N > 3f
- (UserId, Epoch, Report) is unique and can't be modified. 
  Once report is submitted, it can't be updated nor removed.

## Note

The only write request (modifying servers' content) is 
submitLocationReport(report). Because of implementing Byzantine
Reliable Broadcast, we are assured, that if correct server
submit a report, then every correct server eventually submit
the report.

On the top of that, once submitted report can't be modified.
The only potential non-ordering could be seen during the first
(and only) one submit:
```text
    wwwwwwwwwww(r1)
rrrrr(null)  rrrrr(null)
          rrrrr(r1)
```
For that reason, Byzantine Atomic Register should be implemented.

## Submit Report
1. Client sends submit report request (correct client to every server)
   - This is done using asynchronous and secure way.
   - The client now awaits acks from more than (N+f)/2 of servers
2. Server receives submitLocationReport() from a client
   - Incoming message have to be deciphered and validated
     (check if the report does not exist in DB)
3. Server broadcast Echo{report + original signature} to
   all other servers asynchronously and securely, if it
   hasn't sent yet.
4. Once a server receives more than (N+f)/2 of the same message
   and hasn't sent Ready message yet, it broadcasts
   the majority message in Ready{report + original signature}.
   - That means we should temporary store a Map with Echo as
   a key and information about if server already sent an Echo
   and if the incoming message increments the amount of same 
   messages.
5. Once a server receives more than f of the same message and
   hasn't sent Ready message yet, it broadcast the majority
   message in Ready{report + original signature}
   - It works in the same way as with the Echos, we have
   to somehow store the incoming messages.
6. Once a server receives more than 2f of the same message
   it can deliver that majority message.

For that purpose I created two collections:
```
Pair<report, signature> rs;
Pair<sentAck, Set<serverIds>> ss;

Hashtable<rs, ss> echos;
Hashtable<rs, ss> readys;

```
Every time Echo is sent or received, it is added to
echos table. Corresponding sentAck informs, if echo
has been sent. The Set of IDs informs, from which
server the Echo was received. Every time new
echo came, it is also checked if there is more
then quorum of that message. If there is, Ready
message is broadcasted and the system above works
in similar way. 
The problem is - the code is extremely messy,
don't you have any idea?



## Normal user

### SubmitLocationReport(list of proofs, id, epoch, position): out
Create a report from input values.
Send the report securely to all servers asynchronously.
Wait for all replies (or errors/timeouts). Every expected ack
add to Set.
Check if Set contains (N+f)/2 acks.
Done.

### ObtainLocationReport(): out

Send securely and asynchronously read request to all servers,
including nonce to prevent reply attack.

Verify incoming report and save it into array per a server.
As only one report per epoch is allowed, we don't have to
deal with finding the most recent (timestamp, value) pair.
Just accept the report, that is in the array more than (N+f)/2 times.
Send to all ReadingComplete(rid), so they clean the array.

Broadcasting gathered report is not necessary as we are
assured, each server received same information during write.


 


## Correct server

### SubmitLocationReport(report): in
Verify an incoming message, check if the (user, epoch) pair haven't been already submitted,...

If it is a report to store, send securely and asynchronously SubmitEcho(MyId, report) to every server.
Save an information, that echo was sent SentEcho = True.
Save an information, that ready was not sent SentReady = False.

Notify all listening clients (HA or the prover himself), that a write operation
is in a process. Send(report) to the waiting client (how??).

Return ACK to client.

#### SubmitEcho(ServerId, report): in
Have an array Echos\[N\] and store report from each server.
Wait until having more than (N+f)/2 of same reports exist
in Echos array.

Sent SubmitReady(MyId, quorum_report) securely and asynchronously to all servers.
Save an information, that ready was sent SentReady = True.

#### SubmitReady(ServerId, report): in
Have an Readys\[N\] array and store report from each server.
Wait until having more than f of same report in Readys array,
then if SentReady == False, Sent SubmitReady(MyId, f_report) securely
and asynchronously to all servers and set SentReady = True.

Wait until having more than 2f of same messages in Readys array,
then submit the report.

### ObtainLocationReport(): in
Validate user.

Create a Listening array and store nonce for the Read request.
return Report from DB.