package com.example.js_comics.data.repository

import com.example.js_comics.domain.CatalogSource
import com.example.js_comics.domain.Comic
import com.example.js_comics.domain.ComicsCatalog
import com.example.js_comics.domain.ComicsRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Fictional, embedded catalog: no network, credentials or image downloads. */
class DemoComicsRepository : ComicsRepository {
    override suspend fun getComics(): ComicsCatalog {
        currentCoroutineContext().ensureActive()
        return ComicsCatalog(
            listOf(
                Comic(
                    id = 1,
                    title = "Aurora: O Primeiro Sinal",
                    description = "Em 2187, a comandante Aurora Voss lidera uma missão de reconhecimento além de Netuno. Ao interceptar uma transmissão impossível de decifrar, sua tripulação descobre coordenadas de uma estação desaparecida há mais de cem anos. Agora, Aurora precisa escolher entre obedecer às ordens de retorno ou seguir um sinal que parece conhecer seu passado.",
                    imageUrl = null,
                    series = "Aurora",
                    issueNumber = 1,
                    genre = "Ficção científica",
                    publicationYear = 2026,
                    coverKey = "aurora",
                    shortSynopsis = "Quando um sinal desconhecido surge nas fronteiras do sistema solar, a capitã Aurora descobre uma mensagem que pode mudar o destino da humanidade.",
                ),
                Comic(
                    id = 2,
                    title = "Cidade de Papel: O Arquiteto Invisível",
                    description = "Na cidade de Origami, edifícios surgem e desaparecem a cada amanhecer. Lia e Tomás trabalham como restauradores de mapas antigos, até encontrarem plantas de construções que ainda não existem. As pistas apontam para o lendário Arquiteto Invisível, cujo desaparecimento ameaça desmanchar a própria cidade. Para salvá-la, os dois precisarão descobrir quem controla suas transformações.",
                    imageUrl = null,
                    series = "Cidade de Papel",
                    issueNumber = 3,
                    genre = "Fantasia urbana",
                    publicationYear = 2026,
                    coverKey = "cidade_papel",
                    shortSynopsis = "Em uma metrópole que muda de forma todas as manhãs, dois jovens inventores investigam o desaparecimento do homem que desenhou suas ruas.",
                ),
                Comic(
                    id = 3,
                    title = "Além da Órbita: Retorno a Casa",
                    description = "A nave Horizonte perde contato com a Terra depois de atravessar uma anomalia gravitacional. Sem combustível suficiente, a tripulação encontra uma instalação abandonada orbitando um planeta desconhecido. Seus registros sugerem que outros humanos estiveram ali antes deles. Entre recursos escassos, conflitos internos e descobertas inesperadas, o grupo precisa decidir em quem confiar para conseguir voltar para casa.",
                    imageUrl = null,
                    series = "Além da Órbita",
                    issueNumber = 2,
                    genre = "Aventura espacial",
                    publicationYear = 2026,
                    coverKey = "alem_orbita",
                    shortSynopsis = "Após uma missão fracassada nos limites do espaço conhecido, cinco tripulantes precisam sobreviver e encontrar o caminho de volta à Terra.",
                ),
                Comic(
                    id = 4,
                    title = "Sentinela Neon: Memórias Roubadas",
                    description = "Na megacidade de Vesper, a corporação Mnemosyne comercializa memórias humanas como entretenimento. Uma hacker conhecida como Sentinela Neon invade seus servidores para investigar o desaparecimento de centenas de cidadãos. Durante a operação, encontra registros que revelam que suas lembranças de infância foram implantadas. Para descobrir quem realmente é, ela precisará enfrentar o sistema que controla toda a cidade.",
                    imageUrl = null,
                    series = "Sentinela Neon",
                    issueNumber = 5,
                    genre = "Cyberpunk",
                    publicationYear = 2026,
                    coverKey = "sentinela_neon",
                    shortSynopsis = "Em uma cidade onde lembranças podem ser compradas e vendidas, uma vigilante descobre que sua própria identidade foi fabricada.",
                ),
                Comic(
                    id = 5,
                    title = "Crônicas do Eclipse: O Último Guardião",
                    description = "Um eclipse inexplicável passa a acontecer diariamente em todo o planeta. Durante a escuridão, criaturas esquecidas retornam e antigas ruínas revelam inscrições luminosas. Cael, um jovem arqueólogo, descobre ser descendente de uma ordem responsável por manter o equilíbrio entre dois mundos. Com a duração dos eclipses aumentando, ele precisa reunir os últimos guardiões antes que o próximo amanhecer nunca chegue.",
                    imageUrl = null,
                    series = "Crônicas do Eclipse",
                    volumeNumber = 1,
                    genre = "Fantasia cósmica",
                    publicationYear = 2026,
                    coverKey = "eclipse",
                    shortSynopsis = "Quando o Sol desaparece durante uma hora todos os dias, antigos guardiões despertam para impedir que a escuridão se torne permanente.",
                ),
                Comic(
                    id = 6,
                    title = "O Vigilante das Sombras: Caso Zero",
                    description = "Na cidade de Grisalva, pessoas começam a desaparecer sem deixar registros de sua existência. Fotografias mudam, documentos somem e até familiares deixam de se lembrar das vítimas. O detetive Elias Corvo, conhecido como Vigilante das Sombras, encontra uma ligação entre os casos e um experimento secreto de manipulação da memória coletiva. Sua investigação o aproxima de uma verdade que também ameaça apagar seu próprio passado.",
                    imageUrl = null,
                    series = "Vigilante das Sombras",
                    issueNumber = 1,
                    genre = "Mistério / Noir",
                    publicationYear = 2026,
                    coverKey = "vigilante",
                    shortSynopsis = "Um investigador mascarado procura a origem de uma sequência de desaparecimentos ligados a uma organização que oficialmente não existe.",
                ),
            ),
            CatalogSource.DEMO,
        )
    }
}
