import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

console.log('[firebase] Инициализация Firebase...');

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID
};

console.log('[firebase] Конфигурация Firebase:', {
  projectId: firebaseConfig.projectId,
  storageBucket: firebaseConfig.storageBucket,
  authDomain: firebaseConfig.authDomain,
  hasApiKey: !!firebaseConfig.apiKey,
  hasAppId: !!firebaseConfig.appId
});

const app = initializeApp(firebaseConfig);
console.log('[firebase] Firebase App инициализирован');

export const db = getFirestore(app);
console.log('[firebase] Firestore инициализирован');

export const storage = getStorage(app);
console.log('[firebase] Storage инициализирован:', storage.app.name);
