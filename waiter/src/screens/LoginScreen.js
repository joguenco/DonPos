import { useState } from 'react';
import { Pressable, Text, TextInput, View } from 'react-native';
import { login } from '../api';
import { common } from '../theme';

// El mesero entra con su usuario y clave de DonPos (solo los del rol de mesero)
export default function LoginScreen({ server, message, onLogin, onChangeServer }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(message ?? '');
  const [loading, setLoading] = useState(false);

  const enter = () => {
    setLoading(true);
    setError('');
    login(server, username.trim(), password)
      .then(onLogin)
      .catch((loginError) => setError(loginError.message))
      .finally(() => setLoading(false));
  };

  const canEnter = username.trim() !== '' && !loading;

  return (
    <View style={common.centered}>
      <View style={common.card}>
        <Text style={common.title}>Waiter</Text>
        <Text style={common.subtitle}>Entre con su usuario de DonPos.</Text>

        <Text style={common.sectionTitle}>Usuario</Text>
        <TextInput
          style={common.input}
          value={username}
          onChangeText={setUsername}
          placeholder="Mesero"
          autoCapitalize="none"
          autoCorrect={false}
        />

        <Text style={common.sectionTitle}>Contraseña</Text>
        <TextInput
          style={common.input}
          value={password}
          onChangeText={setPassword}
          secureTextEntry
          autoCapitalize="none"
          autoCorrect={false}
          onSubmitEditing={() => canEnter && enter()}
        />

        {error !== '' && <Text style={common.error}>{error}</Text>}

        <Pressable style={[common.button, !canEnter && common.buttonDisabled]} disabled={!canEnter} onPress={enter}>
          <Text style={common.buttonText}>{loading ? 'Entrando…' : 'Entrar'}</Text>
        </Pressable>
        <Pressable style={common.linkButton} onPress={onChangeServer}>
          <Text style={common.linkButtonText}>Servidor: {server} (cambiar)</Text>
        </Pressable>
      </View>
    </View>
  );
}
