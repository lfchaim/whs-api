Aqui está um `docker-compose.yml` para rodar PostgreSQL com Adminer:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:latest
    container_name: postgres_db
    restart: always
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: mydatabase
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - db_network

  adminer:
    image: adminer:latest
    container_name: adminer
    restart: always
    ports:
      - "8080:8080"
    networks:
      - db_network
    depends_on:
      - postgres

volumes:
  postgres_data:

networks:
  db_network:
    driver: bridge
```

**Para usar:**

1. Salve o arquivo como `docker-compose.yml`
2. Execute: `docker-compose up -d`
3. Acesse o Adminer em: `http://localhost:8080`

**Credenciais de acesso no Adminer:**
- **Sistema:** PostgreSQL
- **Servidor:** postgres
- **Usuário:** postgres
- **Senha:** postgres
- **Base de dados:** mydatabase

Você pode alterar as variáveis de ambiente conforme necessário.

Para parar os containers, use:

```bash
docker-compose down
```

Este comando para e remove os containers, mas **mantém os volumes** (seus dados do PostgreSQL ficam salvos).

**Outros comandos úteis:**

- `docker-compose stop` - apenas para os containers (não remove)
- `docker-compose start` - reinicia containers parados
- `docker-compose restart` - reinicia os containers
- `docker-compose down -v` - para, remove containers **e apaga os volumes** (⚠️ perde os dados)
- `sudo docker rmi -f $(sudo docker images)` - remove todas as imagens

O erro "permission denied" geralmente ocorre por falta de permissões no Docker. Tente estas soluções:

### 1. Adicione seu usuário ao grupo docker (recomendado):

```bash
sudo usermod -aG docker $USER
```

Depois, **faça logout e login novamente** (ou reinicie) para aplicar as mudanças. Após isso, você poderá rodar sem `sudo`:

```bash
docker-compose up -d
```

### 2. Se precisar usar agora (temporário):

Verifique as permissões do arquivo:

```bash
sudo chmod 644 docker-compose.yml
```

E certifique-se de estar no diretório correto onde está o arquivo `docker-compose.yml`.

### 3. Se o erro persistir:

Verifique se o Docker está rodando:

```bash
sudo systemctl status docker
```

Se não estiver ativo, inicie:

```bash
sudo systemctl start docker
```

Qual dessas situações se aplica ao seu caso?

Se o grupo docker não existe, vamos criá-lo e configurar:

### 1. Crie o grupo docker:

```bash
sudo groupadd docker
```

### 2. Adicione seu usuário ao grupo:

```bash
sudo usermod -aG docker $USER
```

### 3. Reinicie o serviço Docker:

```bash
sudo systemctl restart docker
```

### 4. Aplique as mudanças do grupo:

```bash
newgrp docker
```

Ou faça **logout e login novamente**.

### 5. Teste sem sudo:

```bash
docker-compose up -d
```

---

**Se ainda não funcionar**, verifique se o Docker está instalado corretamente:

```bash
docker --version
docker-compose --version
```

Qual mensagem de erro exata você está recebendo?

# Adapta.org
Perfeito — obrigado por compartilhar o docker-compose.yml. O arquivo está “correto” em linhas gerais, mas o erro Connection refused indica que o cliente (DBeaver) não está nem conseguindo abrir a conexão TCP com o Postgres. Isso normalmente acontece por um destes motivos:

- O container do Postgres não está de pé ou está em crash loop (porta nunca chega a abrir).
- Conflito de porta no host (outro Postgres/serviço usando 5432).
- O Postgres iniciou mas não está escutando em 0.0.0.0:5432 (escutando só em localhost dentro do container).
- Diferença de versão do Postgres com dados antigos no volume (subiu “latest” por engano e o diretório de dados é de outra major), fazendo o serviço cair.
- Firewall/antivírus bloqueando a porta publicada (mais comum no Windows).
- DBeaver apontando para host/porta errados (p. ex., 5432 quando você mudou; SSL ligado sem suporte, etc.).

Abaixo vai um passo-a-passo para diagnosticar rápido e, em seguida, uma versão revisada do seu compose com melhorias que resolvem os casos mais comuns.

## 1) Diagnóstico rápido (3 minutos)

1. Veja o status dos containers:
   - docker compose ps
   - Se o Postgres não estiver “Up”, rode: docker compose logs -f postgres
     - Se aparecer algo como “database files are incompatible with server” ou erros de init, é incompatibilidade de versão com o volume existente (ver seção 3).

