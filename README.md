# BFHL API

Spring Boot REST API for `POST /bfhl`.

## Run locally

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080` by default.

## Request

```http
POST /bfhl
X-Request-Id: REQ-1001
Content-Type: application/json
```

```json
{
  "data": ["A", "1", "22", "$", "B", "7"]
}
```

## Deployment

The app is Render/Railway friendly:

- Build command: `mvn clean package`
- Start command: `java -jar target/bfhl-api-0.0.1-SNAPSHOT.jar`
- Port: read from `PORT`

Set these optional environment variables before deployment:

- `BFHL_USER_ID`
- `BFHL_EMAIL`
- `BFHL_ROLL_NUMBER`
- `BFHL_ASYNC_THRESHOLD`
