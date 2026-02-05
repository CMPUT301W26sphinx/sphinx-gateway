# Event Lottery System Application - Gateway

This is a W26 Project for CMPUT 301 University of Alberta.
Gateway is an android application for signing up into community events. It is designed to work best for events that has limited sign ups, and a lot of interest from people.

### How it works:
An event organizer posts an event in the app, specifying how much people the event is allowed to. Entrants will sign up into the event to give interest. If it surpasses the limit, it will hold a lottery to which the entrants who win spots can either sign up to join, or pass the ticket to another entrant.

### Features:
1. Pooling System
    - Organizers can draw from a waiting list of interested event attendees as selected participants.
2. QR Code Scanning
    - Entrants can scan QR promotional code to view details about the event and also join the waiting list
3. Firebase Integration
    - Utilize Firebase for storing event details, attendee lists, and real-time check-in status updates.
4. Multi-user Interaction
    - Distinguish between entrants, organizers, and admin with special roles and privileges granted to each actor.
5. Image Upload
    - Allow event organizers upload event poster image
  
### Users:
- Entrant: a person who signs up for an event
- Organizer: the entity that runs the event
- Administrator: The entity that administers and runs the infrastructure