2. Verifique se a porta está realmente publicada no host:
   - docker ps | grep postgres_db
   - Deve aparecer algo como 0.0.0.0:5432->5432/tcp ou 0.0.0.0:5433->5432/tcp (dependendo da sua configuração).
   - Se não aparecer, é conflito de porta ou o container não subiu corretamente.

3. Teste a conexão a partir do host (sem DBeaver) para confirmar abertura de porta:
   - nc -vz 127.0.0.1 5432
   - Se der “succeeded”, a porta está aberta. Se der “Connection refused”, nada está escutando no host nessa porta.

4. Teste de dentro do container Adminer (se ele estiver “Up”):
   - Acesse http://localhost:8080
   - Servidor: postgres (o nome do serviço na mesma rede docker)
   - Usuário: postgres | Senha: postgres | Base: mydatabase
   - Se Adminer conectar, então o Postgres está ok na rede Docker; o problema é a publicação da porta ou algo no host/DBeaver.

5. Confirme que o Postgres está escutando “para fora” dentro do container:
   - docker exec -it postgres_db bash -lc "ss -ltnp | grep 5432 || netstat -ltnp | grep 5432"
   - Opcional: docker exec -it postgres_db bash -lc "psql -U postgres -d mydatabase -c \"show listen_addresses;\""
     - Idealmente deve retornar * (ou 0.0.0.0). Se estiver “localhost”, conexões externas via port mapping podem falhar.

6. Se você estiver no Windows:
   - Verifique firewall/antivírus e uso da porta: netstat -ano | find "5432"
   - Se outro processo estiver usando 5432, mude a porta do host (ex.: 5433:5432).

## 2) O que há de potencialmente problemático no seu compose

- image: postgres:latest: “latest” muda com o tempo. Se o volume já existia com dados de uma versão anterior, o Postgres pode falhar ao iniciar (incompatibilidade de major). Isso causa Connection refused porque o serviço cai antes de abrir a porta.
- Falta healthcheck: dificulta saber quando o serviço está realmente pronto (e o Adminer pode tentar antes da hora).
- Port 5432 no host: se você também tem Postgres instalado localmente, pode haver conflito.
- listen_addresses: dependendo da configuração efetiva, o Postgres pode não estar escutando em 0.0.0.0 dentro do container (menos comum na imagem oficial recente, mas vale garantir).

## 3) Compose revisado (robusto e com boas práticas)

Sugestões aplicadas:
- Fixar a versão do Postgres (ex.: 16). Evita “surpresas” do latest.
- Mudar a porta do host para 5433 (evita conflito com um Postgres local).
- Garantir escuta em todas as interfaces.
- Adicionar healthcheck para sinalizar quando o banco está pronto.
- Fazer o Adminer depender do healthcheck (se sua versão do Compose suportar condition: service_healthy).

Cole e teste esta versão:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    container_name: postgres_db
    restart: unless-stopped
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: mydatabase
      # Opcional: defina o timezone se quiser
      # TZ: America/Sao_Paulo
    ports:
      - "5433:5432"   # alterado para evitar conflito no host
    volumes:
      - postgres_data:/var/lib/postgresql/data
    command: ["postgres", "-c", "listen_addresses=*"]  # garante escuta em 0.0.0.0
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d mydatabase"]
      interval: 5s
      timeout: 5s
      retries: 20
    networks:
      - db_network

  adminer:
    image: adminer:latest
    container_name: adminer
    restart: unless-stopped
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy   # se sua versão não suportar, remova e mantenha só 'depends_on: - postgres'
    networks:
      - db_network

volumes:
  postgres_data:

networks:
  db_network:
    driver: bridge
