-- Criação das tabelas

CREATE TABLE IF NOT EXISTS Grupos (
                                      GID INTEGER PRIMARY KEY,
                                      nome TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Usuarios (
                                        UID INTEGER PRIMARY KEY AUTOINCREMENT,
                                        nome TEXT NOT NULL,
                                        login TEXT UNIQUE NOT NULL,
                                        grupo_id INTEGER NOT NULL,
                                        senha_hash TEXT NOT NULL,
                                        totp_secreto_criptografado BLOB NOT NULL,
                                        num_acessos INTEGER NOT NULL DEFAULT 0,
                                        tentativas_senha INTEGER NOT NULL DEFAULT 0,
                                        tentativas_totp INTEGER NOT NULL DEFAULT 0,
                                        last_time_blocked TIMESTAMP DEFAULT NULL,
                                        FOREIGN KEY (grupo_id) REFERENCES Grupos(GID)
    );

CREATE TABLE IF NOT EXISTS Chaveiro (
                                        KID INTEGER PRIMARY KEY AUTOINCREMENT,
                                        UID INTEGER NOT NULL,
                                        certificado_digital BLOB NOT NULL,
                                        chave_privada_criptografada BLOB NOT NULL,
                                        FOREIGN KEY (UID) REFERENCES Usuarios(UID)
    );

CREATE TABLE IF NOT EXISTS Mensagens (
                                         MID INTEGER PRIMARY KEY,
                                         texto TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Registros (
                                         RID INTEGER PRIMARY KEY AUTOINCREMENT,
                                         MID INTEGER NOT NULL,
                                         usuario TEXT,
                                         arquivo TEXT,
                                         data_hora TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (MID) REFERENCES Mensagens(MID)
    );

-- Inserção dos grupos
INSERT OR IGNORE INTO Grupos (GID, nome) VALUES
(1, 'Administrador'),
(2, 'Usuário');

-- Inserção das mensagens
INSERT OR IGNORE INTO Mensagens (MID, texto) VALUES
(1001, 'Sistema iniciado.'),
(1002, 'Sistema encerrado.'),
(1003, 'Sessão iniciada para <login_name>.'),
(1004, 'Sessão encerrada para <login_name>.'),
(1005, 'Partida do sistema iniciada para cadastro do administrador.'),
(1006, 'Partida do sistema iniciada para operação normal pelos usuários.'),

-- Autenticação - Etapa 1
(2001, 'Autenticação etapa 1 iniciada.'),
(2002, 'Autenticação etapa 1 encerrada.'),
(2003, 'Login name <login_name> identificado com acesso liberado.'),
(2004, 'Login name <login_name> identificado com acesso bloqueado.'),
(2005, 'Login name <login_name> não identificado.'),

-- Autenticação - Etapa 2
(3001, 'Autenticação etapa 2 iniciada para <login_name>.'),
(3002, 'Autenticação etapa 2 encerrada para <login_name>.'),
(3003, 'Senha pessoal verificada positivamente para <login_name>.'),
(3004, 'Primeiro erro da senha pessoal contabilizado para <login_name>.'),
(3005, 'Segundo erro da senha pessoal contabilizado para <login_name>.'),
(3006, 'Terceiro erro da senha pessoal contabilizado para <login_name>.'),
(3007, 'Acesso do usuario <login_name> bloqueado pela autenticação etapa 2.'),

-- Autenticação - Etapa 3
(4001, 'Autenticação etapa 3 iniciada para <login_name>.'),
(4002, 'Autenticação etapa 3 encerrada para <login_name>.'),
(4003, 'Token verificado positivamente para <login_name>.'),
(4004, 'Primeiro erro de token contabilizado para <login_name>.'),
(4005, 'Segundo erro de token contabilizado para <login_name>.'),
(4006, 'Terceiro erro de token contabilizado para <login_name>.'),
(4007, 'Acesso do usuario <login_name> bloqueado pela autenticação etapa 3.'),

-- Tela principal
(5001, 'Tela principal apresentada para <login_name>.'),
(5002, 'Opção 1 do menu principal selecionada por <login_name>.'),
(5003, 'Opção 2 do menu principal selecionada por <login_name>.'),
(5004, 'Opção 3 do menu principal selecionada por <login_name>.'),

-- Tela de cadastro
(6001, 'Tela de cadastro apresentada para <login_name>.'),
(6002, 'Botão cadastrar pressionado por <login_name>.'),
(6003, 'Senha pessoal inválida fornecida por <login_name>.'),
(6004, 'Caminho do certificado digital inválido fornecido por <login_name>.'),
(6005, 'Chave privada verificada negativamente para <login_name> (caminho inválido).'),
(6006, 'Chave privada verificada negativamente para <login_name> (frase secreta inválida).'),
(6007, 'Chave privada verificada negativamente para <login_name> (assinatura digital inválida).'),
(6008, 'Confirmação de dados aceita por <login_name>.'),
(6009, 'Confirmação de dados rejeitada por <login_name>.'),
(6010, 'Botão voltar de cadastro para o menu principal pressionado por <login_name>.'),

-- Tela de consulta
(7001, 'Tela de consulta de arquivos secretos apresentada para <login_name>.'),
(7002, 'Botão voltar de consulta para o menu principal pressionado por <login_name>.'),
(7003, 'Botão Listar de consulta pressionado por <login_name>.'),
(7004, 'Caminho de pasta inválido fornecido por <login_name>.'),
(7005, 'Arquivo de índice decriptado com sucesso para <login_name>.'),
(7006, 'Arquivo de índice verificado (integridade e autenticidade) com sucesso para <login_name>.'),
(7007, 'Falha na decriptação do arquivo de índice para <login_name>.'),
(7008, 'Falha na verificação (integridade e autenticidade) do arquivo de índice para <login_name>.'),
(7009, 'Lista de arquivos presentes no índice apresentada para <login_name>.'),
(7010, 'Arquivo <arq_name> selecionado por <login_name> para decriptação.'),
(7011, 'Acesso permitido ao arquivo <arq_name> para <login_name>.'),
(7012, 'Acesso negado ao arquivo <arq_name> para <login_name>.'),
(7013, 'Arquivo <arq_name> decriptado com sucesso para <login_name>.'),
(7014, 'Arquivo <arq_name> verificado (integridade e autenticidade) com sucesso para <login_name>.'),
(7015, 'Falha na decriptação do arquivo <arq_name> para <login_name>.'),
(7016, 'Falha na verificação (integridade e autenticidade) do arquivo <arq_name> para <login_name>.'),

-- Tela de saída
(8001, 'Tela de saída apresentada para <login_name>.'),
(8002, 'Botão encerrar sessão pressionado por <login_name>.'),
(8003, 'Botão encerrar sistema pressionado por <login_name>.'),
(8004, 'Botão voltar de sair para o menu principal pressionado por <login_name>.');
