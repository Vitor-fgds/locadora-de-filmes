# Fase 7 — imagem única da aplicação (API Spring Boot + frontend React).
# Contexto de build = raiz do repositório (ver docker-compose.yml, serviço "app"):
#   docker build -f backend/Dockerfile .

# ---- 1) Frontend: npm ci + build (gera /front/dist) ----
FROM node:26-alpine AS frontend
WORKDIR /front
# Só os manifestos primeiro: a camada do npm ci fica em cache enquanto as dependências não mudam.
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- 2) Backend: dependências Maven em camada própria + jar com o frontend embutido ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY backend/mvnw backend/pom.xml ./
COPY backend/.mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY backend/src src
# O build do React vira recurso estático do jar (servido pelo SpaConfig, mesma origem da API).
COPY --from=frontend /front/dist src/main/resources/static
# Testes rodam fora da imagem (./mvnw verify); aqui só empacota.
RUN ./mvnw -q -B package -DskipTests && mv target/locadora-backend-*.jar app.jar

# ---- 3) Imagem final: só JRE + jar, sem root ----
FROM eclipse-temurin:25-jre
# curl: usado só pelo healthcheck do compose (a imagem base não traz curl nem wget).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app --no-create-home app
# /app é o diretório de trabalho: "filesystem:../db/seed" do perfil dev vira /db/seed
# (o compose monta ./db/seed ali, somente leitura).
WORKDIR /app
COPY --from=build /build/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
