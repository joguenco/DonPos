import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { getFloors, getTables, openTable } from '../api';
import Select from '../components/Select';
import { common, STATUS } from '../theme';

// Las mesas se ven de 6 en 6 y se vuelven a pedir cada 5 segundos (como el refresco de DonPos)
const PAGE_SIZE = 6;
const REFRESH_INTERVAL = 5000;

export default function TablesScreen({
  username, floorId, onFloorChange, page, onPageChange, notice, onNotice, onOpen, onLogout,
}) {
  const insets = useSafeAreaInsets();
  const [floors, setFloors] = useState(null);
  const [tables, setTables] = useState(null);
  const [opening, setOpening] = useState(null);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    getFloors()
      .then((list) => {
        setFloors(list);
        // Si hay un solo piso no se pregunta: salen sus mesas directo
        if (list.length > 0 && !list.some((floor) => floor.id === floorId)) {
          onFloorChange(list[0].id);
        }
      })
      .catch((error) => onNotice(error.message));
  }, []);

  useEffect(() => {
    if (!floorId) {
      return;
    }

    let active = true;
    const load = () => getTables(floorId)
      .then((list) => active && setTables(list))
      .catch((error) => active && onNotice(error.message));

    load();
    const timer = setInterval(load, REFRESH_INTERVAL);
    return () => {
      active = false;
      clearInterval(timer);
    };
  }, [floorId]);

  // El aviso se borra solo a los 4 segundos
  useEffect(() => {
    if (!notice) {
      return;
    }
    const timer = setTimeout(() => onNotice(''), 4000);
    return () => clearTimeout(timer);
  }, [notice]);

  const pages = tables ? Math.max(1, Math.ceil(tables.length / PAGE_SIZE)) : 1;
  const currentPage = Math.min(page, pages - 1);
  const visibleTables = (tables ?? []).slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);

  // "Table 1 – Table 6", "Table 7 – Table 12"...
  const pageOptions = Array.from({ length: pages }, (_, index) => {
    const first = tables?.[index * PAGE_SIZE];
    const last = tables?.[Math.min((index + 1) * PAGE_SIZE, tables.length) - 1];
    return { value: index, label: first ? `${first.name} – ${last.name}` : 'Mesas' };
  });

  // Botón Actualizar: pide las mesas ya, sin esperar al refresco automático
  const refresh = () => {
    setRefreshing(true);
    getTables(floorId)
      .then(setTables)
      .catch((error) => onNotice(error.message))
      .finally(() => setRefreshing(false));
  };

  const chooseFloor = (id) => {
    setTables(null);
    onPageChange(0);
    onFloorChange(id);
  };

  // Solo se entra a mesas libres o propias; las demás solo avisan
  const tapTable = (table) => {
    if (table.status === 'TAKEN') {
      onNotice(`Esta mesa es de ${table.waiter ?? 'otro usuario'}`);
      return;
    }
    if (table.status === 'LOCKED') {
      onNotice('La están facturando en caja, intenta en un momento');
      return;
    }

    setOpening(table.id);
    openTable(table.id)
      .then((order) => onOpen({ id: table.id, name: table.name }, order))
      .catch((error) => onNotice(error.message))
      .finally(() => setOpening(null));
  };

  return (
    <View style={common.container}>
      <View style={[common.header, { paddingTop: insets.top + 12 }]}>
        <View>
          <Text style={common.headerTitle}>Mesas</Text>
          <Text style={common.headerSubtitle}>Mesero: {username}</Text>
        </View>
        <Pressable style={common.headerButton} onPress={onLogout}>
          <Text style={common.headerButtonText}>Cerrar sesión</Text>
        </Pressable>
      </View>

      {notice !== '' && (
        <View style={common.notice}>
          <Text style={common.noticeText}>{notice}</Text>
        </View>
      )}

      <ScrollView contentContainerStyle={styles.body}>
        <View style={styles.toolbar}>
          <Text style={styles.toolbarText}>Se actualiza solo cada {REFRESH_INTERVAL / 1000} s</Text>
          <Pressable
            style={[styles.refreshButton, (refreshing || !floorId) && common.buttonDisabled]}
            onPress={refresh}
            disabled={refreshing || !floorId}
          >
            <Text style={styles.refreshButtonText}>{refreshing ? 'Actualizando…' : '↻ Actualizar'}</Text>
          </Pressable>
        </View>

        {floors && floors.length > 1 && (
          <>
            <Text style={common.sectionTitle}>Piso</Text>
            <Select
              title="Escoja el piso"
              value={floorId}
              options={floors.map((floor) => ({ value: floor.id, label: floor.name }))}
              onChange={chooseFloor}
            />
          </>
        )}

        {tables && tables.length > PAGE_SIZE && (
          <>
            <Text style={common.sectionTitle}>Mesas</Text>
            <Select title="Escoja las mesas" value={currentPage} options={pageOptions} onChange={onPageChange} />
          </>
        )}

        {tables === null && <ActivityIndicator style={styles.loading} size="large" color="#0b0d11" />}
        {tables && tables.length === 0 && <Text style={styles.empty}>Este piso no tiene mesas</Text>}

        <View style={styles.grid}>
          {visibleTables.map((table) => {
            const status = STATUS[table.status];
            return (
              <Pressable
                key={table.id}
                style={[styles.table, { backgroundColor: status.background, borderColor: status.color }]}
                onPress={() => tapTable(table)}
                disabled={opening !== null}
              >
                <Text style={styles.tableName}>{table.name}</Text>
                <Text style={[styles.tableStatus, { color: status.color }]}>
                  {opening === table.id ? 'Entrando…' : status.label}
                </Text>
                {table.status === 'TAKEN' && table.waiter && <Text style={styles.tableWaiter}>{table.waiter}</Text>}
              </Pressable>
            );
          })}
        </View>

        <View style={styles.legend}>
          {Object.values(STATUS).map((status) => (
            <View key={status.label} style={styles.legendItem}>
              <View style={[styles.legendDot, { backgroundColor: status.color }]} />
              <Text style={styles.legendText}>{status.label}</Text>
            </View>
          ))}
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  body: {
    padding: 16,
    paddingTop: 0,
  },
  toolbar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 12,
  },
  toolbarText: {
    color: '#666666',
    flexShrink: 1,
    marginRight: 8,
  },
  refreshButton: {
    backgroundColor: '#0b0d11',
    borderRadius: 10,
    paddingVertical: 10,
    paddingHorizontal: 14,
  },
  refreshButtonText: {
    color: '#ffffff',
    fontWeight: 'bold',
  },
  loading: {
    marginTop: 40,
  },
  empty: {
    color: '#666666',
    textAlign: 'center',
    marginTop: 40,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginTop: 16,
  },
  table: {
    width: '48%',
    minHeight: 110,
    borderWidth: 2,
    borderRadius: 14,
    padding: 14,
    marginBottom: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  tableName: {
    color: '#0b0d11',
    fontSize: 18,
    fontWeight: 'bold',
  },
  tableStatus: {
    fontSize: 15,
    fontWeight: 'bold',
    marginTop: 6,
    textAlign: 'center',
  },
  tableWaiter: {
    color: '#666666',
    marginTop: 2,
  },
  legend: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    marginTop: 8,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: 8,
    marginBottom: 6,
  },
  legendDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    marginRight: 6,
  },
  legendText: {
    color: '#666666',
  },
});
