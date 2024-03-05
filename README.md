## Security Configuration

1. **Public Endpoints**: All GET endpoints are accessible without authentication.
2. **Secured Endpoints**: POST/PUT/DELETE operations require authorization using a Bearer token.

### Obtaining a Bearer Token

BookStore service uses **Auth0 Authorization server** as JWT token issuer. \
To obtain a Bearer Token, follow these steps:

```bash
curl --location 'http://localhost:8181/api/oauth/token'
```

**Response:**
```json
{
    "access_token": "jwt-token",
    "expires_in": 86400,
    "token_type": "Bearer"
}
```

In the current application, machine-to-machine authentication is implemented through Auth0.
https://auth0.com/blog/using-m2m-authorization/


```
   +---------+                                   +---------------+
   |         |                                   |               |
   |         |>--(A)- Client Authentication ---> |     Auth0     |
   |         |                                   | Authorization |
   |         |                                   |     Server    |
   | Client  |                                   |               |
   |         |<--(B)---- Access Token ---------< |               |
   |         |                                   +---------------+
   |         |                                   |               |
   |         |                                   |    BookStore  |
   |         |                                   |     Service   |
   |         |>--(C)- Call private (scoped) API->|               |               
   +---------+                                   +---------------+

         Client Credentials Flow from the OAuth 2.0 RFC 6749
            
   (A)  The client authenticates with the authorization server and
        requests an access token from the token endpoint.
    
   (B)  The authorization server authenticates the client, and if valid,
        issues an access token.        
            
   (C)  The client calls your API with the issued token.     
```

## Test Data

To populate the database with test data, use the following cURL commands. Ensure to maintain the order of requests for proper data insertion. The test files are located at `src/main/resources/data/*.csv`.

```bash
curl --location 'http://localhost:8080/api/csv/upload/books' \
--form 'file=@"/book-service/src/main/resources/data/books.csv"'

curl --location 'http://localhost:8080/api/csv/upload/users' \
--form 'file=@"/book-service/src/main/resources/data/users.csv"'

curl --location 'http://localhost:8080/api/csv/upload/ratings' \
--form 'file=@"/book-service/src/main/resources/data/ratings.csv"'
```

## Running the Application

Follow these steps to run the application:

1. **Docker**: Execute `docker-compose up --build`.
2. **Upload Test Data**: Use the provided API endpoints to upload test data.
3. **Explore**: Access the application and explore its functionalities.