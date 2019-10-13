CREATE TABLE accounts
(
	account_id IDENTITY NOT NULL PRIMARY KEY,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
	balance DECIMAL(20,2) DEFAULT 0 CHECK (balance >= 0)
);

CREATE TABLE transfers (
  transfer_id IDENTITY NOT NULL PRIMARY KEY,
  from_account_id BIGINT NOT NULL,
  to_account_id BIGINT NOT NULL,
  transfered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  amount DECIMAL(20,2) CHECK (amount > 0),

  FOREIGN KEY (from_account_id) REFERENCES accounts(account_id),
  FOREIGN KEY (to_account_id) REFERENCES accounts(account_id),

  CONSTRAINT NO_SELF_TRANSFERS CHECK (from_account_id != to_account_id)
);
