# Atividade_09-10

## Configuração local

As configurações da aplicação são carregadas de variáveis de ambiente ou do arquivo `.env` na raiz do projeto. O `.env` é ignorado pelo Git; use `.env.example` como base:

```powershell
Copy-Item .env.example .env
```

Configure `MONGODB_URI` com a URI de conexão do MongoDB desejado. Por padrão, a aplicação usa `mongodb://localhost:27017/pizzaria`.

Para iniciar a aplicação no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```