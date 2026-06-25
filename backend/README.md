# City Repair Backend

Spring Boot 3 backend for the city repair and work order processing system.

## Database

Use this database name:

```text
city_repair
```

Initialize it with:

```text
../sql/init_city_repair.sql
```

## Configuration

Runtime configuration is read from environment variables. See `.env.example`.

Minimum local variables:

```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=city_repair
DB_USERNAME=root
DB_PASSWORD=your-password
```

## Run

```bash
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:8080/api
```

Health check:

```text
GET http://localhost:8080/api/health
```
