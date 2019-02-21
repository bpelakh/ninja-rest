# ninja-rest

Simple RMM billing system.

## Build

`mvn clean package` will generate `bpelakh-ninja.war` ready for deployment.

## Deployment

* Copy `bpelakh-ninja.war` into the webapps directory of your favorite server. I tested using Tomcat 9.x.
* Execute `src/main/resources/createDB.sql` in Postgres to initialize the schema. This script also 
  * primes 
the `service_defs` table with some pricing information and 
  * creates two entries in the `users` table (`Joe` and `Admin`) in order to allow for secure access.
* Configure DB access for the application by setting the following system properties in your server:
  * `rmm.db.url` - JDBC URL.
  * `rmm.db.user`
  * `rmm.db.password`

## API

The endpoint is secured using basic authentication, with read-only methods accessible to any
authenticated user and data-altering methods only open to users with the `WRITER` role. Users 
are defined in the `users` table in the DB, passwords are open text for simplicity, not security.

Methods:
* `GET /rmm/devices/{customer}` - get the list of devices for a given customer
* `POST /rmm/devices/{customer}` - add new devices for the customer. The body of the request
   is JSON, you can look at `src/test/resources/devices.json` for an example.
* `PUT /rmm/devices/{customer}` - update devices for the customer. The body of the request
   is JSON, and only already-present devices will be updated.
* `DELETE /rmm/devices/{customer}/{device}` - delete a customer device by ID.

* `GET /rmm/services/{customer}` - get the list of services for a given customer
* `POST /rmm/services/{customer}` - add new services for the customer. The body of the request
   is a JSON list, e.g. `["Antivirus","Cloudberry","TeamViewer"]`.
* `DELETE /rmm/devices/{customer}/{service}` - delete a customer service.

* `GET /rmm/cost/{customer}` - retrieve the monthly total cost for a customer.
