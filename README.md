# NOTE: This repository is the client side code for my networked auction application
Link to server side code: https://github.com/MatthewKluyts/NetworkAuctionAppServer

When practicing for my final programming exam in my 3rd year of University, I created this application as practice.
It is a basic, full-duplex networked auction application that allows users to join an auction, and bid on an item. There is a time limit for each auction and updates are sent by the server to all users that are participating in the auction. When the time is over, the winner is announced. Since this app is full-duplex, the client and the server are able to both send and recieve messages from eachother simultaneously. The client side was created as an android application, and the server side is a java console application.

Things I learnt while creating the client side:
- Implementing networked communication using WebSockets for real-time bidding updates.
- Handling background tasks on seperate threads to avoid blocking the UI.
- Managing UI state effectively during network interruptions and reconnections.
- Handling read and write operations on seperate threads to ensure full-duplex communication.
- Error Handling & Debugging such as identifying and resolving issues like connection failures, bid conflicts, and UI glitches.


