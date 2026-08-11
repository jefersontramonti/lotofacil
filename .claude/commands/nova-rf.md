---
description: Inicia o fluxo completo de implementação de um requisito do Trevo (RF ou RNF), do plano ao PR.
argument-hint: <RF-ID ou descrição, ex: RF-04.5>
---

Requisito: $ARGUMENTS

Execute o fluxo completo abaixo, nesta ordem, criando/atualizando `.claude/handoff.md` a cada etapa com: requisito, módulos/arquivos afetados, status da etapa, e o que a próxima etapa precisa saber.

1. **Branch.** Confirme com o usuário (ou crie, se já autorizado) uma branch no formato `rf-<id-em-minúsculo>-<slug-curto>` a partir de `main`, seguindo CLAUDE.md seção 9.
2. **Planejar** — chame o agente `trevo-architect` com o requisito. Ele produz plano e critério de aceite. Grave em `.claude/handoff.md`.
3. **Testar primeiro** — chame o agente `test-engineer` com o plano. Ele escreve os testes que ainda vão falhar. Confirme que os testes falham (por ausência de implementação, não por engano) antes de seguir.
4. **Implementar** — chame o agente `trevo-developer` com o plano e os testes. Ele implementa até `./gradlew test lint ktlintCheck` passar.
5. **Revisar** — chame o agente `trevo-reviewer` com o diff completo. Se reprovado, volte ao passo 4 com os achados; não pule para o compliance sem o reviewer aprovado.
6. **Gate de compliance** — chame o agente `compliance-auditor`, sempre, mesmo se o requisito parecer não tocar as regras invioláveis. Se REPROVADO, volte ao passo 4. Sem exceção — este passo nunca é pulado.
7. **Atualizar PROJECT_STATE.md** — marque o(s) RF/RNF envolvido(s) como concluído (ou em andamento, se a tarefa só cobrir parte do escopo), com a branch/PR.
8. **PR** — com reviewer e compliance-auditor aprovados, abra o PR com `gh pr create`, título referenciando o ID do requisito (ex: `RF-04.5 · editar dezenas do volante`), corpo com o resumo do plano e do que foi testado. Peça confirmação ao usuário antes de abrir, a menos que ele já tenha autorizado PRs automáticos para esta sessão.

Se qualquer etapa achar que o requisito não existe no catálogo de `Docs/Trevo - Requisitos.dc.html`, pare e pergunte antes de inventar escopo. Se o diff tocar mais de dez arquivos, avise e proponha dividir antes de seguir (CLAUDE.md seção 9).
