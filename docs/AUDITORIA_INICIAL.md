# Auditoria arquitetural inicial

**Versão:** 1.0
**Escopo:** inspeção estática do repositório Android
**Objetivo:** registrar a linha de base técnica para a consolidação do MVP (M2).

> A auditoria não alterou o código nem executou novo build; suas conclusões derivam da inspeção estática do repositório e das validações de release já realizadas.

## Classificação de maturidade

- **M1 — MVP funcional:** alcançado.
- **M2 — Consolidação técnica:** em andamento.
- **M3 — Maturidade e distribuição:** ainda não alcançado.

O projeto já possui separação coerente entre UI, ViewModel, Repository, Room, criptografia e processamento OTP. Também possui persistência local, proteção do segredo em repouso e configuração de Release com R8. Isso não equivale, porém, a um aplicativo pronto para distribuição: testes, cobertura de casos TOTP, tratamento de erros, biometria e hardening ainda precisam evoluir.

## Matriz consolidada

| ID | Área | Evidência observada | Interpretação | Estado | Risco | Prioridade | Ação futura | Critério de aceite |
|---|---|---|---|---|---|---|---|---|
| AUD-GIT-01 | Git | Histórico do projeto disponível e rastreável | Alterações podem ser acompanhadas no repositório | OK | Baixo | P2 | Manter commits organizados | Histórico preservado e alterações identificáveis |
| AUD-GIT-04 | Segurança Git | `keystore.properties` e keystore estão fora do versionamento | Credenciais e chave de assinatura não devem entrar no repositório | OK | Crítico | P0 | Revisar histórico periodicamente | Nenhuma credencial real ou keystore presente no histórico |
| AUD-GIT-05 | Artefatos locais | `.hprof`, `build/`, `.gradle/`, `.kotlin/` e `.vscode/` estão ignorados | Artefatos e configurações locais não fazem parte do código-fonte | OK | Médio | P1 | Manter regras do `.gitignore` | `git status` não lista esses artefatos como não rastreados |
| AUD-ARCH-01 | Arquitetura | Responsabilidades separadas em `data/`, `ui/` e `util/` | A estrutura é adequada ao estágio atual | Adequado | Médio | P1 | Preservar limites entre camadas | Testes e mudanças de camada não exigem acoplamento indevido |
| AUD-SEC-02 | Criptografia | Segredo persistido como ciphertext + IV; AES/GCM com chave no Android Keystore | Há proteção real do segredo em repouso | Adequado | Alto | P0 | Adicionar testes e revisar hardening | Testes confirmam cifragem, decifragem, integridade e comportamento após falhas |
| AUD-SEC-03 | Memória | `decrypt()` transforma o segredo em texto claro dentro de `Account` e `TotpAccountState` | A proteção em repouso não minimiza a exposição em memória | Atenção | Alto | P1 | Avaliar fluxo de geração sem manter segredo no estado da UI | O segredo não integra o estado de apresentação e sua exposição é minimizada |
| AUD-OTP-01 | TOTP | Gerador fixa HMAC-SHA1, 6 dígitos e 30 segundos; parser não preserva parâmetros da URI | O suporte a `otpauth://totp` é parcial | Lacuna | Alto | P1 | Representar e utilizar `algorithm`, `digits` e `period` | URIs com parâmetros válidos geram códigos conforme sua configuração |
| AUD-OTP-02 | Base32 | Caracteres fora de `A-Z2-7` são removidos silenciosamente | Entradas corrompidas podem ser aceitas ou alteradas sem aviso | Lacuna | Alto | P1 | Validar e rejeitar segredo inválido explicitamente | Entradas inválidas produzem erro determinístico e entradas válidas são preservadas |
| AUD-TEST-01 | Testes unitários | Não existe `app/src/test` | Parsing, TOTP, criptografia e regras de domínio não têm cobertura automatizada | Lacuna | Alto | P1 | Criar testes unitários | Testes cobrem casos válidos, inválidos e limites principais |
| AUD-TEST-02 | Testes instrumentados | Não existe `app/src/androidTest` | Persistência e fluxos Android não têm cobertura instrumentada | Lacuna | Médio | P1 | Criar testes instrumentados | Testes validam Room, ciclo de vida e fluxos essenciais no Android |
| AUD-BIO-01 | Biometria | Permissão e dependência existem, mas `BiometricHelper` está vazio e não há fluxo de autenticação | A biometria não está implementada | Não implementado | Médio | P2 | Implementar somente quando houver requisito de desbloqueio definido | Usuário precisa autenticar antes de acessar dados protegidos, com testes de sucesso, recusa e indisponibilidade |
| AUD-UX-01 | QR inválido | O scanner só encaminha valores que começam com `otpauth://`; entradas inválidas são ignoradas sem mensagem | O usuário não sabe por que a leitura falhou | Lacuna | Médio | P2 | Exibir erro e permitir nova tentativa | QR inválido gera mensagem clara sem salvar conta |
| AUD-UX-02 | Câmera | Falhas de inicialização usam `printStackTrace()` e não alteram o estado da tela | Erros de câmera não têm feedback visual adequado | Lacuna | Médio | P2 | Modelar estado de erro da câmera | Falha de permissão ou inicialização mostra estado acionável ao usuário |
| AUD-QR-01 | Ciclo de vida do scanner | `onDispose` encerra o executor, mas não libera explicitamente scanner e provider | Recursos da câmera/analisador podem permanecer ativos além do necessário | A melhorar | Médio | P2 | Desvincular câmera, limpar analisador e fechar scanner | Navegar para fora da tela libera todos os recursos sem vazamento |
| AUD-ARCH-02 | Composição | `AppDatabase`, `AccountRepository` e factory são criados dentro de `AuthenticatorContent()` | Acoplamento aceitável para o MVP, mas a UI conhece infraestrutura | Aceitável no MVP | Médio | P2 | Extrair composição de dependências quando a cobertura permitir | Dependências são fornecidas por uma composição explícita e testável |
| AUD-ARCH-03 | Atualização OTP | ViewModel recalcula estados em loop de um segundo | Comportamento funcional, porém refinável | Funcional / refinável | Baixo | P2 | Avaliar atualização alinhada ao ciclo do relógio TOTP | Atualizações ocorrem no instante necessário sem trabalho redundante |
| AUD-PLAT-01 | Manifesto | `android.hardware.camera.any` está declarado sem `required="false"` | Dispositivos sem câmera podem ser impedidos de instalar o app | Decisão a revisar | Médio | P2 | Decidir se câmera é requisito obrigatório | Manifesto e política de distribuição refletem a decisão documentada |
| AUD-REL-01 | Release | APK Release assinado foi gerado; R8 e redução de recursos estão configurados | A cadeia de build de Release foi validada | Validado | Alto | P0 | Repetir validações em processo reproduzível | APK é gerado, assinado e verificado em ambiente documentado |