```

Observações importantes:
- Se sua instalação do Docker Compose não aceitar depends_on com condition, troque por:
  depends_on:
    - postgres
- Se o volume postgres_data já existe e foi criado em outra versão major, você pode ver erros no log ao subir. Para testar “do zero” sem perder seus dados atuais, crie um volume novo temporário:
  - volumes:
      - postgres_data_v2:/var/lib/postgresql/data
  Depois você decide se migra dados (pg_dump/pg_restore, pg_upgrade) ou volta o pin da imagem para a versão antiga do seu volume.

## 4) Como configurar o DBeaver (depois do compose revisado)

- Host: 127.0.0.1 (ou localhost)
- Port: 5433
- Database: mydatabase
- Username: postgres
- Password: postgres
- SSL: desabilitado (a menos que você tenha configurado SSL no Postgres)
- Teste a conexão e salve.

Se você mantiver 5432:5432, ajuste a porta no DBeaver para 5432.

## 5) Se ainda der “Connection refused”, confira estes pontos

- Container caindo/reiniciando:
  - docker compose logs -f postgres
  - Erros comuns:
    - “database files are incompatible with server”: volume de versão antiga. Soluções:
      - Fixe a imagem na mesma versão do volume (por exemplo, postgres:14) e suba.
      - Faça migração (pg_dump/pg_restore) para a nova versão.
      - Para teste rápido sem dados: apague o volume atual (cuidado: destrói dados).
        - docker compose down
        - docker volume rm nome_do_projeto_postgres_data
        - docker compose up -d
- Conflito de porta:
  - Linux/macOS: lsof -i :5432 (ou :5433)
  - Windows: netstat -ano | find "5432"
  - Se ocupado, use outra porta do host no compose.
- Firewall/antivírus (Windows):
  - Garanta que o Docker Desktop tem permissão de abrir portas.
  - Teste com 5433 para evitar regras pré-existentes da 5432.
- Escuta dentro do container:
  - docker exec -it postgres_db bash -lc "psql -U postgres -d mydatabase -c \"show listen_addresses;\""
  - Deve retornar *. Se não, mantenha o command: ["postgres", "-c", "listen_addresses=*"] no compose.

## 6) Dica de verificação cruzada (útil)

- Teste do próprio host usando psql:
  - psql -h 127.0.0.1 -p 5433 -U postgres -d mydatabase
- Teste via Adminer:
  - http://localhost:8080
  - Servidor: postgres
  - Usuário: postgres | Senha: postgres | Base: mydatabase
- Teste de um “cliente” dentro da mesma rede Docker:
  - docker run --rm -it --network=$(docker network ls --format '{{.Name}}' | grep db_network) postgres:16-alpine psql -h postgres -U postgres -d mydatabase -c '\l'

## 7) Segurança e boas práticas

- Evite manter POSTGRES_PASSWORD=postgres em ambientes compartilhados/produção.
- Sempre fixe a versão da imagem (ex.: postgres:16) para evitar surpresas em rebuild.
- Faça backup antes de mexer no volume se houver dados importantes.

Se você rodar os comandos de diagnóstico e me colar aqui os trechos principais do docker compose ps e docker compose logs -f postgres (últimas 50–100 linhas), eu te digo exatamente qual dos cenários é o seu e ajusto o compose pontualmente.

# Adapta.org - Postgis
## Compose ajustado para habilitar PostGIS (com boas práticas)

A forma mais simples e robusta é trocar a imagem do Postgres pela imagem oficial do PostGIS e, opcionalmente, incluir um script de inicialização que já cria a extensão no banco definido em POSTGRES_DB na primeira subida.

Abaixo vai um docker-compose.yml pronto, com:
- Imagem PostGIS versionada (evita surpresas do “latest”)
- Healthcheck (útil para orquestrar dependências)
- Volume de dados
- Diretório de inicialização para criar a extensão automaticamente
- Adminer para testar rapidamente a conexão

### docker-compose.yml (proposto)

```yaml
version: "3.8"

services:
  postgres:
    image: postgis/postgis:16-3.4
    container_name: postgres_db
    restart: always
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: mydatabase
    ports:
      - "5432:5432" # altere para "5433:5432" se a 5432 já estiver em uso no host
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./initdb:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d ${POSTGRES_DB:-postgres}"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - db_network

  adminer:
    image: adminer:latest
    container_name: adminer
    restart: always
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    networks:
      - db_network

volumes:
  postgres_data:

networks:
  db_network:
    driver: bridge
