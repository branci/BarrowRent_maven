CREATE TABLE "BARROW" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "USE" VARCHAR(255) NOT NULL,
    "VOLUMELT" DOUBLE
);

CREATE TABLE "CUSTOMER" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "FULLNAME" VARCHAR(255) NOT NULL,
    "BIRTHDATE" DATE NOT NULL,
    "IDCARD" VARCHAR(255) NOT NULL
);

CREATE TABLE "LEASE" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "CUSTOMERID" BIGINT NOT NULL REFERENCES CUSTOMER (ID),
    "BARROWID" BIGINT NOT NULL REFERENCES BARROW (ID),
    "PRICE" DECIMAL,    --BIGINT???
    "REALENDTIME" DATE,
    "STARTTIME" DATE,
    "EXPECTEDENDTIME" DATE
);