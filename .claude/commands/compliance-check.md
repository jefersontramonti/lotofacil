---
description: Roda só a auditoria das 5 regras invioláveis (compliance-auditor), isolada, sobre o diff atual ou um trecho específico. Use antes de mexer em texto de tela, cadastro ou cobrança, mesmo fora do fluxo de /nova-rf.
argument-hint: (opcional) caminho de arquivo ou trecho específico para focar a auditoria
---

Chame o agente `compliance-auditor` sobre: $ARGUMENTS (se vazio, sobre o diff atual da branch contra `main`).

Reporte o resultado bruto do agente — não resuma nem suavize um REPROVADO.
