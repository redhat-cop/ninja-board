FROM registry.redhat.io/rhel8/nodejs-18-minimal:1-33
WORKDIR /app
COPY package.json ./
COPY package-lock.json ./
COPY ./ ./
RUN npm i

CMD ["npm", "run", "start"]