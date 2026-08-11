---
description: Roda review + gate de compliance sobre o diff atual (branch vs main), sem passar pelo fluxo completo de /nova-rf. Use quando o código já foi escrito fora do fluxo padrão.
---

Rode, nesta ordem, sobre o diff atual da branch (contra `main`):

1. Chame o agente `trevo-reviewer` sobre o diff completo.
2. Chame o agente `compliance-auditor` sobre o mesmo diff, sempre — mesmo que a mudança pareça puramente técnica.
3. Reporte os dois resultados juntos. Se qualquer um reprovar, não sugira abrir PR — liste o que precisa mudar primeiro, sem suavizar.

Não abra PR nem faça push a menos que o usuário peça explicitamente depois de ver os dois resultados.
