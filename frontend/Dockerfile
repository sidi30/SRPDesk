FROM node:20-alpine AS build
WORKDIR /app

ARG VITE_API_URL=/api/v1
ARG VITE_KEYCLOAK_URL=http://localhost:18180
ARG VITE_KEYCLOAK_REALM=lexsecura
ARG VITE_KEYCLOAK_CLIENT_ID=frontend

ENV VITE_API_URL=$VITE_API_URL
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ENV VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM
ENV VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID

COPY package.json package-lock.json .npmrc ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && chown nginx:nginx /var/run/nginx.pid
EXPOSE 80
USER nginx
CMD ["nginx", "-g", "daemon off;"]
