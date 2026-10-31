import { useState } from 'react';
import { FlatList, Modal, Pressable, StyleSheet, Text, View } from 'react-native';

// Lista desplegable: muestra la opción escogida y al tocarla abre las demás
// options: [{ value, label }]
export default function Select({ title, value, options, onChange }) {
  const [open, setOpen] = useState(false);
  const current = options.find((option) => option.value === value);

  const choose = (option) => {
    setOpen(false);
    onChange(option.value);
  };

  return (
    <>
      <Pressable style={styles.select} onPress={() => setOpen(true)}>
        <Text style={styles.selectText}>{current?.label ?? 'Escoger'}</Text>
        <Text style={styles.arrow}>▾</Text>
      </Pressable>

      <Modal visible={open} transparent animationType="fade" onRequestClose={() => setOpen(false)}>
        <Pressable style={styles.overlay} onPress={() => setOpen(false)}>
          <View style={styles.card}>
            <Text style={styles.title}>{title}</Text>
            <FlatList
              data={options}
              keyExtractor={(option) => String(option.value)}
              renderItem={({ item }) => (
                <Pressable
                  style={[styles.option, item.value === value && styles.optionSelected]}
                  onPress={() => choose(item)}
                >
                  <Text style={styles.optionText}>{item.label}</Text>
                  {item.value === value && <Text style={styles.check}>✓</Text>}
                </Pressable>
              )}
            />
          </View>
        </Pressable>
      </Modal>
    </>
  );
}

const styles = StyleSheet.create({
  select: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#c8c8c8',
    borderRadius: 10,
    paddingVertical: 12,
    paddingHorizontal: 14,
  },
  selectText: {
    color: '#0b0d11',
    fontSize: 16,
    fontWeight: 'bold',
  },
  arrow: {
    color: '#666666',
    fontSize: 16,
  },
  overlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
    backgroundColor: 'rgba(11, 13, 17, 0.6)',
  },
  card: {
    width: '100%',
    maxWidth: 420,
    maxHeight: '80%',
    backgroundColor: '#ffffff',
    borderRadius: 16,
    padding: 20,
  },
  title: {
    color: '#0b0d11',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  option: {
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
  optionSelected: {
    backgroundColor: '#e8fff1',
    borderColor: '#1f9d55',
  },
  optionText: {
    color: '#0b0d11',
    fontSize: 16,
  },
  check: {
    color: '#1f9d55',
    fontSize: 18,
    fontWeight: 'bold',
  },
});
