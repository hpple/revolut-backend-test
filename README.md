Revolut Backend Test
=======

[![Build Status](https://travis-ci.com/hpple/revolut-backend-test.svg?branch=master)](https://travis-ci.com/hpple/revolut-backend-test)

Since there is almost no business logic, testing is represented by end-to-end scenarios only.

For simplicity reasons *jooq-codegen* output is already generated & committed to VCS. 
To regenerate it use:
```
gradlew jooqCodegen
```

To build standalone executable jar use:
```
gradlew fatJar
```

Run:
```
java -jar money-transfer-app.jar
```

Application is using in-memory RDBMS. Web server is listening on 8080 port.

### API ###
 
#### Base URL ####
``
http://localhost:8080/api/v1
``

#### Operations ####
```
GET /accounts
GET /accounts/:id
GET /accounts/:id/transfers
POST /accounts

GET /transfers
GET /transfers/:id
POST /transfers
```

For simplicity, number of digits to the right of the decimal point is expected to be <= 2 for any incoming money amount.
 
Self transfers are forbidden.