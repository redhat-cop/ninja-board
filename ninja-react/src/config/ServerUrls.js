import axios from "axios";

const local = axios.create({
  baseURL: "http://localhost:8080"
});

const dev = {};

const prod = {};

const API = local;

export default API;
