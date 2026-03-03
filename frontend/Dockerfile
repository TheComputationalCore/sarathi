# ===============================
# Build Stage
# ===============================
FROM node:18-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ===============================
# Runtime Stage (Caddy)
# ===============================
FROM caddy:2-alpine

# Copy build output
COPY --from=build /app/build /srv

# Copy Caddy config
COPY Caddyfile /etc/caddy/Caddyfile

# Caddy listens on 80 internally
EXPOSE 80