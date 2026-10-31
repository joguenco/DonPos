import { useEffect, useState } from 'react';
import { FlatList, Modal, Platform, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import * as SecureStore from 'expo-secure-store';
import { SafeAreaProvider, useSafeAreaInsets } from 'react-native-safe-area-context';
import { useKeepAwake } from 'expo-keep-awake';
import * as Brightness from 'expo-brightness';

// El servidor envia un ping cada 30 s; si no llega nada en este tiempo, la conexion se da por perdida
const SILENCE_TIMEOUT = 70_000;
// Si DonPos nunca limpia la tablet, los datos del cliente se borran solos despues de este tiempo
const CUSTOMER_TIMEOUT = 60_000;
// Sin datos del cliente, la pantalla se oscurece despues de este tiempo
const DIM_TIMEOUT = 30_000;
// Brillo de la pantalla oscurecida: 0 es negro y 1 el maximo
const DIM_BRIGHTNESS = 0.2;
// Configurar se abre manteniendo presionada la barra negra este tiempo, para que el cliente no lo cambie por accidente
const SETTINGS_PRESS_DELAY = 6000;
// Codigo con el que el servidor cierra la conexion cuando la API esta mal
const UNAUTHORIZED_CLOSE_CODE = 4001;
const UNAUTHORIZED = 'unauthorized';
const SETTINGS_KEY = 'settings';
const CASHIER_KEY = 'cashier';

const CUSTOMER_FIELDS = [
  ['identification', 'Identificación'],
  ['name', 'Nombre'],
  ['email', 'E-mail'],
  ['address', 'Dirección'],
  ['phone', 'Teléfono'],
];

// Servidor y API se guardan para siempre: en el celular en la caja fuerte (SecureStore),
// en el navegador en localStorage porque ahi no existe la caja fuerte
async function readSettings() {
  const value = Platform.OS === 'web'
    ? localStorage.getItem(SETTINGS_KEY)
    : await SecureStore.getItemAsync(SETTINGS_KEY);

  return value ? JSON.parse(value) : null;
}

async function writeSettings(settings) {
  const value = JSON.stringify(settings);

  if (Platform.OS === 'web') {
    localStorage.setItem(SETTINGS_KEY, value);
  } else {
    await SecureStore.setItemAsync(SETTINGS_KEY, value);
  }
}

// El cajero dura mientras la pagina este abierta: en el navegador sessionStorage aguanta las
// recargas y se borra al cerrar la pestaña; en el celular basta con la memoria de la app
function readCashier() {
  return Platform.OS === 'web' ? sessionStorage.getItem(CASHIER_KEY) : null;
}

function writeCashier(cashier) {
  if (Platform.OS === 'web') {
    sessionStorage.setItem(CASHIER_KEY, cashier);
  }
}

// Tambien sirve para probar la API: si esta mal el servidor responde 401
async function getCashiers(server, apiKey) {
  const response = await fetch(`http://${server}/people`, { headers: { 'X-API-KEY': apiKey } });

  if (response.status === 401) {
    throw new Error(UNAUTHORIZED);
  }

  const people = await response.json();
  return people.map((person) => person.name);
}

// Vuelve al brillo que tiene configurado la tablet; el iPhone no permite eso y se usa el maximo
function restoreBrightness() {
  if (Platform.OS === 'android') {
    Brightness.restoreSystemBrightnessAsync();
  } else {
    Brightness.setBrightnessAsync(1);
  }
}

// SafeAreaProvider mide cuanto ocupa la barra del celular (hora, bateria, camara) para no tapar nada con ella
export default function App() {
  return (
    <SafeAreaProvider>
      <TabletScreen />
    </SafeAreaProvider>
  );
}

function TabletScreen() {
  const insets = useSafeAreaInsets();
  const [settings, setSettings] = useState(null);
  const [cashier, setCashier] = useState(readCashier);
  const [editing, setEditing] = useState(false);
  const [serverDraft, setServerDraft] = useState(Platform.OS === 'web' ? window.location.host : '');
  const [apiKeyDraft, setApiKeyDraft] = useState('');
  const [settingsError, setSettingsError] = useState('');
  const [saving, setSaving] = useState(false);
  const [cashiers, setCashiers] = useState(null);
  const [cashiersError, setCashiersError] = useState(false);
  const [cashierDraft, setCashierDraft] = useState('');
  const [connected, setConnected] = useState(false);
  const [customer, setCustomer] = useState(null);
  const [touchedAt, setTouchedAt] = useState(0);

  // La pantalla nunca se apaga mientras la app este abierta; en la web solo funciona con HTTPS
  useKeepAwake();

  useEffect(() => {
    readSettings()
      .then((value) => {
        if (value) {
          setSettings(value);
        } else {
          setEditing(true);
        }
      })
      .catch(() => setEditing(true));
  }, []);

  useEffect(() => {
    if (settings && !cashier) {
      loadCashiers();
    }
  }, [settings, cashier]);

  useEffect(() => {
    if (!settings || !cashier) {
      return;
    }

    let socket = null;
    let attempt = 0;
    let retryTimer = null;
    let silenceTimer = null;

    const connect = () => {
      socket = new WebSocket(
        `ws://${settings.server}/start_web_socket?cashier=${encodeURIComponent(cashier)}`
          + `&apiKey=${encodeURIComponent(settings.apiKey)}`,
      );

      socket.onopen = () => {
        attempt = 0;
        setConnected(true);
        watchSilence();
      };

      socket.onmessage = (event) => {
        watchSilence();
        const data = JSON.parse(event.data);

        switch (data.event) {
          case 'send-message':
            setCustomer(data.message);
            break;

          case 'clear-message':
            setCustomer(null);
            break;
        }
      };

      socket.onclose = (event) => {
        if (event.code === UNAUTHORIZED_CLOSE_CODE) {
          askForNewApiKey();
        } else {
          reconnect();
        }
      };
    };

    const watchSilence = () => {
      clearTimeout(silenceTimer);
      silenceTimer = setTimeout(reconnect, SILENCE_TIMEOUT);
    };

    // Espera 1, 2, 4, 8 y despues 10 segundos entre cada intento
    const reconnect = () => {
      disconnect();
      retryTimer = setTimeout(connect, Math.min(1000 * 2 ** attempt, 10_000));
      attempt++;
    };

    const disconnect = () => {
      clearTimeout(retryTimer);
      clearTimeout(silenceTimer);
      setConnected(false);

      if (socket) {
        socket.onclose = null;
        socket.close();
      }
    };

    connect();

    return disconnect;
  }, [settings, cashier]);

  useEffect(() => {
    if (!customer) {
      return;
    }

    const timer = setTimeout(() => setCustomer(null), CUSTOMER_TIMEOUT);
    return () => clearTimeout(timer);
  }, [customer]);

  // Con la tablet en espera la pantalla se oscurece; llegan datos o alguien la toca y se aclara.
  // Mientras se configura o se escoge el cajero no se oscurece. En la web el navegador no deja cambiar el brillo
  useEffect(() => {
    if (Platform.OS === 'web') {
      return;
    }

    restoreBrightness();

    if (customer || editing || !cashier) {
      return;
    }

    const timer = setTimeout(() => Brightness.setBrightnessAsync(DIM_BRIGHTNESS), DIM_TIMEOUT);
    return () => clearTimeout(timer);
  }, [customer, editing, cashier, touchedAt]);

  // La API cambio en el servidor: vuelve a Configurar con el mismo servidor para escribir la nueva
  const askForNewApiKey = () => {
    setServerDraft(settings.server);
    setApiKeyDraft('');
    setSettingsError('La API cambió, ingrese la nueva');
    setSettings(null);
    setEditing(true);
  };

  const loadCashiers = () => {
    setCashiers(null);
    setCashiersError(false);
    getCashiers(settings.server, settings.apiKey)
      .then((names) => setCashiers(names))
      .catch((error) => {
        if (error.message === UNAUTHORIZED) {
          askForNewApiKey();
        } else {
          setCashiers([]);
          setCashiersError(true);
        }
      });
  };

  // Vacia significa mantener la API que ya estaba guardada
  const apiKey = apiKeyDraft.trim() || settings?.apiKey;
  const canSave = serverDraft.trim() !== '' && Boolean(apiKey) && !saving;

  // Antes de guardar prueba la API con el servidor; asi nunca se guarda una API mala
  const saveSettings = () => {
    const server = serverDraft.trim().replace(/^\w+:\/\//, '');

    setSaving(true);
    setSettingsError('');
    getCashiers(server, apiKey)
      .then(async () => {
        const newSettings = { server, apiKey };

        await writeSettings(newSettings);
        setSettings(newSettings);
        setEditing(false);
      })
      .catch((error) => {
        setSettingsError(error.message === UNAUTHORIZED ? 'La API no es correcta' : `No se pudo conectar con ${server}`);
      })
      .finally(() => setSaving(false));
  };

  const openSettings = () => {
    if (!settings) {
      return;
    }

    setServerDraft(settings.server);
    setApiKeyDraft('');
    setSettingsError('');
    setEditing(true);
  };

  const startCashier = () => {
    writeCashier(cashierDraft);
    setCashier(cashierDraft);
  };

  const cashiersMessage = cashiersError
    ? `No se pudo conectar con ${settings?.server}`
    : cashiers === null ? 'Buscando cajeros…' : 'No hay cajeros';

  return (
    // Cualquier toque en la pantalla la vuelve a aclarar y reinicia los 30 segundos
    <View style={styles.container} onTouchStart={() => setTouchedAt(Date.now())}>
      {/* El fondo negro sigue por detras de la barra del celular y el texto empieza debajo de ella */}
      <Pressable
        style={[styles.header, { paddingTop: insets.top + 16 }]}
        onLongPress={openSettings}
        delayLongPress={SETTINGS_PRESS_DELAY}
      >
        <Text style={styles.title}>{settings && cashier ? `Atendido por ${cashier}` : 'Kent'}</Text>
        {settings && cashier && !connected && <Text style={styles.status}>Reconectando…</Text>}
      </Pressable>

      <Modal visible={editing} transparent animationType="fade" onRequestClose={() => settings && setEditing(false)}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Configurar tablet</Text>
            <Text style={styles.modalSubtitle}>Escriba la dirección del servidor y la API.</Text>

            <Text style={styles.sectionTitle}>Servidor</Text>
            <TextInput
              style={styles.input}
              value={serverDraft}
              onChangeText={setServerDraft}
              placeholder="192.168.1.8:8181"
              autoCapitalize="none"
              autoCorrect={false}
            />

            <Text style={styles.sectionTitle}>API</Text>
            <TextInput
              style={styles.input}
              value={apiKeyDraft}
              onChangeText={setApiKeyDraft}
              placeholder={settings ? 'Mantener la actual' : 'Pegue la API del servidor'}
              secureTextEntry
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="off"
            />

            {settingsError !== '' && <Text style={styles.error}>{settingsError}</Text>}

            <Pressable style={[styles.button, !canSave && styles.buttonDisabled]} disabled={!canSave} onPress={saveSettings}>
              <Text style={styles.buttonText}>{saving ? 'Probando…' : 'Guardar'}</Text>
            </Pressable>
            {settings && (
              <Pressable style={styles.cancelButton} onPress={() => setEditing(false)}>
                <Text style={styles.cancelButtonText}>Cancelar</Text>
              </Pressable>
            )}
          </View>
        </View>
      </Modal>

      {settings && !cashier && (
        <View style={styles.body}>
          <View style={[styles.modalCard, styles.pickerCard]}>
            <Text style={styles.modalTitle}>Escoja el cajero</Text>
            <Text style={styles.modalSubtitle}>¿Quién atiende en esta tablet?</Text>

            <FlatList
              style={styles.nameList}
              data={cashiers ?? []}
              keyExtractor={(name) => name}
              renderItem={({ item }) => (
                <Pressable
                  onPress={() => setCashierDraft(item)}
                  style={[styles.nameOption, cashierDraft === item && styles.nameOptionSelected]}
                >
                  <Text style={styles.nameOptionText}>{item}</Text>
                  {cashierDraft === item && <Text style={styles.check}>✓</Text>}
                </Pressable>
              )}
              ListEmptyComponent={<Text style={styles.nameEmpty}>{cashiersMessage}</Text>}
            />
            {cashiersError && (
              <Pressable style={styles.secondaryButton} onPress={loadCashiers}>
                <Text style={styles.secondaryButtonText}>Reintentar</Text>
              </Pressable>
            )}

            <Pressable
              style={[styles.button, !cashierDraft && styles.buttonDisabled]}
              disabled={!cashierDraft}
              onPress={startCashier}
            >
              <Text style={styles.buttonText}>Empezar</Text>
            </Pressable>
          </View>
        </View>
      )}

      {customer && (
        <View style={styles.body}>
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Por favor, verifique sus datos</Text>
            {CUSTOMER_FIELDS.map(([key, label]) => (
              <View key={key} style={styles.row}>
                <Text style={styles.label}>{label}</Text>
                <Text style={styles.value}>{customer[key] || '—'}</Text>
              </View>
            ))}
          </View>
        </View>
      )}

      <StatusBar style="light" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f0f0f0',
  },
  header: {
    backgroundColor: '#0b0d11',
    padding: 16,
    alignItems: 'center',
  },
  title: {
    color: '#ffffff',
    fontSize: 20,
    fontWeight: 'bold',
  },
  status: {
    color: '#75fead',
    marginTop: 4,
  },
  modalOverlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
    backgroundColor: 'rgba(11, 13, 17, 0.6)',
  },
  modalCard: {
    width: '100%',
    maxWidth: 460,
    backgroundColor: '#ffffff',
    borderRadius: 16,
    padding: 24,
  },
  pickerCard: {
    alignSelf: 'center',
  },
  modalTitle: {
    color: '#0b0d11',
    fontSize: 22,
    fontWeight: 'bold',
  },
  modalSubtitle: {
    color: '#666666',
    marginTop: 4,
  },
  sectionTitle: {
    color: '#666666',
    fontSize: 13,
    fontWeight: 'bold',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginTop: 20,
    marginBottom: 8,
  },
  input: {
    borderWidth: 1,
    borderColor: '#c8c8c8',
    borderRadius: 10,
    paddingVertical: 12,
    paddingHorizontal: 14,
    fontSize: 16,
  },
  error: {
    color: '#c0392b',
    marginTop: 12,
  },
  secondaryButton: {
    marginTop: 8,
    borderWidth: 1,
    borderColor: '#0b0d11',
    borderRadius: 10,
    paddingVertical: 10,
    alignItems: 'center',
  },
  secondaryButtonText: {
    color: '#0b0d11',
    fontWeight: 'bold',
  },
  nameList: {
    maxHeight: 280,
    marginTop: 16,
  },
  nameOption: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#c8c8c8',
    borderRadius: 10,
    paddingVertical: 12,
    paddingHorizontal: 14,
    marginBottom: 8,
  },
  nameOptionSelected: {
    backgroundColor: '#e8fff1',
    borderColor: '#1f9d55',
  },
  nameOptionText: {
    color: '#0b0d11',
    fontSize: 16,
  },
  check: {
    color: '#1f9d55',
    fontSize: 18,
    fontWeight: 'bold',
  },
  nameEmpty: {
    color: '#666666',
    paddingVertical: 12,
  },
  button: {
    marginTop: 20,
    backgroundColor: '#0b0d11',
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
  },
  buttonDisabled: {
    opacity: 0.4,
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  cancelButton: {
    marginTop: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  cancelButtonText: {
    color: '#666666',
  },
  body: {
    padding: 24,
  },
  card: {
    alignSelf: 'center',
    width: '100%',
    maxWidth: 720,
    backgroundColor: '#ffffff',
    borderRadius: 16,
    padding: 24,
  },
  cardTitle: {
    color: '#0b0d11',
    fontSize: 26,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  row: {
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#eeeeee',
  },
  label: {
    color: '#666666',
    fontSize: 14,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  value: {
    color: '#0b0d11',
    fontSize: 26,
    fontWeight: 'bold',
    marginTop: 2,
  },
});
