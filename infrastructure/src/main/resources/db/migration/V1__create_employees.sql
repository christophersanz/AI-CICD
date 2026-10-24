CREATE TABLE employees (
    id              UUID         PRIMARY KEY,
    document_type   VARCHAR(20)  NOT NULL,
    document_number VARCHAR(20)  NOT NULL,
    first_names     VARCHAR(150) NOT NULL,
    last_names      VARCHAR(150) NOT NULL,
    email           VARCHAR(254) NOT NULL,
    CONSTRAINT uk_employees_document UNIQUE (document_type, document_number)
);
