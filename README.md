# 1 Security Configuration

## OAuth 2.0 Client Credentials Flow

👉  See the [OAuth 2.0 RFC 6749](https://tools.ietf.org/html/rfc6749#section-4.4)


     +---------+                                   +---------------+
     |         |                                   |               |
     |         |>--(A)- Client Authentication ---> | Authorization |
     | Client  |                                   |     Server    |
     |         |<--(B)---- Access Token ---------< |               |
     |         |                                   +---------------+
     |         |                                   |               |
     |         |                                   |    App        |   
     |         |>--(C)- Call private (scoped) API->|               |               
     +---------+                                   +---------------+

            Client Credentials Flow from the OAuth 2.0 RFC 6749
            
       (A)  The client authenticates with the authorization server and
            requests an access token from the token endpoint.
    
       (B)  The authorization server authenticates the client, and if valid,
            issues an access token.        
            
       (C)  The client calls your API with the issued token.     



