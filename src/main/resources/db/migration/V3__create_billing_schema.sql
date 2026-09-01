CREATE TABLE billings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                          billing_number VARCHAR(30) NOT NULL,

                          appointment_id BIGINT NOT NULL,

                          total_amount DECIMAL(12, 2) NOT NULL,

                          amount_paid DECIMAL(12, 2) NOT NULL DEFAULT 0.00,

                          payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',

                          payment_method VARCHAR(30) NULL,

                          notes VARCHAR(500) NULL,

                          created_by BIGINT NOT NULL,

                          version BIGINT NOT NULL DEFAULT 0,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          updated_at TIMESTAMP NOT NULL
                                                              DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,


                          CONSTRAINT uk_billings_number
                              UNIQUE (billing_number),

                          CONSTRAINT uk_billings_appointment
                              UNIQUE (appointment_id),


                          CONSTRAINT fk_billings_appointment
                              FOREIGN KEY (appointment_id)
                                  REFERENCES appointments(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_billings_created_by
                              FOREIGN KEY (created_by)
                                  REFERENCES app_users(id)
                                  ON DELETE RESTRICT,


                          CONSTRAINT chk_billings_total_amount
                              CHECK (total_amount >= 0),

                          CONSTRAINT chk_billings_amount_paid
                              CHECK (amount_paid >= 0),

                          CONSTRAINT chk_billings_amount_not_overpaid
                              CHECK (amount_paid <= total_amount),

                          CONSTRAINT chk_billings_payment_status
                              CHECK (
                                  payment_status IN (
                                                     'UNPAID',
                                                     'PARTIALLY_PAID',
                                                     'PAID'
                                      )
                                  ),

                          CONSTRAINT chk_billings_payment_method
                              CHECK (
                                  payment_method IS NULL
                                      OR payment_method IN (
                                                            'CASH',
                                                            'CARD',
                                                            'BANK_TRANSFER'
                                      )
                                  ),

                          INDEX idx_billings_payment_status (payment_status),

                          INDEX idx_billings_created_at (created_at)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;