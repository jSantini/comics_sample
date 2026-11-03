# Fase 1 — evidências de validação

Data: 20/10/2026. Branch: `modernization/phase-one`.
Base: `1f3ab8a144b1f3e68ebb381bae8b8b1a38ffcb34`.
Registro de validação da Fase 1 para publicação na branch dedicada.
Nenhum merge ou reescrita de histórico faz parte desta entrega.
Revisão e aceitação humanas continuam pendentes.

## Ambiente e comandos

JDK Temurin 17.0.18, SDK Android 34, Build Tools 34.0.0, Gradle 8.2,
AGP 8.2.2 e Kotlin 1.9.0. Ferramentas previamente instaladas neste ambiente.
Diretório de trabalho: raiz do checkout, com JDK 17 e SDK 34 configurados.

```bash
./gradlew --no-daemon --max-workers=2 clean :app:assembleDebug \
  testDebugUnitTest :domain:test \
  :app:lintDebug :data:lintDebug :component:lintDebug
```

Resultado final: `BUILD SUCCESSFUL in 2m 11s`, 144 tarefas executadas.
APK final: `app/build/outputs/apk/debug/app-debug.apk`, configurado em modo demo.
Reconstrução limpa para excluir resultados e classes obsoletos dos testes removidos.
Logs completos são artefatos locais de validação e não são versionados.

## Testes JVM

Os XMLs da reconstrução final foram lidos e suas contagens verificadas:

| Suíte | Executados | Falhas/erros/ignorados |
| --- | ---: | ---: |
| ComicsMappingTest | 6 | 0/0/0 |
| ComicsRepositoryTest | 6 | 0/0/0 |
| ComicsHttpTest | 7 | 0/0/0 |
| ComicsViewModelTest | 6 | 0/0/0 |
| Total | 25 | 0/0/0 |

`domain:test` e `component:testDebugUnitTest` retornam `NO-SOURCE`; isso não foi
contado como teste executado. Os modelos/contratos de domínio são exercitados pelas
suítes de dados e apresentação. Não foi medida cobertura percentual.

Cobertura comportamental desta fase:

- Mapeamento, campos obrigatórios/opcionais, IDs duplicados e URLs de capa inseguras.
- Demo fictício estável sem fonte de rede; sucesso remoto e falhas offline/timeout.
- Preservação da identidade de CancellationException e cancelamento da fonte ativa.
- Retrofit real contra MockWebServer: caminho sem autenticação do provedor,
  envelope válido, vazio, HTTP 401/429/503 com corpo HTML, JSON truncado,
  campo ausente, HTTP 204, JSON null e cancelamento em voo.
- ViewModel: loading/conteúdo, vazio, falha inesperada, erro/retry/loading/sucesso,
  supressão de cargas simultâneas e cancelamento ao limpar o ViewModelStore.

## Lint

`app`: 15 warnings, zero erros. `data` e `component`: zero issues.
Avisos restantes: 7 `UnusedAttribute` no ícone legado para API 21,
7 `UnusedResources` de cores legadas e 1 `RedundantLabel` no manifest.
Não foi criado baseline nem desabilitada regra para obter sucesso.
O aviso anterior `ObsoleteLintCustomCheck` não apareceu na reconstrução final após
a limpeza das dependências Material/Navigation. Isso não constitui validação visual.

Relatórios: `app/build/reports/lint-results-debug.html` e os XMLs equivalentes dos módulos.

## Integração remota e guardas de configuração

```bash
./gradlew --no-daemon --max-workers=2 :app:assembleDebug \
  -PCOMICS_BASE_URL=https://catalog.example.invalid/api/
```

Resultado: `BUILD SUCCESSFUL in 45s`. O endereço é fictício e não operacional;
este comando valida a configuração/compilação, não a disponibilidade de um serviço.
O APK final foi reconstruído sem essa propriedade, retornando ao modo demo.
Nenhuma conexão com o gateway fictício foi necessária para compilar.

Três execuções de `./gradlew --no-daemon --max-workers=2 help -PCOMICS_BASE_URL=...`
rejeitaram, com exit 1 e a mensagem específica da validação:

- URL HTTP.
- URL HTTPS com userinfo (usuário/senha fictícios de teste).
- URL HTTPS com query fictícia.

Essas rejeições são o resultado esperado das guardas, não falhas de ambiente.
Usuário/senha e token usados nessas guardas são fixtures fictícias.