## Evidências principais

### Segurança e persistência

- `AccountEntity` persiste `encryptedSecret` e `iv`, não o segredo em texto puro.
- `AndroidKeystoreManager` usa `AES/GCM/NoPadding`, chave AES de 256 bits e Android Keystore.
- O repositório descriptografa o segredo para gerar o código TOTP; essa exposição em memória está registrada em `AUD-SEC-03`.

### TOTP e QR Code

- `OtpAuthParser` valida o esquema `otpauth`, o host `totp`, o label e o segredo.
- `TotpGenerator` usa HMAC-SHA1, 6 dígitos e período de 30 segundos.
- `algorithm`, `digits` e `period` da URI ainda não fazem parte do domínio.
- O decoder Base32 remove caracteres não permitidos em vez de rejeitá-los.

### Interface e permissões

- O scanner solicita a permissão de câmera em tempo de execução.
- QR Codes inválidos não geram feedback visual.
- A exclusão de uma conta passa por `AlertDialog` e só chama `viewModel.deleteAccount(id)` após confirmação.
- A biometria não possui implementação funcional, apesar da permissão e dependência declaradas.

### Organização do pacote

Os arquivos estão fisicamente em `app/src/main/java/com/exemplo/authenticator`, enquanto as declarações Kotlin, o `namespace` e o `applicationId` usam `com.jhonmaxdata.authenticator`. O build pode funcionar com essa situação, mas o alinhamento físico deve ser avaliado separadamente.

## Ordem recomendada para o próximo ciclo

1. Criar testes TOTP para geração, parsing e casos-limite.
2. Incorporar `algorithm`, `digits` e `period` ao parser, domínio e gerador.
3. Tornar a validação Base32 explícita.
4. Criar testes de persistência e criptografia.
5. Melhorar os estados de erro do QR Code e da câmera.
6. Avaliar a exposição do segredo em memória.
7. Implementar biometria somente com requisito de desbloqueio definido.
8. Extrair a composição de dependências.
9. Corrigir o descarte explícito dos recursos do scanner.
10. Retomar requisitos de distribuição após essas validações.

## Conclusão

A linha de base confirma um MVP funcional com arquitetura coerente e proteção criptográfica do segredo em repouso. A consolidação técnica ainda depende principalmente de testes, conformidade completa com URIs TOTP, validação rigorosa de entradas, tratamento de falhas e decisão sobre biometria.

A distinção operacional permanece:

> **Release build assinado não é o mesmo que aplicativo pronto para distribuição.**
