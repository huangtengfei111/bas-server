# Bill Analyze System API

Follow these simple steps to run this app:


* AdjustJDBC connection parameters in [database.properties](src/main/resources/database.properties)

* Database

```
mysql> 

USE mysql;
CREATE USER 'admin'@'localhost' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
exit;
```

* Create two empty schemas in MySQL DB

```
mvn db-migrator:create
mvn db-migrator:drop / reset
mvn db-migrator:migrate
```

* Display deps

```
mvn dependency:tree
```

* Run the application

```
mvn jetty:run
```

* Navigate with browser

    to: [http://localhost:8080/](http://localhost:8080/)


# API Tests

* GET resource
```
curl -X GET http://localhost:9091/cases
```

* CREATE resource
```
curl -d '{"name":"TEST 02", "num":"20190501-A2"}' -H "Content-Type: application/json" -X POST http://localhost:9091/cases
```

* UPDATE resource
```
curl -d '{"name":"TEST 02", "num":"20190501-A2"}' -H "Content-Type: application/json" -X PUT http://localhost:9091/cases
```

* DELETE resource
```
curl -X DELETE http://localhost:9091/cases/1
```

* Upload file
```
curl -X POST -H "Content-Type: octet/stream" --data-binary @src/test/resources/ven_numbers.csv http://localhost:9091/cases/1/ven_numbers/upload


curl -H "Origin: http://acme.com/" \
 -H "Access-Control-Request-Method: POST" \
 -H "Access-Control-Request-Headers: X-Requested-With" \
 -X OPTIONS --verbose http://cdn.acme.com/icons.woff

curl --verbose -X POST -H "Origin: http://acme.com/" -H "Access-Control-Request-Method: POST" -H "Content-Type: octet/stream" --data-binary @src/test/resources/ven_numbers.csv http://localhost:9091/cases/1/ven_numbers/upload


curl -i --cookie "cookies.txt" --cookie-jar "cookies.txt" -X POST -H "Content-Type: multipart/form-data" -F "data=@src/test/resources/ven_numbers.csv" http://localhost:9091/cases/1/ven_numbers/upload

curl -i --cookie "cookies.txt" --cookie-jar "cookies.txt" -X POST http://localhost:9091/cases/1/ven_numbers/do-import
curl -i --cookie "cookies.txt" --cookie-jar "cookies.txt" -X POST http://localhost:9091/cases/1/ven_numbers/abort-import


curl -i -X POST http://localhost:9091/cases/1/pbills/do-import/001
```

* Filter
```
curl -X GET http://localhost:9091/cases/filter/archived
curl -X GET http://localhost:9091/cases/filter/active
```
* Search
```
curl -d '{"name":"东方"}' -H "Content-Type: application/json" -X POST http://localhost:9091/cases/search
```

* Gen password

```
mvn exec:java -Dexec.mainClass="app.util.ShiroPasswdGen"

plain: 123456
salt: QZuPGmJycUsF8cKBYrq6HQ==
hashed: 8NUEUtRPFoattXn3E7noBpbr9ftaKgcrrzxJd1Dymk0=

```

curl -i -F "username=root&p=123456" -X POST http://localhost:9091/login

# Resources

* https://www.baeldung.com/javalite-rest


# Login

* hashed message auth code (NMAC)
* https://github.com/javalite/activejdbc/issues/692
* http://javalite.io/scopes

# DEMO
* http://47.111.163.155:8080/#/cases/1/celltowerloc
 
