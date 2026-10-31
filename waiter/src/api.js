import { writeSession } from './storage';

const ORDER = '/donpos/order/v1';

// Error que muestra la app: code viene de DonPosOrder (TAKEN, LOCKED, GONE, NOT_HELD...)
export class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

let server = null;
let session = null;
let listeners = { onExpired: () => {}, onRefreshed: () => {} };

export function configure(newServer, newSession, newListeners) {
  server = newServer;
  session = newSession;
  listeners = newListeners;
}

// Acepta '192.168.1.8:8080' o 'http://192.168.1.8:8080/'
export function normalizeServer(text) {
  return text.trim().replace(/^\w+:\/\//, '').replace(/\/+$/, '');
}

async function send(address, method, path, body, token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(`http://${address}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'NETWORK', `No se pudo conectar con ${address}`);
  }

  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    // Respuesta que no es JSON (por ejemplo una página de error)
  }

  if (!response.ok) {
    // DonPosOrder responde { code, message }; Micronaut responde { message }
    throw new ApiError(response.status, data?.code ?? 'ERROR', data?.message ?? 'Error del servidor');
  }
  return data;
}

export function ping(address) {
  return send(address, 'GET', '/ping');
}

export async function login(address, username, password) {
  try {
    const data = await send(address, 'POST', '/donpos/v1/login', { username, password });
    return { username: data.username, accessToken: data.access_token, refreshToken: data.refresh_token };
  } catch (error) {
    if (error.status === 401) {
      const message = error.message.includes('mesero') ? error.message : 'Usuario o contraseña incorrectos';
      throw new ApiError(401, 'LOGIN', message);
    }
    throw error;
  }
}

// El token dura 1 hora; con el refresh token se pide otro sin volver a escribir la clave
async function refresh() {
  try {
    const data = await send(server, 'POST', '/oauth/access_token', {
      grant_type: 'refresh_token',
      refresh_token: session.refreshToken,
    });
    session = { ...session, accessToken: data.access_token, refreshToken: data.refresh_token ?? session.refreshToken };
    await writeSession(session);
    listeners.onRefreshed(session);
    return true;
  } catch {
    return false;
  }
}

async function request(method, path, body) {
  try {
    return await send(server, method, path, body, session?.accessToken);
  } catch (error) {
    if (error.status !== 401) {
      throw error;
    }
    if (await refresh()) {
      return send(server, method, path, body, session.accessToken);
    }
    listeners.onExpired();
    throw new ApiError(401, 'SESSION', 'La sesión terminó, vuelva a entrar');
  }
}

export const getFloors = () => request('GET', `${ORDER}/floors/`);
export const getTables = (floorId) => request('GET', `${ORDER}/assigner/floors/${encodeURIComponent(floorId)}/tables`);
export const openTable = (placeId) => request('POST', `${ORDER}/assigner/${encodeURIComponent(placeId)}/open`);
export const signalTable = (placeId) => request('POST', `${ORDER}/assigner/${encodeURIComponent(placeId)}/signal`);
export const closeTable = (placeId) => request('POST', `${ORDER}/assigner/${encodeURIComponent(placeId)}/close`);
export const getCategories = () => request('GET', `${ORDER}/order/categories`);
export const getProducts = (categoryId) => request('GET', `${ORDER}/order/categories/${encodeURIComponent(categoryId)}/products`);
export const addProduct = (placeId, productId) => request('POST', `${ORDER}/order/${encodeURIComponent(placeId)}/lines`, { productId });
export const removeLine = (placeId, index) => request('DELETE', `${ORDER}/order/${encodeURIComponent(placeId)}/lines/${index}`);
