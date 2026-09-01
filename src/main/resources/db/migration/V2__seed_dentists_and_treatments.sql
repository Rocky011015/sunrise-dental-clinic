-- =========================================================
-- Sunrise Dental Clinic
-- Reference data for appointment scheduling
-- =========================================================


-- -------------------------
-- Dentists
-- -------------------------

INSERT INTO dentists (
    dentist_code,
    full_name,
    specialization,
    consultation_fee,
    active,
    version
)
SELECT
    'DEN-001',
    'Dr. Nimal Perera',
    'General Dentistry',
    3500.00,
    TRUE,
    0
    WHERE NOT EXISTS (
    SELECT 1
    FROM dentists
    WHERE dentist_code = 'DEN-001'
);


INSERT INTO dentists (
    dentist_code,
    full_name,
    specialization,
    consultation_fee,
    active,
    version
)
SELECT
    'DEN-002',
    'Dr. Anjali Fernando',
    'Orthodontics',
    4500.00,
    TRUE,
    0
    WHERE NOT EXISTS (
    SELECT 1
    FROM dentists
    WHERE dentist_code = 'DEN-002'
);


INSERT INTO dentists (
    dentist_code,
    full_name,
    specialization,
    consultation_fee,
    active,
    version
)
SELECT
    'DEN-003',
    'Dr. Kasun Silva',
    'Oral Surgery',
    5000.00,
    TRUE,
    0
    WHERE NOT EXISTS (
    SELECT 1
    FROM dentists
    WHERE dentist_code = 'DEN-003'
);


-- -------------------------
-- Treatments
-- -------------------------

INSERT INTO treatments (
    treatment_code,
    treatment_name,
    description,
    base_price,
    estimated_duration_minutes,
    active,
    version
)
SELECT
    'TRT-001',
    'Dental Examination',
    'Routine dental examination and oral health assessment.',
    2500.00,
    30,
    TRUE,
    0
    WHERE NOT EXISTS (
    SELECT 1
    FROM treatments
    WHERE treatment_code = 'TRT-001'
);


INSERT INTO treatments (
    treatment_code,
    treatment_name,
    description,
    base_price,
    estimated_duration_minutes,
    active,
    version
)
SELECT
    'TRT-002',
    'Teeth Cleaning',
    'Professional scaling and cleaning procedure.',
    4000.00,
    45,
    TRUE,
    0
    WHERE NOT EXISTS (
    SELECT 1
    FROM treatments
    WHERE treatment_code = 'TRT-002'
);


INSERT INTO treatments (
    treatment_code,
    treatment_name,
    description,
    base_price,
    estimated_duration_minutes,
    active,
    version
)
SELECT
    'TRT-003',
    'Dental Filling',
    'Restoration of a tooth affected by decay or minor damage.',
    6500.00,
    45,
    TRUE,
    0
    WHERE NOT EXISTS (
    SELECT 1
    FROM treatments
    WHERE treatment_code = 'TRT-003'
);


INSERT INTO treatments (
    treatment_code,
    treatment_name,
    description,
    base_price,
    estimated_duration_minutes,
    active,
    version
)
SELECT
    'TRT-004',
    'Tooth Extraction',
    'Removal of a damaged or problematic tooth.',
    8000.00,
    60,
    TRUE,
    0
    WHERE NOT EXISTS (
    SELECT 1
    FROM treatments
    WHERE treatment_code = 'TRT-004'
);


INSERT INTO treatments (
    treatment_code,
    treatment_name,
    description,
    base_price,
    estimated_duration_minutes,
    active,
    version
)
SELECT
    'TRT-005',
    'Orthodontic Consultation',
    'Initial orthodontic assessment and treatment planning.',
    5000.00,
    45,
    TRUE,
    0
    WHERE NOT EXISTS (
    SELECT 1
    FROM treatments
    WHERE treatment_code = 'TRT-005'
);