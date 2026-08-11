---
name: room-migrations
description: Regras para qualquer mudança de esquema Room no Trevo — migração versionada, nunca perda de dado do usuário. Use antes de alterar qualquer @Entity, @Dao ou versão de banco em :core:data.
---

# Migrações Room do Trevo

Fonte: CLAUDE.md seções 8 e 9, RNF-06.6.

## Regra fixa

Toda migração de esquema é versionada explicitamente (`Migration(from, to)`), nunca `fallbackToDestructiveMigration()` em build de produção. Nenhuma migração pode destruir dado do usuário entre versões — palpites salvos, dezenas fixas e histórico de conferência sobrevivem a todo upgrade.

## O que isso implica na prática

- Toda alteração em `@Entity` (novo campo, campo removido, tipo mudado, nova tabela) exige uma `Migration` correspondente no mesmo PR, testada.
- Campo novo não-nulo em tabela existente precisa de `DEFAULT` explícito na migração — nunca assuma que o Room preenche sozinho.
- Alterar o esquema do Room **sem perguntar antes ao usuário está fora de escopo** para o `trevo-developer` (CLAUDE.md seção 9) — isso deveria já estar explícito no plano do `trevo-architect`; se não estiver, é motivo para parar, não para decidir sozinho.

## Teste obrigatório

Toda migração nova tem teste de migração (Room fornece `MigrationTestHelper`) que cria o banco na versão antiga, popula dado de exemplo, roda a migração, e confirma que o dado sobrevive — não só que o schema bate. Esse teste é responsabilidade do `test-engineer`, escrito antes da migração existir.
