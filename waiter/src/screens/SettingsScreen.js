import { useState } from 'react';
import { Platform, Pressable, Text, TextInput, View } from 'react-native';
import { normalizeServer, ping } from '../api';
import { common } from '../theme';

// Primera vez: dónde está DonPosOrder (la PC de la caja). Antes de guardar se prueba.
export default function SettingsScreen({ server, onSave, onCancel }) {
  const [draft, setDraft] = useState(
    server ?? (Platform.OS === 'web' ? `${window.location.hostname}:8080` : ''),
  );
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const save = () => {
    const address = normalizeServer(draft);

    setSaving(true);
    setError('');
    ping(address)
      .then(() => onSave(address))
      .catch(() => setError(`No se pudo conectar con ${address}`))
      .finally(() => setSaving(false));
  };

  const canSave = draft.trim() !== '' && !saving;

  return (
    <View style={common.centered}>
      <View style={common.card}>
        <Text style={common.title}>Configurar Waiter</Text>
        <Text style={common.subtitle}>Escriba la dirección de DonPosOrder (la PC de la caja).</Text>

        <Text style={common.sectionTitle}>Servidor</Text>
        <TextInput
          style={common.input}
          value={draft}
          onChangeText={setDraft}
          placeholder="192.168.1.8:8080"
          autoCapitalize="none"
          autoCorrect={false}
          keyboardType="url"
        />

        {error !== '' && <Text style={common.error}>{error}</Text>}

        <Pressable style={[common.button, !canSave && common.buttonDisabled]} disabled={!canSave} onPress={save}>
          <Text style={common.buttonText}>{saving ? 'Probando…' : 'Guardar'}</Text>
        </Pressable>
        {onCancel && (
          <Pressable style={common.linkButton} onPress={onCancel}>
            <Text style={common.linkButtonText}>Cancelar</Text>
          </Pressable>
        )}
      </View>
    </View>
  );
}