```

### Script de inicialização para criar a extensão automaticamente

Crie uma pasta initdb no mesmo diretório do docker-compose.yml e adicione o arquivo 01_enable_postgis.sql com o conteúdo abaixo. Esses scripts rodam apenas na primeira inicialização do banco (quando o volume ainda está vazio).

Caminho do arquivo: ./initdb/01_enable_postgis.sql

```sql
-- Cria as extensões PostGIS na base definida por POSTGRES_DB (mydatabase)
CREATE EXTENSION IF NOT EXISTS postgis;
-- Extensões opcionais:
CREATE EXTENSION IF NOT EXISTS postgis_topology;
-- Em versões que suportam:
-- CREATE EXTENSION IF NOT EXISTS postgis_raster;
```

Observações:
- A imagem postgis/postgis já inclui o PostGIS compilado. O script acima apenas ativa a extensão no banco.
- Por padrão, o entrypoint do contêiner executa os .sql de /docker-entrypoint-initdb.d conectando-se à base POSTGRES_DB (mydatabase), se definida. Assim, a extensão é criada diretamente na base certa.

---

## Como subir (ou recriar) corretamente

1) Se é a primeira vez:
- docker compose up -d

2) Se você já tinha o volume postgres_data criado antes (ou já subiu o container sem o initdb):
- O script não rodará automaticamente. Você tem 2 opções:
  - Opção A (sem perder dados): criar a extensão manualmente
    - docker compose exec -u postgres postgres psql -d mydatabase -c "CREATE EXTENSION IF NOT EXISTS postgis;"
    - docker compose exec -u postgres postgres psql -d mydatabase -c "CREATE EXTENSION IF NOT EXISTS postgis_topology;"
  - Opção B (limpar tudo e deixar o script rodar do zero)
    - docker compose down
    - docker volume rm $(docker volume ls -q | grep postgres_data)  # cuidado: apaga os dados
    - docker compose up -d

3) Testar com Adminer:
- Acesse http://localhost:8080
- System: PostgreSQL
- Server: localhost
- Username: postgres
- Password: postgres
- Database: mydatabase

4) Testar com DBeaver:
- Host: localhost
- Porta: 5432 (ou a que você mapeou, ex.: 5433)
- Database: mydatabase
- User: postgres
- Password: postgres

Se der “Connection refused”:
- Verifique se não há outro Postgres ocupando a porta no host:
  - Linux/macOS: lsof -i :5432 ou netstat -anp | grep 5432
  - Windows (PowerShell): Get-NetTCPConnection -LocalPort 5432
- Se a porta estiver em uso, mude o mapeamento para "5433:5432" no compose e conecte no DBeaver em 5433.
- Confira se o container está saudável:
  - docker compose ps
  - docker compose logs -f postgres (veja erros de init, permissões de volume, etc.)

---

## Como verificar se a extensão está ativa

Você pode checar de algumas formas:

- Via psql:
  - docker compose exec -u postgres postgres psql -d mydatabase -c "\dx"
  - Procure por postgis e postgis_topology na lista de extensões
- Via DBeaver:
  - Abra o editor SQL conectado em mydatabase e rode:
    - SELECT PostGIS_Full_Version();
    - SELECT srid, auth_name, auth_srid, proj4text FROM spatial_ref_sys LIMIT 5;

Se retornar informações sem erro, PostGIS está funcional.

---

## Dicas e boas práticas

- Fixe versões: você já viu acima a imagem postgis/postgis:16-3.4. Evita mudanças inesperadas quando a imagem “latest” for atualizada.
- Senha forte: evite POSTGRES_PASSWORD=postgres em ambientes compartilhados. Para dev local, ok. Em produção, use secrets e rede privada.
- Backup antes de trocar volume: se o volume já tem dados, faça dump (pg_dump/pg_dumpall) antes de removê-lo.
- Portas: se você já tem um Postgres nativo instalado no host, prefira mapear para outra porta externa, ex.: "5433:5432".
- Performance em Windows/WSL2: volumes bind (./initdb) são tranquilos, mas para dados grandes mantenha o data dir no volume nomeado (como está).
- Client_encoding e timezone: se necessário, defina no init script (ex.: ALTER DATABASE mydatabase SET timezone TO 'UTC';).

---

## Recapitulando

- Troque a imagem para postgis/postgis e adicione um init script para criar a extensão.
- Se o volume já existe, crie a extensão manualmente com CREATE EXTENSION dentro do banco.
- Garanta que a porta não está em conflito e que o container está saudável.
- Teste com Adminer e DBeaver.

Se quiser, posso adaptar o compose para múltiplos bancos com PostGIS, adicionar usuário/role separado de postgres, ou incluir scripts para schemas e permissões. É só dizer como você quer organizar o ambiente.
