CREATE TABLE app_users (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           username VARCHAR(50) NOT NULL,
                           password_hash VARCHAR(255) NOT NULL,
                           full_name VARCHAR(100) NOT NULL,
                           role VARCHAR(20) NOT NULL,
                           enabled BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT uk_app_users_username UNIQUE (username),
                           CONSTRAINT chk_app_users_role
                               CHECK (role IN ('ADMIN', 'STAFF'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE patients (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          patient_code VARCHAR(20) NOT NULL,
                          full_name VARCHAR(120) NOT NULL,
                          address VARCHAR(255) NOT NULL,
                          contact_number VARCHAR(20) NOT NULL,
                          email VARCHAR(120),
                          date_of_birth DATE,
                          version BIGINT NOT NULL DEFAULT 0,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,
                          CONSTRAINT uk_patients_patient_code UNIQUE (patient_code),
                          INDEX idx_patients_full_name (full_name),
                          INDEX idx_patients_contact_number (contact_number)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE dentists (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          dentist_code VARCHAR(20) NOT NULL,
                          full_name VARCHAR(120) NOT NULL,
                          specialization VARCHAR(100) NOT NULL,
                          consultation_fee DECIMAL(10,2) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          version BIGINT NOT NULL DEFAULT 0,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,
                          CONSTRAINT uk_dentists_dentist_code UNIQUE (dentist_code),
                          CONSTRAINT chk_dentists_consultation_fee
                              CHECK (consultation_fee >= 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE treatments (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            treatment_code VARCHAR(20) NOT NULL,
                            treatment_name VARCHAR(120) NOT NULL,
                            description VARCHAR(500),
                            base_price DECIMAL(10,2) NOT NULL,
                            estimated_duration_minutes INT NOT NULL DEFAULT 30,
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            version BIGINT NOT NULL DEFAULT 0,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
                            CONSTRAINT uk_treatments_code UNIQUE (treatment_code),
                            CONSTRAINT uk_treatments_name UNIQUE (treatment_name),
                            CONSTRAINT chk_treatments_price CHECK (base_price >= 0),
                            CONSTRAINT chk_treatments_duration
                                CHECK (estimated_duration_minutes > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE appointments (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              appointment_number VARCHAR(30) NOT NULL,
                              patient_id BIGINT NOT NULL,
                              dentist_id BIGINT NOT NULL,
                              treatment_id BIGINT NOT NULL,
                              appointment_date DATE NOT NULL,
                              appointment_time TIME NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
                              notes VARCHAR(500),
                              created_by BIGINT NOT NULL,
                              version BIGINT NOT NULL DEFAULT 0,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,

                              CONSTRAINT uk_appointments_number
                                  UNIQUE (appointment_number),

                              CONSTRAINT fk_appointments_patient
                                  FOREIGN KEY (patient_id) REFERENCES patients(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT fk_appointments_dentist
                                  FOREIGN KEY (dentist_id) REFERENCES dentists(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT fk_appointments_treatment
                                  FOREIGN KEY (treatment_id) REFERENCES treatments(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT fk_appointments_created_by
                                  FOREIGN KEY (created_by) REFERENCES app_users(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT chk_appointments_status
                                  CHECK (status IN (
                                                    'SCHEDULED',
                                                    'CONFIRMED',
                                                    'COMPLETED',
                                                    'CANCELLED'
                                      )),

                              INDEX idx_appointments_date_status (
        appointment_date,
        status
    ),

                              INDEX idx_appointments_dentist_slot (
        dentist_id,
        appointment_date,
        appointment_time,
        status
    )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE bills (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       bill_number VARCHAR(30) NOT NULL,
                       appointment_id BIGINT NOT NULL,
                       consultation_fee DECIMAL(10,2) NOT NULL,
                       treatment_cost DECIMAL(10,2) NOT NULL,
                       discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                       total_amount DECIMAL(10,2) NOT NULL,
                       payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                       issued_by BIGINT NOT NULL,
                       issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_bills_bill_number UNIQUE (bill_number),
                       CONSTRAINT uk_bills_appointment UNIQUE (appointment_id),

                       CONSTRAINT fk_bills_appointment
                           FOREIGN KEY (appointment_id) REFERENCES appointments(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT fk_bills_issued_by
                           FOREIGN KEY (issued_by) REFERENCES app_users(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT chk_bills_values CHECK (
                           consultation_fee >= 0
                               AND treatment_cost >= 0
                               AND discount_amount >= 0
                               AND total_amount >= 0
                           ),

                       CONSTRAINT chk_bills_payment_status
                           CHECK (payment_status IN (
                                                     'PENDING',
                                                     'PAID',
                                                     'CANCELLED'
                               ))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE appointment_audit (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   appointment_id BIGINT NOT NULL,
                                   action_type VARCHAR(30) NOT NULL,
                                   old_status VARCHAR(20),
                                   new_status VARCHAR(20),
                                   changed_by BIGINT,
                                   details VARCHAR(500),
                                   changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_audit_appointment
                                       FOREIGN KEY (appointment_id) REFERENCES appointments(id)
                                           ON DELETE RESTRICT,

                                   CONSTRAINT fk_audit_changed_by
                                       FOREIGN KEY (changed_by) REFERENCES app_users(id)
                                           ON DELETE SET NULL,

                                   INDEX idx_audit_appointment (appointment_id),
                                   INDEX idx_audit_changed_at (changed_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;