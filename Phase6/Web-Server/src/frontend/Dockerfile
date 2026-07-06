# --- Stage 1: build the static production bundle with Node ---
FROM node:22-alpine AS build
WORKDIR /app

# Copy only the lockfile/manifest first so `npm ci` is cached as its own layer
# and only reruns when dependencies actually change.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# --- Stage 2: serve the built static files with Nginx, acting as a reverse proxy ---
# Alpine-based Nginx is a tiny, purpose-built static file server -- there is no
# reason to ship Node or the source/dev-dependencies into the runtime image.
FROM nginx:1.27-alpine AS runtime
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
