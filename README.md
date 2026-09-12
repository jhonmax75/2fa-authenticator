# 2FA Authenticator

Aplicativo Android para gerenciamento local de códigos de autenticação em dois fatores (2FA/TOTP), desenvolvido em Kotlin com foco em segurança, organização arquitetural e processamento local dos dados.

> **Status:** MVP funcional com build de Release assinado. O projeto continua em desenvolvimento e passa por uma etapa de consolidação técnica, testes e revisão de segurança.

## Sobre o projeto

O **2FA Authenticator** gerencia credenciais de autenticação baseadas em TOTP (*Time-based One-Time Password*). A proposta é oferecer uma solução local para:

- armazenar contas de autenticação;
- proteger dados sensíveis localmente;
- gerar códigos temporários;
- organizar credenciais em uma interface Android;
- configurar contas por QR Code;
- separar interface, domínio e persistência;
- produzir uma versão Release otimizada e assinada.

O projeto também funciona como estudo prático de desenvolvimento Android, arquitetura de software, criptografia aplicada, persistência local, automação de build e engenharia de release.

## Estado atual

O projeto possui uma cadeia de build de Release funcional, incluindo:

- compilação Kotlin;
- processamento de recursos;
- execução do R8;
- redução de recursos;
- geração de APK Release;
- assinatura digital configurada;
- configuração de assinatura separada do código-fonte;
- configuração de memória do Gradle para o processamento do R8.

O artefato gerado encontra-se em:

```text
app/build/outputs/apk/release/app-release.apk
```

Versão atual:

```text
versionName = 1.0.0
versionCode = 1
```

