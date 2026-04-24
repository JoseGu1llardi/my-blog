INSERT INTO authors (
    user_name, email, password, full_name, slug,
    bio, role, active, token_version, created_at, updated_at
) VALUES (
             'gr1llard',
             'jwribeiro@gmail.com',
             '$2b$10$eWyQ08yib1dvkuuPYs/.B.tKDPPhWRUvnHNLI91XdLmayrG1wBpze',
             'Jose Wellington',
             'gr1llard',
             NULL,
             'ADMIN',
             TRUE,
             1,
             NOW(),
             NOW()
         ) ON CONFLICT (user_name) DO NOTHING;