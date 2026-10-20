# Comics — Android showcase

Aplicativo pequeno de catálogo com Kotlin, Jetpack Compose, Hilt e Retrofit.
O modo padrão é **demo offline**: três histórias fictícias embarcadas, sem credenciais,
requisições ou downloads de capas. A interface identifica esse modo explicitamente.
Não há persistência, paginação ou navegação entre telas nesta fase.

## Executar

Requisitos: JDK 17, Android SDK Platform 34 e Build Tools 34.0.0. O Wrapper usa
Gradle 8.2; AGP 8.2.2 e Kotlin 1.9.0 foram preservados para limitar a migração.
Configure `ANDROID_HOME` ou `local.properties` (ignorado pelo Git).

```bash
./gradlew --no-daemon --max-workers=2 :app:assembleDebug testDebugUnitTest :domain:test
./gradlew --no-daemon --max-workers=2 :app:lintDebug :data:lintDebug :component:lintDebug
```

Instale `app/build/outputs/apk/debug/app-debug.apk` em Android API 21 ou superior.
Execute os comandos na raiz do checkout com JDK e SDK configurados.

Execute apenas um build por vez no mesmo checkout.

## Integração remota opcional

O aplicativo não contém autenticação Marvel, chave privada ou assinatura de provedor.
Para habilitar uma fonte remota, forneça a URL **não secreta** de um gateway público
HTTPS controlado por você:

```bash
./gradlew :app:assembleDebug -PCOMICS_BASE_URL=https://catalog.example.invalid/api/
```

O endereço acima é apenas um exemplo não operacional. Sem a propriedade, o build é demo.
A URL deve terminar com `/` e não pode conter usuário, senha, query ou fragmento.
Não use tokens no caminho nem credenciais em propriedades Gradle: a URL é pública
no APK. Não há autenticação de usuário nesta fase; esse contrato não serve a catálogos privados.

O gateway deve implementar `GET /api/comics` (conforme o prefixo escolhido), guardar
credenciais e assinar chamadas ao provedor exclusivamente no servidor. Não há backend
implementado neste repositório. Resposta esperada:

```json
{
  "data": {
    "results": [
      {
        "id": 1,
        "title": "História fictícia",
        "description": "Exemplo de contrato, não uma resposta de produção",
        "thumbnail": {"path": "https://images.example.invalid/cover", "extension": "jpg"}
      }
    ]
  }
}
```

`data.results` é obrigatório; `[]` representa catálogo vazio. Cada item precisa de
ID positivo e único e título não vazio. Descrição e capa são opcionais. Capas com
HTTP, credenciais, query ou fragmento são descartadas. O manifest bloqueia cleartext;
o cliente do catálogo não segue redirects e usa verificação TLS padrão e timeouts.
Não há interceptor de logging de requisições/respostas.

Antes de usar dados reais, valide a disponibilidade, o contrato e os termos do provedor,
atribuição e licenças de imagens. Limites e proteção de abuso pertencem ao gateway.
A integração externa real não é validada pelos testes locais.

## Arquitetura e decisões

```text
:app ──> :domain <── :data
  ├────> :data       (composição via Hilt)
  └────> :component
```

- `domain`: Kotlin/JVM puro; modelos, contrato de repositório e categorias de falha.
- `data`: DTOs de transporte, validação/mapeamento, fontes demo/remota e composição Hilt.
- `app`: estado de apresentação próprio, ViewModel e Compose. A rota coleta com lifecycle;
  a tela recebe apenas estado e callbacks.
- `component`: componentes visuais compartilhados.

A consulta é pontual: o contrato usa `suspend`, sem um Flow artificial na camada de dados.
O ViewModel expõe `StateFlow` somente leitura. As ações de UI são executadas na Main;
retries durante uma carga ativa são ignorados. Ao terminar ou falhar, outra tentativa
pode começar. O cancelamento não é traduzido para erro de negócio e acompanha o ciclo
de vida do ViewModel. Retrofit realiza I/O assíncrono, sem dispatcher fixo no ViewModel.

Estados: loading, conteúdo, vazio e erro com retry. Mensagens internas e corpos HTTP
não chegam à interface. O catálogo demo não depende da disponibilidade da rede.
Não há caso de uso que apenas repasse uma chamada: extraia-o quando surgir uma regra
ou orquestração que justifique isso. Room, DataStore, Paging e novas telas estão fora
do escopo. A atualização ampla da toolchain também foi adiada.

## Testes e limitações

Os testes JVM utilizam fakes, `kotlinx-coroutines-test` e MockWebServer, cobrindo
mapeamento, dados inválidos, HTTP, falhas de rede, timeout, cancelamento, estados,
retry e supressão de requisições concorrentes. O HTTP local existe somente nos testes;
o app exige HTTPS. Os antigos testes aritméticos foram removidos.

Testes instrumentados legados de contexto ainda não são testes de interface.
Não há validação em emulador/dispositivo, screenshots automatizados ou CI nesta fase. A stack preservada pode reportar incompatibilidade das verificações de lint
Compose; consulte as evidências em `docs/phase-one-validation.md`.

A remoção de credenciais do fonte atual não apaga o histórico Git. O proprietário deve
avaliar revogação/rotação dos valores anteriormente versionados; nenhuma reescrita de
histórico foi realizada. Esta entrega requer revisão humana e não declara prontidão
para produção.
