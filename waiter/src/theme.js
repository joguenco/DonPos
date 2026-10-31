import { StyleSheet } from 'react-native';

// Colores de cada estado de mesa: libre, mía, de otro mesero, la caja la está facturando
export const STATUS = {
  FREE: { color: '#1f9d55', background: '#e8fff1', label: 'Libre' },
  MINE: { color: '#1d5fd1', background: '#e7f0ff', label: 'Mía' },
  TAKEN: { color: '#c0392b', background: '#fdecea', label: 'Ocupada' },
  LOCKED: { color: '#8a6d00', background: '#fff6d6', label: 'En uso para facturación' },
};

export const money = (value) => `$${(value ?? 0).toFixed(2)}`;

export const common = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f0f0f0',
  },
  header: {
    backgroundColor: '#0b0d11',
    paddingHorizontal: 16,
    paddingBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerTitle: {
    color: '#ffffff',
    fontSize: 20,
    fontWeight: 'bold',
  },
  headerSubtitle: {
    color: '#b8bcc4',
    marginTop: 2,
  },
  headerButton: {
    borderWidth: 1,
    borderColor: '#ffffff',
    borderRadius: 10,
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  headerButtonText: {
    color: '#ffffff',
    fontWeight: 'bold',
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  card: {
    width: '100%',
    maxWidth: 460,
    backgroundColor: '#ffffff',
    borderRadius: 16,
    padding: 24,
  },
  title: {
    color: '#0b0d11',
    fontSize: 22,
    fontWeight: 'bold',
  },
  subtitle: {
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
  linkButton: {
    marginTop: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  linkButtonText: {
    color: '#666666',
  },
  notice: {
    backgroundColor: '#0b0d11',
    paddingVertical: 10,
    paddingHorizontal: 16,
  },
  noticeText: {
    color: '#ffffff',
    textAlign: 'center',
  },
});
