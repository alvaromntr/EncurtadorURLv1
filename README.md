# 🔗 URL Cutter

Um encurtador de URLs full-stack com autenticação 2FA, analytics de cliques, geração de QR Code e painel de histórico.

---

## 📋 Sumário

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Integrações](#integrações)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do Banco de Dados](#configuração-do-banco-de-dados)
- [Como Executar](#como-executar)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Endpoints da API](#endpoints-da-api)

---

## Visão Geral

O URL Cutter permite que usuários autenticados encurtem URLs longas, acompanhem cliques em tempo real, visualizem analytics em gráficos e gerem QR Codes para cada link encurtado. A autenticação utiliza JWT com verificação de dois fatores (OTP) enviado por e-mail.

---

## Arquitetura

```
┌──────────────────────────────────────────────┐
│              Usuário (Navegador)              │
└─────────────────────┬────────────────────────┘
                      │ HTTP/HTTPS
┌─────────────────────▼────────────────────────┐
│           Frontend — React.js                │
│  Zustand · Tailwind CSS · DaisyUI · Recharts │
│              (Vite — porta 5173)             │
└─────────────────────┬────────────────────────┘
                      │ REST (Axios)
┌─────────────────────▼────────────────────────┐
│          Backend — Spring Boot (Java)         │
│  Spring WebFlux · Spring Security · JWT · R2DBC│
│              (porta 8081)                    │
└──────────┬──────────────────────┬────────────┘
           │                      │
┌──────────▼──────────┐  ┌────────▼───────────┐
│  PostgreSQL (R2DBC) │  │  Resend (E-mail API)│
│   (porta 5432)      │  │  Envio de OTP       │
└─────────────────────┘  └────────────────────┘
```

O backend é reativo de ponta a ponta (Spring WebFlux + R2DBC), sem bloqueio de threads. O frontend consome a API via Axios com interceptores que injetam automaticamente o token JWT nas requisições.

---

## Funcionalidades

### Autenticação
- Cadastro de usuário com e-mail e senha
- Login com verificação de dois fatores (OTP de 6 dígitos via e-mail)
- Tokens JWT com expiração de 10 horas
- Alteração de senha autenticada
- Exclusão de conta com remoção em cascata de roles e OTPs
- Conformidade com LGPD: modal de consentimento no cadastro

### Encurtamento de URLs
- Geração de código curto via algoritmo Snowflake ID + Base62
- Associação de cada URL ao usuário criador
- Reutilização de URL: se a URL original já existe, retorna o mesmo link curto
- Redirecionamento automático via `GET /r/{shortCode}` com status 302
- Expiração visual calculada no frontend (7 dias a partir da criação)
- Exclusão de URLs próprias

### Analytics
- Registro de clique a cada redirecionamento (IP, User-Agent, Referer, timestamp)
- Contagem de cliques em tempo real (polling a cada 3 segundos)
- Página de Analytics com seletor de URL e gráfico de linha (cliques por dia)
- Endpoint de analytics agregado por data (`GET /api/clicks/url/{urlId}/analytics`)

### QR Code
- Geração de QR Code para qualquer URL encurtada
- Exibido em modal com opção de download em PNG
- Dimensões: 300×300 pixels

---

## Tecnologias

### Backend
| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.11 | Framework base |
| Spring WebFlux | — | Programação reativa |
| Spring Security | — | Autenticação e autorização |
| Spring Data R2DBC | — | Acesso reativo ao banco |
| PostgreSQL | — | Banco de dados relacional |
| JJWT | 0.12.5 | Geração e validação de JWT |
| ZXing | 3.5.3 | Geração de QR Code |
| Lombok | — | Redução de boilerplate |

### Frontend
| Tecnologia | Versão | Uso |
|---|---|---|
| React | 19 | UI declarativa |
| Vite | 8 | Build e dev server |
| Zustand | 5 | Gerenciamento de estado |
| Tailwind CSS | 4 | Estilização utilitária |
| DaisyUI | 5 | Componentes de UI (tema `forest`) |
| Recharts | 3 | Gráficos |
| Axios | 1 | Requisições HTTP |
| React Router DOM | 7 | Roteamento SPA |
| Lucide React | 1 | Ícones |

---

## Integrações

### Resend (E-mail)
Utilizado para envio dos códigos OTP tanto no login quanto no cadastro. Configure a chave em `application.properties`:
```properties
resend.api-key=sua_chave_aqui
```

### Snowflake ID Generator
Implementação própria do algoritmo Snowflake para geração de IDs únicos e ordenados cronologicamente, convertidos para Base62 para formar os short codes das URLs.

---

## Estrutura do Projeto

```
url-cutter/
├── url-cutter-api/          # Backend Spring Boot
│   ├── src/main/java/
│   │   └── com/tcc/url_cutter_api/
│   │       ├── config/      # SecurityConfig, CORS
│   │       ├── controller/  # REST controllers
│   │       ├── dto/         # Request/Response records
│   │       ├── enums/       # UserStatus, RoleName
│   │       ├── model/       # Entidades JPA/R2DBC
│   │       ├── repo/        # Repositórios reativos
│   │       ├── service/     # Lógica de negócio
│   │       └── utils/       # JWT, QRCode, Snowflake, SecurityUtils
│   ├── src/test/            # Testes unitários (JUnit 5 + Mockito)
│   └── src/main/resources/
│       └── application.properties
│
├── url-cutter-app/          # Frontend React
│   ├── src/
│   │   ├── components/      # Navbar, AuthButtons
│   │   ├── config/          # axiosInstance (interceptor JWT)
│   │   ├── listener/        # NavigationListener (Zustand → React Router)
│   │   ├── pages/           # Shortener, Analytics, LoginForm, SignUp, Profile, VerifyOtp
│   │   └── store/           # useAuthStore, useUrlStore, useQrCodeStore, useNavigation
│   └── index.html
│
└── sql/
    └── tables.sql           # Script DDL completo
```

---

## Pré-requisitos

- **Java 21+**
- **Node.js 20+** e **npm**
- **PostgreSQL 14+** rodando localmente ou em nuvem
- Conta no [Resend](https://resend.com) para envio de e-mails

---

## Configuração do Banco de Dados

1. Crie um banco chamado `url-cutter`:
```sql
CREATE DATABASE "url-cutter";
```

2. Execute o script DDL para criar as tabelas:
```bash
psql -U postgres -d url-cutter -f sql/tables.sql
```

O script cria as seguintes tabelas:
- `users` — dados dos usuários
- `roles` — perfis de acesso (ADMIN, OPERADOR, USER)
- `user_roles` — associação usuário ↔ role
- `two_factor_codes` — OTPs de autenticação
- `short_url` — URLs encurtadas
- `click_event` — registro de cliques

---

## Como Executar

### 1. Backend (API)

```bash
cd url-cutter-api

# Com Maven Wrapper (recomendado)
./mvnw spring-boot:run

# Ou no Windows
mvnw.cmd spring-boot:run
```

A API estará disponível em `http://localhost:8081`.

### 2. Frontend

```bash
cd url-cutter-app

# Instalar dependências
npm install

# Iniciar servidor de desenvolvimento
npm run dev
```

O frontend estará disponível em `http://localhost:5173`.

### 3. Build de Produção (Frontend)

```bash
cd url-cutter-app
npm run build
npm run preview
```

---

## Variáveis de Ambiente

Configure o arquivo `url-cutter-api/src/main/resources/application.properties`:

```properties
# Servidor
server.port=8081

# JWT
jwt.secret=sua_chave_base64_aqui

# URL base para redirecionamento e QR Code
app.base-url=http://localhost:8081/r/

# E-mail (Resend)
resend.api-key=sua_chave_resend_aqui

# Banco de dados
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/url-cutter
spring.r2dbc.username=postgres
spring.r2dbc.password=sua_senha_aqui
```

> **Atenção:** Nunca versione `application.properties` com credenciais reais. Use variáveis de ambiente ou um vault em produção.

---

## Endpoints da API

### Autenticação (`/auth`)
| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| POST | `/auth/signup` | Cadastro de usuário | Público |
| POST | `/auth/login` | Login (envia OTP) | Público |
| POST | `/auth/verify-signup` | Verifica OTP do cadastro e ativa conta | Público |
| POST | `/auth/verify-2fa` | Verifica OTP do login e retorna JWT | Público |
| POST | `/auth/change-password` | Altera a senha | JWT |
| DELETE | `/auth/delete` | Exclui a conta do usuário | JWT |

### URLs (`/api`)
| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| POST | `/api/shorten` | Encurta uma URL | Público* |
| GET | `/api/my-urls` | Lista URLs do usuário autenticado | JWT |
| DELETE | `/api/{id}` | Remove uma URL | JWT |

### Redirecionamento
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/r/{shortCode}` | Redireciona para a URL original (302) e registra o clique |

### Cliques (`/api/clicks`)
| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| GET | `/api/clicks` | Lista todos os cliques | Público* |
| GET | `/api/clicks/url/{urlId}` | Cliques de uma URL específica | Público* |
| GET | `/api/clicks/url/{urlId}/analytics` | Analytics agregado por data | Público* |
| GET | `/api/clicks/ip/{ip}` | Cliques por endereço IP | Público* |
| GET | `/api/clicks/{id}` | Clique por ID | Público* |
| POST | `/api/clicks` | Registra um clique manualmente | Público* |
| DELETE | `/api/clicks/{id}` | Remove um clique | Público* |

### QR Code
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/qr/qrgenerator/{shortCode}` | Gera imagem PNG do QR Code (300×300) |

> *Rotas marcadas como "Público*" estão abertas na `SecurityConfig` atual (`/api/**`). Em produção, recomenda-se restringir as rotas de escrita e listagem com autenticação adequada.

---

## Executando os Testes

```bash
cd url-cutter-api
./mvnw test
```

Os testes cobrem os controllers principais com Mockito e StepVerifier (Reactor Test):
- `AuthControllerTest` — fluxos de login, signup, 2FA, troca de senha e exclusão de conta
- `UrlControllerTest` — encurtamento, listagem e exclusão de URLs
- `ClickEventControllerTest` — registro e consulta de cliques
- `UrlSenderControllerTest` — redirecionamento e captura de IP/User-Agent
- `QRCodeControllerTest` — geração de QR Code e tratamento de erros
