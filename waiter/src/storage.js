import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

const SETTINGS_KEY = 'settings';
const SESSION_KEY = 'session';

// En el celular se guarda en la caja fuerte (SecureStore); en el navegador no existe,
// por eso ahí se usa localStorage
async function read(key) {
  const value = Platform.OS === 'web'
    ? localStorage.getItem(key)
    : await SecureStore.getItemAsync(key);

  return value ? JSON.parse(value) : null;
}

async function write(key, data) {
  if (data === null) {
    if (Platform.OS === 'web') {
      localStorage.removeItem(key);
    } else {
      await SecureStore.deleteItemAsync(key);
    }
    return;
  }

  const value = JSON.stringify(data);
  if (Platform.OS === 'web') {
    localStorage.setItem(key, value);
  } else {
    await SecureStore.setItemAsync(key, value);
  }
}

// { server: '192.168.1.8:8080' }
export const readSettings = () => read(SETTINGS_KEY);
export const writeSettings = (settings) => write(SETTINGS_KEY, settings);

// { username, accessToken, refreshToken }: así la app recuerda qué mesero es
export const readSession = () => read(SESSION_KEY);
export const writeSession = (session) => write(SESSION_KEY, session);
