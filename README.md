# junie2middie-backend

## HTTP Protocol Specification

### Create a contact

`POST /contact`

Creates a new contact using the provided payload in JSON format:
```
    {
      "id": -1, // if id is < 0 we will create a new contact, otherwise we will update the existing, if any 
      "name": "Bob Johnson",
      "status": "Away",
      "avatarUrl": "https://example.com/avatar2.jpg"
    } 
```


### Get a list of contacts

`GET /contacts` 

Returns a list of contacts in JSON format. 
```
{
  "contacts":[
    {
      "id": 1,
      "name": "Alice Smith",
      "status": "Online",
      "avatarUrl": "https://example.com/avatar1.jpg"
    },
    {
      "id": 2,
      "name": "Bob Johnson",
      "status": "Away",
      "avatarUrl": "https://example.com/avatar2.jpg"
    } 
}
```

### Send Message

`POST /message`

Creates a new message using the provided payload in JSON format:

```
{
  "id": -1, // id does not matter when creating a message 
  "content": "Hello, this is a test message",
  "senderId": 0, 
  "receiverId": 1,  
  "timestamp": "2023-06-15T14:30:00", 
}
```

### Get Chat Messages

`GET /contact/{contact1}/messages/{contact2}`

Returns a conversation between the given contact1 and contact2 in JSON format:

```
{
  "messages": [
    {
      "id": 1,
      "content": "Hello, how are you?",
      "senderId": 1,
      "receiverId": 0,
      "timestamp": "2023-06-15T10:30:00"
    },
    {
      "id": 2,
      "content": "I'm good, thanks for asking!",
      "senderId": 0,
      "receiverId": 1,
      "timestamp": "2023-06-15T11:30:00"
    }
 ]
}
```