A geração do APK não representa, por si só, validação para distribuição. Ainda são necessárias as validações descritas em [Limitações e trabalho em andamento](#limitações-e-trabalho-em-andamento).

## Validação externa do TOTP

O núcleo TOTP foi validado com uma configuração de produção da Binance. A configuração TOTP exportada foi reconstruída localmente como uma URI `otpauth://`, sem registrar o segredo em texto na documentação, convertida em QR Code e importada pelo aplicativo. O código de 6 dígitos gerado pelo aplicativo foi aceito pela Binance.

Essa validação confirma o fluxo de parsing, geração do código e compatibilidade com a credencial utilizada. Ela não valida a captura do QR Code original apresentado pela Binance nem representa compatibilidade completa com todos os parâmetros possíveis de `otpauth://`.

## Tecnologias

- Kotlin
- Android
- Jetpack Compose
- Gradle e Android Gradle Plugin
- Kotlin KSP
- R8
- AndroidX
- Java 17
- TOTP / 2FA
- QR Code
- Room para persistência local
- Android Keystore e criptografia aplicada

## Arquitetura

O código-fonte é organizado por responsabilidades:

```text
app/
+-- src/
    +-- main/
        +-- java/
            +-- com/exemplo/authenticator/
                +-- data/
                |   +-- crypto/
                |   +-- local/
                |   +-- otp/
                |   +-- repository/
                +-- ui/
                |   +-- components/
                |   +-- screens/
                |   +-- theme/
                +-- util/
```

Embora o caminho físico atual use `com/exemplo/authenticator`, as declarações de pacote Kotlin, o `namespace` e o `applicationId` usam `com.jhonmaxdata.authenticator`. Essa diferença de diretório deve ser organizada em uma etapa futura para manter a estrutura física alinhada ao pacote declarado.

### Camadas principais

#### `data/`

Responsável pelo acesso e tratamento dos dados:

- `crypto/` — proteção criptográfica e geração dos códigos;
- `local/` — persistência local com Room;
- `otp/` — leitura e processamento de dados OTP;
- `repository/` — abstração do acesso aos dados.

#### `ui/`

Responsável pela interface do aplicativo:

- `components/` — componentes reutilizáveis;
- `screens/` — telas da aplicação;
- `theme/` — configuração visual e tema.

A separação deve ser preservada e refinada conforme o projeto evoluir.

## Segurança

Como o aplicativo trabalha com dados utilizados para autenticação, segurança é um requisito estrutural. Entre os pontos considerados estão:

- armazenamento local;
- proteção de dados sensíveis;
- separação entre dados e interface;
- uso do Android Keystore;
- configuração externa das credenciais de assinatura;
- assinatura do APK de Release;
- redução e otimização do código no build de produção;
- validação do artefato final.

### Assinatura do Release

As credenciais de assinatura não devem ser armazenadas diretamente no código-fonte. A configuração local utiliza `keystore.properties`, baseada no arquivo [`keystore.properties.example`](keystore.properties.example).

O arquivo contendo as credenciais reais e o keystore devem permanecer fora do controle de versão. As regras correspondentes estão em [`.gitignore`](.gitignore).

## Configuração do ambiente

### Requisitos

- Android SDK;
- Java 17;
- Gradle Wrapper;
- ambiente Android configurado;
- dispositivo físico ou emulador para execução.

O projeto utiliza o Gradle Wrapper. Os builds devem ser executados preferencialmente com `gradlew.bat`.

## Executando o projeto

### Build Debug

```powershell
.\gradlew.bat assembleDebug
```

### Build Release

Para gerar um Release assinado, configure primeiro o arquivo local `keystore.properties` conforme o exemplo:

```text
KEYSTORE_FILE=my-release-key.jks
KEYSTORE_PASSWORD=<senha-do-keystore>
KEY_ALIAS=my-key
KEY_PASSWORD=<senha-da-chave>
```

Depois execute:

```powershell
.\gradlew.bat assembleRelease --no-daemon --max-workers=2
```

O APK será produzido em:

```text
app/build/outputs/apk/release/app-release.apk
```

## Build de Release

O Release utiliza otimização e redução de código:

```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
}
```

O processamento pelo R8 utiliza a configuração de memória definida em `gradle.properties`, atualmente com limite de heap de 2048 MB e metaspace de 512 MB.

## Arquivos gerados e configuração local

Diretórios como `build/`, `.gradle/` e `.kotlin/`, além de configurações locais do VS Code, não fazem parte do código-fonte versionado. Dumps de memória JVM (`*.hprof`) também são ignorados.

## Limitações e trabalho em andamento

O fato de o APK Release ser gerado com sucesso não significa que o projeto esteja concluído. As próximas etapas devem priorizar:

- testes automatizados;
- validação dos fluxos TOTP;
- revisão da criptografia;
- revisão do armazenamento local;
- tratamento de erros;
- testes de recuperação;
- testes de importação por QR Code;
- validação em dispositivos reais;
- revisão da arquitetura;
- análise de dependências;
- documentação técnica;
- validação da assinatura do APK;
- preparação para distribuição.

## Roadmap

### Fase 1 — MVP funcional

- Estrutura Android;
- build Debug;
- build Release;
- configuração de assinatura;
- APK Release gerado;
- R8 funcionando;
- redução de recursos habilitada.

### Fase 2 — Consolidação técnica

- testes unitários;
- testes instrumentados;
- testes TOTP;
- testes de persistência;
- revisão criptográfica;
- revisão do armazenamento;
- validação do APK assinado;
- tratamento sistemático de erros;
- revisão arquitetural.

### Fase 3 — Preparação para distribuição

- política de privacidade;
- documentação de segurança;
- testes em dispositivos reais;
- controle de versões;
- processo de release reproduzível;
- CI/CD;
- preparação para distribuição.

## Princípio de desenvolvimento

O desenvolvimento segue uma abordagem incremental:

```text
validar
   |
isolar
   |
corrigir
   |
testar
   |
construir
   |
verificar
   |
documentar
```

A geração de um artefato de Release é apenas uma etapa. A qualidade do software também depende da rastreabilidade das decisões, da validação dos componentes e da segurança do ciclo de desenvolvimento.

## Licença

A licença do projeto ainda deverá ser definida.
