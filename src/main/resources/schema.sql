CREATE TABLE IF NOT EXISTS customer_communication (
    id int AUTO_INCREMENT  PRIMARY KEY,
    policy_reference varchar(100) NOT NULL,
    email varchar(100) NOT NULL,
    phone_number VARCHAR(20),
    policy_issued_date TIMESTAMP NOT NULL,
    status varchar(20) NOT NULL
);