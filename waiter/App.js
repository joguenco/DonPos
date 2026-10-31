import { useEffect, useState } from 'react';
import { View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider, useSafeAreaInsets } from 'react-native-safe-area-context';
import { configure } from './src/api';
import { readSession, readSettings, writeSession, writeSettings } from './src/storage';
import { common } from './src/theme';
import LoginScreen from './src/screens/LoginScreen';
import OrderScreen from './src/screens/OrderScreen';
import SettingsScreen from './src/screens/SettingsScreen';
import TablesScreen from './src/screens/TablesScreen';

// SafeAreaProvider mide cuánto ocupa la barra del celular (hora, batería, cámara) para no taparla
export default function App() {
  return (
    <SafeAreaProvider>
      <StatusBar style="light" />
      <Main />
    </SafeAreaProvider>
  );
}

// Pantallas en orden: Configurar → Login → Mesas (piso y página) → Dentro de la mesa
function Main() {
  const insets = useSafeAreaInsets();
  const [ready, setReady] = useState(false);
  const [server, setServer] = useState(null);
  const [session, setSession] = useState(null);
  const [editingServer, setEditingServer] = useState(false);
  const [floorId, setFloorId] = useState(null);
  const [page, setPage] = useState(0);
  const [table, setTable] = useState(null);
  const [notice, setNotice] = useState('');

  useEffect(() => {
    Promise.all([readSettings(), readSession()])
      .then(([settings, savedSession]) => {
        setServer(settings?.server ?? null);
        setSession(savedSession);
      })
      .catch(() => {})
      .finally(() => setReady(true));
  }, []);

  const logout = (message = '') => {
    writeSession(null);
    setSession(null);
    setTable(null);
    setFloorId(null);
    setPage(0);
    setNotice(message);
  };

  // api.js usa siempre el servidor y la sesión de ahora; se actualiza en cada dibujo
  // (antes que las pantallas pidan datos) y avisa cuando renueva el token o la sesión vence
  configure(server, session, {
    onExpired: () => logout('La sesión terminó, vuelva a entrar'),
    onRefreshed: setSession,
  });

  const saveServer = async (address) => {
    await writeSettings({ server: address });
    setServer(address);
    setEditingServer(false);
  };

  const saveSession = async (newSession) => {
    await writeSession(newSession);
    setNotice('');
    setSession(newSession);
  };

  const exitTable = (message) => {
    setTable(null);
    setNotice(message);
  };

  if (!ready) {
    return <View style={common.container} />;
  }

  if (!server || editingServer) {
    return (
      <View style={[common.container, { paddingTop: insets.top }]}>
        <SettingsScreen
          server={server}
          onSave={saveServer}
          onCancel={server ? () => setEditingServer(false) : null}
        />
      </View>
    );
  }

  if (!session) {
    return (
      <View style={[common.container, { paddingTop: insets.top }]}>
        <LoginScreen
          server={server}
          message={notice}
          onLogin={saveSession}
          onChangeServer={() => setEditingServer(true)}
        />
      </View>
    );
  }

  if (table) {
    return (
      <OrderScreen
        key={table.id}
        table={table}
        initialOrder={table.order}
        username={session.username}
        onExit={exitTable}
      />
    );
  }

  return (
    <TablesScreen
      username={session.username}
      floorId={floorId}
      onFloorChange={setFloorId}
      page={page}
      onPageChange={setPage}
      notice={notice}
      onNotice={setNotice}
      onOpen={(openedTable, order) => {
        setNotice('');
        setTable({ ...openedTable, order });
      }}
      onLogout={() => logout()}
    />
  );
}
