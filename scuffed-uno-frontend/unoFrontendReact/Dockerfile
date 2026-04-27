ARG NODE_VERSION=24.14.0-alpine

FROM node:${NODE_VERSION} AS builder
WORKDIR /app

COPY unoFrontendReact/package.json unoFrontendReact/package-lock.json ./
COPY unoFrontendReact/. .
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN --mount=type=cache,target=/root/.npm npm ci

COPY unoFrontendReact/. .
RUN npm run build

FROM nginx:stable-alpine AS production
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]