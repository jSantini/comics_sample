# Ajustes após revisão humana — Fase 1

Base: `a186a355234415e06ca5a842aa5efd7118e0d298`.
Branch: `modernization/phase-one`. Data: 20/10/2026.
Hardening preparado em commit separado para publicação somente nesta branch, sem merge.
Os metadados e a atribuição do commit base permanecem intactos; a assistência do Codex
é identificada na mensagem do novo commit. O código continua aguardando revisão humana.
Este registro não declara aprovação.

## Literal de COMICS_BASE_URL

A concatenação manual de aspas foi substituída por `JsonOutput.toJson`, disponível
no ambiente Groovy do Gradle, sem buildSrc, plugin próprio ou dependência adicional.
Para a URI validada, o escaping JSON produz um literal Java compatível. O valor não
é interpolado como código. O campo público `BuildConfig.COMICS_BASE_URL` continua String.

As regras anteriores permanecem: HTTPS com host, barra final, sem userinfo, query ou
fragmento; ausência/valor vazio seleciona demo. A porta explícita agora deve estar
entre 1 e 65535. Host ASCII ou IPv6; Unicode é aceito no caminho. Caracteres que não
são válidos em uma URI precisam de percent-encoding, em vez de relaxar a validação.

Verificação reproduzível, com JDK 17, SDK configurado e Python 3, a partir da raiz:

```bash
python3 scripts/verify_catalog_url.py
```

Resultado real: exit 0; **7 fixtures válidas e 12 inválidas**. Cada válida executa a
tarefa real `:data:generateDebugBuildConfig`, compila o Java gerado com `javac` e
compara a String em runtime com o valor esperado. Cobertura: demo vazio, HTTPS simples,
porta, IPv6, `$`, apóstrofo, `+`, `;`, `&`, `=`, escapes percent-encoded, Unicode e emoji.
Entradas inválidas incluem HTTP, userinfo/query fictícios, fragmento, ausência de
host/barra final, porta zero/fora do intervalo, aspas, barra invertida, newline e espaço.
Nenhuma API externa é contatada. Classes de verificação ficam em diretório temporário;
o bloco finally regenera a configuração padrão. Neste ambiente, o demo foi restaurado.
Execute sem outro build simultâneo; o script modifica apenas os outputs gerados.

## Política de imagens

A Application implementa a factory singleton padrão do Coil e fornece um cliente
OkHttp exclusivo para imagens. Ele não segue redirects, nem no mesmo host, nem entre
hosts ou esquemas. Um interceptor rejeita HTTP, userinfo, query e fragmento antes do
transporte. Timeouts são explícitos e a confiança TLS de produção permanece padrão.
O cliente não compartilha autenticação ou interceptors com o catálogo.

A validação já existente nos DTOs continua filtrando URLs externas antes da UI e teve
sua cobertura ampliada para file/content/data/javascript e fragmentos. Contratos do
domínio, repositório, DTO e estado de apresentação não mudaram. O demo continua com
três histórias fictícias e capas nulas, sem solicitar imagens ou criar chamadas remotas.

Escolha proporcional: bloquear todos os redirects, sem allowlist, pinning, proxy de
imagens ou nova camada arquitetural. Limites: CDNs com redirects e URLs assinadas por
query não são suportados; a UI usa seu placeholder existente. Não há política de bloqueio
de IPs privados ou reputação dos hosts. A origem confiável do catálogo deve escolher
as URLs apropriadas. A configuração trata o transporte das capas do catálogo e não
remove os outros fetchers locais da biblioteca Coil.

## Build, testes e análise estática

Na raiz, com JDK 17 e SDK Android 34 configurados:

```bash
./gradlew --no-daemon --max-workers=2 clean :app:assembleDebug \
  testDebugUnitTest :domain:test \
  :app:lintDebug :data:lintDebug :component:lintDebug
```

Resultado real: **BUILD SUCCESSFUL in 1m 40s**, 144 tarefas, sendo 136 executadas e
8 atualizadas. APK debug reconstruído com `COMICS_BASE_URL = ""` (demo).

| Suíte JVM | Testes | Falhas/erros/ignorados |
| --- | ---: | ---: |
| ComicsViewModelTest | 6 | 0/0/0 |
| ComicImageClientTest | 7 | 0/0/0 |
| ComicsHttpTest | 7 | 0/0/0 |
| ComicsMappingTest | 6 | 0/0/0 |
| ComicsRepositoryTest | 6 | 0/0/0 |
| Total | 32 | 0/0/0 |

Os 7 novos testes usam o cliente de produção contra MockWebServer em TLS loopback:
HTTPS direto; rejeição de HTTP; rejeição de userinfo/query/fragmento; redirect HTTPS
no mesmo host; redirect para outro host; downgrade HTTPS→HTTP; certificado não confiável.
A CA confiável é criada em memória somente no teste, sem chaves privadas versionadas.
Os testes não relaxam a configuração TLS da aplicação.

Lint: zero erros; `app` com 15 warnings legados (7 UnusedAttribute, 7 UnusedResources,
1 RedundantLabel), `data` e `component` sem issues. `domain:test` e os testes de
`component` continuam NO-SOURCE; não são contados como testes executados.
`git diff --check` também passou, incluindo os arquivos novos no artefato de diff.

Não executados: dispositivo/emulador, composição/decoding Coil em Android, testes
instrumentados, integração externa real ou build release. Os testes TLS verificam
o transporte real, não renderização ou conteúdo de uma imagem decodificada.
Nenhuma funcionalidade do Dia 2, AGENTS.md ou estrutura de agentes foi adicionada.
