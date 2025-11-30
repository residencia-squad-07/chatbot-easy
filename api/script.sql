-- Active: 1763063532668@@0.tcp.sa.ngrok.io@11080@easy
-- Drop tables if exist (ordem correta para evitar erro de FK)
DROP TABLE IF EXISTS `ConfiguracaoERP`;
DROP TABLE IF EXISTS `Solicitacao`;
DROP TABLE IF EXISTS `Agendamento`;
DROP TABLE IF EXISTS `User_Admin`;
DROP TABLE IF EXISTS `Usuario`;
DROP TABLE IF EXISTS `Empresa`;
DROP TABLE IF EXISTS `Administrador`;

-- CreateTable
CREATE TABLE `Administrador` (
    `id_admin` INTEGER NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(191) NOT NULL,
    `email` VARCHAR(191) NOT NULL,
    `senha` VARCHAR(191) NOT NULL,
    UNIQUE INDEX `Administrador_email_key`(`email`),
    PRIMARY KEY (`id_admin`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `Empresa` (
    `id_empresa` INTEGER NOT NULL AUTO_INCREMENT,
    `nome_empresa` VARCHAR(191) NOT NULL,
    `cnpj` VARCHAR(191) NOT NULL,
    `app_Key` VARCHAR(191) NOT NULL,
    `app_Secret` VARCHAR(191) NOT NULL,
    UNIQUE INDEX `Empresa_cnpj_key`(`cnpj`),
    PRIMARY KEY (`id_empresa`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `Usuario` (
    `id_user` INTEGER NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(100) NOT NULL,
    `telefone` VARCHAR(20) DEFAULT NULL,
    `papel` ENUM('administrador','funcionario') NOT NULL,
    `atividade` ENUM('ativo','inativo') NOT NULL,
    `id_empresa` INTEGER NOT NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `usuario_telefone`(`telefone`),
    PRIMARY KEY (`id_user`),
    KEY `fk_admempresa_empresa` (`id_empresa`),
    CONSTRAINT `fk_admempresa_empresa` FOREIGN KEY (`id_empresa`) REFERENCES `Empresa` (`id_empresa`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- CreateTable
CREATE TABLE `User_Admin` (
    `id_user_admin` INTEGER NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(191) NOT NULL,
    `email` VARCHAR(191) NOT NULL,
    `senha` VARCHAR(191) NOT NULL,
    `id_user` INTEGER NOT NULL,
    `id_empresa` INTEGER NOT NULL,
    UNIQUE INDEX `administrador_email`(`email`),
    PRIMARY KEY (`id_user_admin`),
    KEY `fk_user_admin_user` (`id_empresa`),
    CONSTRAINT `fk_user_admin_user` FOREIGN KEY (`id_empresa`) REFERENCES `Empresa` (`id_empresa`) ON DELETE CASCADE
) DEFAULT CHARACTER SET utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Agendamento`(
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `data_solicitacao` DATETIME(3) NOT NULL,
    `proxima_execucao` DATETIME(3),
    `status`  ENUM('ativo', 'inativo') NOT NULL,
    `id_usuario` INTEGER NOT NULL,
    PRIMARY KEY (`id`)
)

-- CreateTable
CREATE TABLE `Solicitacao` (
    `id_solicitacao` INTEGER NOT NULL AUTO_INCREMENT,
    `tipo_solicitacao` ENUM('recibo', 'despesa', 'relatorio') NOT NULL,
    `data_solicitacao` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `status` ENUM('sucesso', 'erro', 'pendente') NOT NULL,
    `id_usuario` INTEGER NOT NULL,
    PRIMARY KEY (`id_solicitacao`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `ConfiguracaoERP` (
    `id_config` INTEGER NOT NULL AUTO_INCREMENT,
    `url_api` VARCHAR(191) NULL,
    `token_api` VARCHAR(191) NULL,
    `status` ENUM('ativo', 'inativo') NOT NULL,
    `id_empresa` INTEGER NOT NULL,
    UNIQUE INDEX `ConfiguracaoERP_id_empresa_key`(`id_empresa`),
    PRIMARY KEY (`id_config`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- AddForeignKey
ALTER TABLE `Solicitacao` ADD CONSTRAINT `Solicitacao_id_usuario_fkey` FOREIGN KEY (`id_usuario`) REFERENCES `Usuario`(`id_user`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `ConfiguracaoERP` ADD CONSTRAINT `ConfiguracaoERP_id_empresa_fkey` FOREIGN KEY (`id_empresa`) REFERENCES `Empresa`(`id_empresa`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `Agendamento` ADD CONSTRAINT `Agendamento_id_usuario_fkey` FOREIGN KEY (`id_usuario`) REFERENCES `Usuario`(`id_user`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `Usuario`
ADD COLUMN `primeiro_contato` ENUM('sim', 'nao') NOT NULL DEFAULT 'nao'
AFTER `atividade`;