# ---- 第一阶段：Node.js 编译 ----
FROM node:18-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com && npm install
COPY . .
RUN npm run build

# ---- 第二阶段：Nginx 运行 ----
FROM nginx:alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
