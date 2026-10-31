import { useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator, AppState, BackHandler, FlatList, Pressable, ScrollView, StyleSheet, Text, View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import {
  addProduct, closeTable, getCategories, getProducts, openTable, removeLine, signalTable,
} from '../api';
import { common, money } from '../theme';

// Mientras el mesero está dentro de la mesa, el celular avisa "sigo aquí" cada 30 segundos;
// si deja de avisar 2 minutos, DonPosOrder suelta el candado
const SIGNAL_INTERVAL = 30_000;

const MENU = 'menu';
const ORDER = 'order';

// DonPos guarda una línea por cada toque; en pantalla se juntan las del mismo producto y precio
function groupLines(lines) {
  const groups = new Map();

  for (const line of lines) {
    const key = `${line.productId}|${line.price}`;
    if (!groups.has(key)) {
      groups.set(key, {
        key, productId: line.productId, name: line.name, price: line.price, quantity: 0, total: 0, indexes: [],
      });
    }
    const group = groups.get(key);
    group.quantity += line.quantity;
    group.total += line.total;
    group.indexes.push(line.index);
  }
  return [...groups.values()];
}

export default function OrderScreen({ table, initialOrder, username, onExit }) {
  const insets = useSafeAreaInsets();
  const [order, setOrder] = useState(initialOrder);
  const [tab, setTab] = useState(MENU);
  const [categories, setCategories] = useState(null);
  const [categoryId, setCategoryId] = useState(null);
  const [products, setProducts] = useState(null);
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState('');
  const leaving = useRef(false);

  const lines = order?.lines ?? [];
  const groups = groupLines(lines);
  const itemCount = lines.reduce((sum, line) => sum + line.quantity, 0);

  // Cuántos de cada producto ya se pidieron: es el número azul de cada botón del menú
  const orderedByProduct = new Map();
  for (const line of lines) {
    orderedByProduct.set(line.productId, (orderedByProduct.get(line.productId) ?? 0) + line.quantity);
  }

  // La mesa ya no es del mesero (se cobró o la tiene la caja): vuelve a las mesas con el aviso
  const leaveWith = (message) => {
    if (!leaving.current) {
      leaving.current = true;
      onExit(message);
    }
  };

  // Salir con el botón: la mesa sigue ocupada, solo se quita el candado
  const goBack = () => {
    if (!leaving.current) {
      leaving.current = true;
      closeTable(table.id).catch(() => {}).finally(() => onExit(''));
    }
  };

  // Vuelve a entrar a la mesa (pone otra vez el candado) y trae el pedido actualizado
  const reopen = () => openTable(table.id).then(setOrder);

  const handleError = async (error, retry) => {
    if (error.code === 'GONE') {
      leaveWith(error.message);
    } else if (error.code === 'NOT_HELD') {
      // Se perdió la señal un rato: se vuelve a entrar y se repite lo que el mesero hizo
      try {
        await reopen();
        if (retry) {
          await retry();
        }
      } catch (reopenError) {
        leaveWith(reopenError.message);
      }
    } else if (error.code !== 'SESSION') {
      setNotice(error.message);
    }
  };

  useEffect(() => {
    getCategories()
      .then((list) => {
        setCategories(list);
        if (list.length > 0) {
          setCategoryId(list[0].id);
        }
      })
      .catch((error) => setNotice(error.message));
  }, []);

  useEffect(() => {
    if (!categoryId) {
      return;
    }
    setProducts(null);
    getProducts(categoryId)
      .then(setProducts)
      .catch((error) => setNotice(error.message));
  }, [categoryId]);

  useEffect(() => {
    const timer = setInterval(() => {
      signalTable(table.id).catch((error) => handleError(error));
    }, SIGNAL_INTERVAL);

    // Al volver a la app (celular en el bolsillo) se pone el candado de nuevo
    const appState = AppState.addEventListener('change', (state) => {
      if (state === 'active' && !leaving.current) {
        reopen().catch((error) => leaveWith(error.message));
      }
    });

    // El botón atrás de Android hace lo mismo que "Mesas"
    const back = BackHandler.addEventListener('hardwareBackPress', () => {
      goBack();
      return true;
    });

    return () => {
      clearInterval(timer);
      appState.remove();
      back.remove();
    };
  }, [table.id]);

  useEffect(() => {
    if (!notice) {
      return;
    }
    const timer = setTimeout(() => setNotice(''), 4000);
    return () => clearTimeout(timer);
  }, [notice]);

  // Un cambio a la vez: así el pedido de la pantalla siempre es el último que guardó el servidor
  const change = (action) => {
    if (busy) {
      return;
    }
    setBusy(true);
    action()
      .catch((error) => handleError(error, action))
      .finally(() => setBusy(false));
  };

  const add = (product) => {
    if (product.variablePrice) {
      setNotice('Este producto tiene precio variable, agrégalo en caja');
      return;
    }
    change(() => addProduct(table.id, product.id).then(setOrder));
  };

  // + agrega otra línea del mismo producto; − quita la última que se agregó
  const addOneMore = (group) => change(() => addProduct(table.id, group.productId).then(setOrder));
  const removeOne = (group) => change(() => removeLine(table.id, group.indexes[group.indexes.length - 1]).then(setOrder));

  return (
    <View style={common.container}>
      <View style={[common.header, { paddingTop: insets.top + 12 }]}>
        <View>
          <Text style={common.headerTitle}>{table.name}</Text>
          <Text style={common.headerSubtitle}>Mesero: {username}</Text>
        </View>
        <Pressable style={common.headerButton} onPress={goBack}>
          <Text style={common.headerButtonText}>← Mesas</Text>
        </Pressable>
      </View>

      <View style={styles.tabs}>
        <Pressable style={[styles.tab, tab === MENU && styles.tabSelected]} onPress={() => setTab(MENU)}>
          <Text style={[styles.tabText, tab === MENU && styles.tabTextSelected]}>Menú</Text>
        </Pressable>
        <Pressable style={[styles.tab, tab === ORDER && styles.tabSelected]} onPress={() => setTab(ORDER)}>
          <Text style={[styles.tabText, tab === ORDER && styles.tabTextSelected]}>Pedido ({itemCount})</Text>
        </Pressable>
      </View>

      {notice !== '' && (
        <View style={common.notice}>
          <Text style={common.noticeText}>{notice}</Text>
        </View>
      )}

      {tab === MENU ? (
        <View style={styles.content}>
          <View>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.categories}>
              {(categories ?? []).map((category) => (
                <Pressable
                  key={category.id}
                  style={[styles.category, category.id === categoryId && styles.categorySelected]}
                  onPress={() => setCategoryId(category.id)}
                >
                  <Text style={[styles.categoryText, category.id === categoryId && styles.categoryTextSelected]}>
                    {category.name}
                  </Text>
                </Pressable>
              ))}
            </ScrollView>
          </View>

          {products === null ? (
            <ActivityIndicator style={styles.loading} size="large" color="#0b0d11" />
          ) : (
            <FlatList
              data={products}
              keyExtractor={(product) => product.id}
              numColumns={2}
              columnWrapperStyle={styles.productRow}
              contentContainerStyle={styles.products}
              ListEmptyComponent={<Text style={styles.empty}>Esta categoría no tiene productos</Text>}
              renderItem={({ item }) => {
                const ordered = orderedByProduct.get(item.id);
                return (
                  <Pressable
                    style={[styles.product, (busy || item.variablePrice) && styles.productDisabled]}
                    onPress={() => add(item)}
                  >
                    <Text style={styles.productName}>{item.name}</Text>
                    <Text style={styles.productPrice}>{item.variablePrice ? 'En caja' : money(item.price)}</Text>
                    {ordered > 0 && (
                      <View style={styles.badge}>
                        <Text style={styles.badgeText}>{ordered}</Text>
                      </View>
                    )}
                  </Pressable>
                );
              }}
            />
          )}
        </View>
      ) : (
        <ScrollView style={styles.content} contentContainerStyle={styles.orderList}>
          {groups.length === 0 && <Text style={styles.empty}>Todavía no hay productos</Text>}
          {groups.map((group) => (
            <View key={group.key} style={styles.group}>
              <View style={styles.groupInfo}>
                <Text style={styles.groupName}>{group.name}</Text>
                <Text style={styles.groupDetail}>{money(group.price)} c/u · {money(group.total)}</Text>
              </View>
              <View style={styles.stepper}>
                <Pressable style={[styles.stepButton, styles.stepRemove]} onPress={() => removeOne(group)} disabled={busy}>
                  <Text style={[styles.stepText, styles.stepRemoveText]}>−</Text>
                </Pressable>
                <Text style={styles.quantity}>{group.quantity}</Text>
                <Pressable style={styles.stepButton} onPress={() => addOneMore(group)} disabled={busy}>
                  <Text style={styles.stepText}>+</Text>
                </Pressable>
              </View>
            </View>
          ))}
        </ScrollView>
      )}

      {/* Barra de abajo: en el menú lleva al pedido y en el pedido vuelve al menú */}
      <Pressable
        style={[styles.bar, { paddingBottom: insets.bottom + 14 }]}
        onPress={() => setTab(tab === MENU ? ORDER : MENU)}
      >
        <Text style={styles.barText}>
          {itemCount} {itemCount === 1 ? 'producto' : 'productos'} · {money(order?.total)}
        </Text>
        <Text style={styles.barAction}>{tab === MENU ? 'Ver pedido ›' : '‹ Seguir pidiendo'}</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  tabs: {
    flexDirection: 'row',
    backgroundColor: '#ffffff',
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  tab: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 3,
    borderBottomColor: 'transparent',
  },
  tabSelected: {
    borderBottomColor: '#0b0d11',
  },
  tabText: {
    color: '#666666',
    fontSize: 16,
  },
  tabTextSelected: {
    color: '#0b0d11',
    fontWeight: 'bold',
  },
  content: {
    flex: 1,
  },
  categories: {
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  category: {
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#c8c8c8',
    borderRadius: 20,
    paddingVertical: 8,
    paddingHorizontal: 16,
    marginRight: 8,
  },
  categorySelected: {
    backgroundColor: '#0b0d11',
    borderColor: '#0b0d11',
  },
  categoryText: {
    color: '#0b0d11',
    fontWeight: 'bold',
  },
  categoryTextSelected: {
    color: '#ffffff',
  },
  loading: {
    marginTop: 30,
  },
  empty: {
    color: '#666666',
    textAlign: 'center',
    paddingVertical: 24,
  },
  products: {
    paddingHorizontal: 12,
    paddingBottom: 16,
  },
  productRow: {
    justifyContent: 'space-between',
  },
  product: {
    width: '48.5%',
    minHeight: 90,
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    marginBottom: 10,
    justifyContent: 'center',
  },
  productDisabled: {
    opacity: 0.5,
  },
  productName: {
    color: '#0b0d11',
    fontSize: 15,
    fontWeight: 'bold',
    paddingRight: 22,
  },
  productPrice: {
    color: '#1d5fd1',
    marginTop: 4,
  },
  badge: {
    position: 'absolute',
    top: 8,
    right: 8,
    minWidth: 24,
    height: 24,
    borderRadius: 12,
    paddingHorizontal: 6,
    backgroundColor: '#1d5fd1',
    alignItems: 'center',
    justifyContent: 'center',
  },
  badgeText: {
    color: '#ffffff',
    fontWeight: 'bold',
  },
  orderList: {
    padding: 12,
  },
  group: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    marginBottom: 8,
  },
  groupInfo: {
    flex: 1,
    marginRight: 8,
  },
  groupName: {
    color: '#0b0d11',
    fontSize: 16,
    fontWeight: 'bold',
  },
  groupDetail: {
    color: '#666666',
    marginTop: 2,
  },
  stepper: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  stepButton: {
    width: 40,
    height: 40,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#1d5fd1',
    alignItems: 'center',
    justifyContent: 'center',
  },
  stepRemove: {
    borderColor: '#c0392b',
  },
  stepText: {
    color: '#1d5fd1',
    fontSize: 22,
    fontWeight: 'bold',
  },
  stepRemoveText: {
    color: '#c0392b',
  },
  quantity: {
    color: '#0b0d11',
    fontSize: 18,
    fontWeight: 'bold',
    minWidth: 36,
    textAlign: 'center',
  },
  bar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#e7f0ff',
    borderTopWidth: 1,
    borderTopColor: '#c9dbfb',
    paddingHorizontal: 16,
    paddingTop: 14,
  },
  barText: {
    color: '#1d5fd1',
    fontSize: 15,
  },
  barAction: {
    color: '#1d5fd1',
    fontSize: 15,
    fontWeight: 'bold',
  },
});
