-- Usuários de DEMONSTRAÇÃO (só para a aula). Em produção esta pasta (db/seed) não é carregada.
--
-- Senhas (só existem aqui no comentário; no banco vai apenas o hash BCrypt):
--   admin / Admin@12345   (papel ADMIN)
--   ana   / Senha@12345   (papel USER)
--
-- Os hashes abaixo foram gerados com BCryptPasswordEncoder (custo 10). O "$2a$10$" do início
-- diz o algoritmo e o custo; o resto do prefixo é o salt. Por isso dois usuários com a MESMA
-- senha teriam hashes diferentes.

INSERT INTO usuarios (username, senha_hash, papel)
VALUES ('admin', '$2a$10$OmO7cMNip8BOEp0V85Qxguq/G/frEmLhBfk0lwvNkyGcp8FmlYZWq', 'ADMIN');

INSERT INTO usuarios (username, senha_hash, papel)
VALUES ('ana', '$2a$10$oKbqdsoFGhV3BHy2QEEY/Ocgu7aNflA.W8qBQCrjozEi6ODApA3lm', 'USER');