## Verificações direcionadas de segurança

`git diff --check`: exit 0.

Foi executada uma verificação direcionada que lê os três valores antigos somente
em memória a partir da base Git, sem imprimi-los, e procura correspondências em
arquivos atuais e em todas as entradas descompactadas do APK. Nenhum encontrado.
Também verifica ausência de logging de produção/printStackTrace, cleartext desabilitado
e BuildConfig final selecionando o demo. A inspeção do manifest empacotado com
`aapt2 dump xmltree ... --file AndroidManifest.xml` confirmou `usesCleartextTraffic=false`.

O diff completo, incluindo arquivos novos e removidos, foi revisado. APKs, logs,
relatórios gerados, arquivos de ambiente e configurações locais não integram o commit.
Nenhum AGENTS.md ou estrutura de agentes foi adicionado.

Essas verificações são direcionadas: não substituem análise de vulnerabilidades de
dependências, revisão humana ou avaliação de segurança do backend. Valores antigos
continuam no histórico; revogação/rotação depende do proprietário.

## Falhas intermediárias corrigidas

- Script Gradle: `java` era resolvido como extensão; import explícito de `java.net.URI`.
- JSON truncado: EOFException estava categorizada como rede; agora é dado inválido.
- Corpo ausente: retorno suspend nullable do Retrofit 2.9 gerava NPE; a API agora usa
  Response de transporte e o repositório valida status/corpo explicitamente.

As asserções foram preservadas e passaram após as correções. Nenhum teste foi ignorado
para esconder esses problemas. Os testes aritméticos legados foram removidos e substituídos
por testes comportamentais.

## Limitações

Não executados: aplicativo em dispositivo/emulador, testes instrumentados/Compose,
integração externa real, build release e verificação de dependências por base de CVEs.
Não há emulador/imagem configurados ou `/dev/kvm` neste ambiente inspecionado.
O warning do SDK XML (AGP antigo versus ferramentas SDK recentes) e a nota de API
obsoleta no código gerado por Hilt não impediram build, testes ou lint.

O modo remoto pressupõe um gateway público HTTPS mantido separadamente; não implementa
backend, login ou armazenamento de segredos. Não houve ampliação para persistência,
paginação, navegação, CI, novas funcionalidades ou atualização ampla da toolchain.
A entrega não declara prontidão para produção ou aprovação humana.

## Revalidação antes da publicação para revisão humana

Em 20/10/2026, o comando completo de clean, assembleDebug, testes JVM e lint acima
foi executado novamente: `BUILD SUCCESSFUL in 1m 44s`, 144 tarefas, sendo
136 executadas e 8 atualizadas. APK de debug reconstruído em modo demo.

- `ComicsViewModelTest`: 6 testes, zero falhas/erros/ignorados.
- `ComicsHttpTest`: 7 testes, zero falhas/erros/ignorados.
- `ComicsMappingTest`: 6 testes, zero falhas/erros/ignorados.
- `ComicsRepositoryTest`: 6 testes, zero falhas/erros/ignorados.

Lint: `app`: 15 warnings; `data`: 0 warnings; `component`: 0 warnings; zero erros.
`domain:test` e `component:testDebugUnitTest` permanecem NO-SOURCE.

A revisão completa do diff incluiu arquivos novos, modificados e removidos. Não foram
incluídos APKs, logs, caches, relatórios de build, keystores, configurações de máquina,
arquivos .env ou links simbólicos. Referências documentais a caminhos temporários da
máquina foram retiradas. Nenhum AGENTS.md ou estrutura de agentes foi criado.

Além da comparação com os valores de autenticação anteriores e da inspeção do APK,
foi realizada pesquisa estática de padrões comuns de chaves privadas, tokens GitHub,
chaves AWS/Google, JWTs e chaves de API. Nenhum segredo real foi identificado nos arquivos
atuais inspecionados. Strings user/password/token nos testes são fixtures fictícias.
`git diff --check` também terminou com exit 0. Essas buscas não são uma certificação
exaustiva de segurança e não removem valores do histórico existente.

A autoria Git configurada é preservada; a assistência do Codex é identificada nos
metadados da mensagem do commit. A publicação na branch dedicada destina-se à revisão
humana, sem merge na branch principal e sem declaração de aprovação ou produção.
