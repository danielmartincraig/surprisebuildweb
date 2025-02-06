FROM clojure:temurin-23-lein-bookworm
RUN npm install && npm run release

FROM nginx:1.27.3

# Copy custom configuration file from the current directory
COPY nginx.conf /etc/nginx/

# Copy static website content
COPY public /usr/share/nginx/html/

# Start nginx
CMD ["nginx", "-g", "daemon off;"]